package com.behavox.hbaseView.servlets;

import com.behavox.hbaseView.FilterUtils;
import com.behavox.hbaseView.HBaseManager;
import com.behavox.hbaseView.Utils;
import com.behavox.hbaseView.hbase.view.HBaseConfigManager;
import com.behavox.hbaseView.hbase.view.TableView;
import com.behavox.hbaseView.titan.TitanUtils;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class HBaseDataServlet extends AbstractServlet {

    private static final int MAX_CELL_SIZE = 20 * 1024;

    private static final Logger log = LoggerFactory.getLogger(HBaseDataServlet.class);

    private static final Map<String, Method> methods = new HashMap<>();

    static {
        for (Method method : HBaseDataServlet.class.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                Class<?>[] parameterTypes = method.getParameterTypes();

                if ((parameterTypes.length == 2
                        && parameterTypes[0].equals(HTable.class)
                        && parameterTypes[1].equals(HttpServletRequest.class))
                        || (
                        parameterTypes.length == 1 && parameterTypes[0].equals(HttpServletRequest.class)
                )) {
                    Method oldMethod = methods.put(method.getName(), method);

                    assert oldMethod == null;
                }
            }
        }
    }

    @Override
    protected void processGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String contextPath = req.getRequestURI();

        int idx = contextPath.lastIndexOf('/');

        String methodName = contextPath.substring(idx + 1);

        Method method = methods.get(methodName);

        Object[] args;

        if (method.getParameters().length == 2) {
            String tableName = req.getParameter("table");

            if (Strings.isNullOrEmpty(tableName)) {
                resp.sendError(404, "Required parameter 'table' is not present");

                return;
            }

            HTable hTable = HBaseManager.getInstance().getTable(tableName);

            args = new Object[]{hTable, req};
        }
        else {
            args = new Object[]{req};
        }

        try {
            Object res;

            try {
                res = method.invoke(this, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            } catch (IllegalAccessException e) {
                throw Throwables.propagate(e);
            }

            if (method.getReturnType() == void.class) {
                resp.getWriter().print("done");
            } else if (method.getReturnType().equals(Object.class)) {
                Utils.BHEX_GSON.toJson(res, resp.getWriter());
            } else {
                Utils.BHEX_GSON.toJson(res, method.getGenericReturnType(), resp.getWriter());
            }

        } catch (Throwable t) {
            log.error("Failed to process request " + req.toString(), t);

            resp.sendError(505, "Internal error: " + t.toString());
        }
    }

    public List<String> listNamespaces(HttpServletRequest request) throws IOException {
        return Stream.of(HBaseManager.getInstance().getAdmin().listNamespaceDescriptors())
                .map(NamespaceDescriptor::getName)
                .collect(Collectors.toList());
    }

    public List<TableDescr> listTables(HttpServletRequest request) throws IOException {
        String ns = request.getParameter("ns");

        HBaseAdmin admin = HBaseManager.getInstance().getAdmin();
        TableName[] tableNames = admin.listTableNamesByNamespace(ns);

        List<TableDescr> res = new ArrayList<>(tableNames.length);

        for (TableName tableName : tableNames) {
            HTable table = HBaseManager.getInstance().getTable(tableName.getNameAsString());
            res.add(new TableDescr(table.getTableDescriptor()));
        }

        return res;
    }

    public TableDescr tableInfo(HTable hTable, HttpServletRequest request) throws IOException {
        return new TableDescr(hTable.getTableDescriptor());
    }

    private static class TableDescr {
        private final String fullName;
        private final String q;

        private final List<String> families;

        public TableDescr(HTableDescriptor d) {
            fullName = d.getNameAsString();
            this.q = d.getTableName().getQualifierAsString();

            families = new ArrayList<>();

            for (HColumnDescriptor columnDescriptor : d.getColumnFamilies()) {
                families.add(columnDescriptor.getNameAsString());
            }
        }
    }

    public TableDescrAndContent firstScan(HTable hTable, HttpServletRequest request) throws IOException {
        ScanResult scanResult = scan(hTable, request);

        TableDescr tableDescr = tableInfo(hTable, request);

        TableView tableView = HBaseConfigManager.getInstance().getOrCreateTableView(hTable.getName().getQualifierAsString());

        synchronized (tableView) {
            tableView.getOrCreateKeySettings();
        }

        TableDescrAndContent res = new TableDescrAndContent(tableDescr, scanResult, tableView);

        List<String> otherNamespaces = Stream.of(HBaseManager.getInstance().getAdmin().listTables())
                .map(HTableDescriptor::getTableName)
                .filter(it -> {
                    return Arrays.equals(it.getQualifier(), hTable.getName().getQualifier())
                        && !Arrays.equals(it.getNamespace(), hTable.getName().getNamespace());
        }).map(TableName::getNamespaceAsString).distinct().sorted().collect(Collectors.toList());

        res.otherNamespaces = otherNamespaces;

        return res;
    }

    public ComparisonResult compare(HttpServletRequest request) throws IOException {
        String tableName1 = request.getParameter("table1");
        String tableName2 = request.getParameter("table2");

        HBaseAdmin admin = HBaseManager.getInstance().getAdmin();

        if (!admin.tableExists(tableName1))
            return new ComparisonResult("Table " + tableName1 + " is not exist");

        if (!admin.tableExists(tableName2))
            return new ComparisonResult("Table " + tableName2 + " is not exist");

        HTable table1 = HBaseManager.getInstance().getTable(tableName1);
        HTable table2 = HBaseManager.getInstance().getTable(tableName2);

        Scan scan = new Scan();

        ComparisonResult res = new ComparisonResult(null);

        try (ResultScanner scanner1 = table1.getScanner(scan)) {
            try (ResultScanner scanner2 = table2.getScanner(scan)) {

                do {
                    Result result1 = scanner1.next();
                    Result result2 = scanner2.next();

                    if (result1 == null && result2 == null)
                        break;

                    if (result1 == null) {
                        res.unmatchedRowKey = result2.getRow();
                        break;
                    }

                    if (result2 == null) {
                        res.unmatchedRowKey = result1.getRow();
                        break;
                    }

                    BinaryComparator key1 = new BinaryComparator(result1.getRow());

                    int cmp = key1.compareTo(result2.getRow());

                    if (cmp > 0) {
                        res.unmatchedRowKey = result2.getRow();
                        break;
                    }
                    if (cmp < 0) {
                        res.unmatchedRowKey = result1.getRow();
                        break;
                    }

                    try {
                        Result.compareResults(result1, result2);
                    } catch (Exception e) {
                        res.unmatchedRowKey = result1.getRow();
                        break;
                    }

                    res.rowCount++;
                }
                while (true);
            }
        }
        catch (TableNotEnabledException e) {
            return new ComparisonResult(e.getMessage());
        }

        return res;
    }

    public ScanResult scan(HTable hTable, HttpServletRequest request) throws IOException {
        Scan scan = new Scan();

        String firstRow = request.getParameter("startRow");
        if (firstRow != null && firstRow.length() > 0) {
            scan.setStartRow(Bytes.fromHex(firstRow));
        }

        String stopRow = request.getParameter("stopRow");
        if (stopRow != null && stopRow.length() > 0) {
            scan.setStopRow(Bytes.fromHex(stopRow));
        }

        ScanResult res = new ScanResult();

        String filter = request.getParameter("filter");
        if (filter != null && !filter.isEmpty()) {
            scan.setFilter(FilterUtils.toHBaseFilter(filter));
        }
        else {
            String gFilter = request.getParameter("gFilter");

            if (!HbaseGFilterManager.isEmptyOrComment(gFilter)) {
                try {
                    Filter f = HbaseGFilterManager.getInstance().evaluateFilter(gFilter);

                    scan.setFilter(f);
                } catch (ScriptException e) {
                    res.error = TitanUtils.toString(e);
                    return res;
                }
            }
        }

        String[] columns = request.getParameterValues("column");

        if (columns != null && columns.length > 0) {
            for (String column : columns) {
                int idx = column.indexOf(':');
                if (idx == -1) {
                    HColumnDescriptor[] columnFamilies = hTable.getTableDescriptor().getColumnFamilies();
                    if (columnFamilies.length != 1)
                        throw new RuntimeException("Column list is specified incorrectly");

                    scan.addColumn(columnFamilies[0].getName(), Bytes.toBytes(column));
                }

                scan.addColumn(Bytes.toBytes(column.substring(0, idx)), Bytes.toBytes(column.substring(idx + 1)));
            }
        }

        int limit = 30;

        scan.setMaxResultSize(limit + 1);

        try (ResultScanner resultScanner = hTable.getScanner(scan)) {
            for (Result result : resultScanner) {
                res.rows.add(new RowDescr(result));

                if (res.rows.size() >= limit) {
                    Result nextRow = resultScanner.next();

                    if (nextRow != null) {
                        res.nextRowKey = nextRow.getRow();
                    }

                    break;
                }
            }
        }

        return res;
    }

    public void keyRendererChanged(HTable hTable, HttpServletRequest request) {
        String rendererName = request.getParameter("rendererName");
        String rendererAttr = request.getParameter("rendererAttr");

        TableView tableView = HBaseConfigManager.getInstance().getOrCreateTableView(hTable.getName().getQualifierAsString());

        synchronized (tableView) {
            TableView.KeySettings keySettings = tableView.getOrCreateKeySettings();
            keySettings.setRendererName(rendererName);
            keySettings.setRendererAttr(rendererAttr);

            HBaseConfigManager.getInstance().saveTableView(tableView);
        }
    }

    public void columnRendererChanged(HTable hTable, HttpServletRequest request) {
        String family = request.getParameter("family");
        String q = request.getParameter("q");

        String rendererName = request.getParameter("rendererName");
        String rendererAttr = request.getParameter("rendererAttr");

        TableView tableView = HBaseConfigManager.getInstance().getOrCreateTableView(hTable.getName().getQualifierAsString());

        synchronized (tableView) {
            TableView.ColumnSettings settings = tableView.getOrCreateColumnSettings(family, q);

            settings.setRendererName(rendererName);
            settings.setRendererAttr(rendererAttr);

            HBaseConfigManager.getInstance().saveTableView(tableView);
        }
    }

    private static class RowDescr {
        private byte[] key;

        private final Map<String, Map<String, byte[]>> data = new LinkedHashMap<>();

        public RowDescr(Result row) {
            key = row.getRow();

            row.getNoVersionMap().forEach((family, fMap) -> {
                Map<String, byte[]> m = new LinkedHashMap<>();

                fMap.forEach((q, value) -> {
                    if (value != null) {
                        if (value.length > MAX_CELL_SIZE)
                            value = Arrays.copyOf(value, MAX_CELL_SIZE);

                        m.put(Bytes.toString(q), value);
                    }
                });

                if (!m.isEmpty())
                    data.put(Bytes.toString(family), m);
            });
        }
    }

    private static class ScanResult {
        private byte[] nextRowKey;

        private final List<RowDescr> rows = new ArrayList<>();

        private String error;
    }

    private static class TableDescrAndContent {
        private final TableDescr table;

        private final ScanResult scan;

        private final TableView tableView;

        private List<String> otherNamespaces;

        public TableDescrAndContent(TableDescr descr, ScanResult scan, TableView tableView) {
            this.table = descr;
            this.scan = scan;
            this.tableView = tableView;
        }
    }

    public static class ComparisonResult {
        private String error;

        private byte[] unmatchedRowKey;

        private long rowCount;

        public ComparisonResult(String error) {
            this.error = error;
        }
    }
}
