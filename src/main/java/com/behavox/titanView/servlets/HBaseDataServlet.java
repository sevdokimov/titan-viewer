package com.behavox.titanView.servlets;

import com.behavox.titanView.HBaseManager;
import com.behavox.titanView.Utils;
import com.behavox.titanView.hbase.view.HBaseConfigManager;
import com.behavox.titanView.hbase.view.TableView;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(DataServlet.class);

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
                if (e.getCause() instanceof IOException) {
                    throw (IOException) e.getCause();
                }

                throw Throwables.propagate(e);
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

        return new TableDescrAndContent(tableDescr, scanResult, tableView);
    }

    public ScanResult scan(HTable hTable, HttpServletRequest request) throws IOException {
        Scan scan = new Scan();

        String firstRow = request.getParameter("startRow");
        if (firstRow != null) {
            scan.setStartRow(Bytes.fromHex(firstRow));
        }

        String stopRow = request.getParameter("stopRow");
        if (stopRow != null) {
            scan.setStopRow(Bytes.fromHex(stopRow));
        }

        int limit = 30;

        scan.setMaxResultsPerColumnFamily(1024);
        scan.setBatch(limit + 1);

        ScanResult res = new ScanResult();

        try (ResultScanner result = hTable.getScanner(scan)) {
            Result row;
            while ((row = result.next()) != null) {
                res.rows.add(new RowDescr(row));

                if (res.rows.size() >= limit) {
                    Result nextRow = result.next();

                    if (nextRow != null) {
                        res.nextRowKey = nextRow.getRow();
                    }

                    break;
                }
            }
        }

        return res;
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
                    if (value != null)
                        m.put(Bytes.toString(q), value);
                });

                if (!m.isEmpty())
                    data.put(Bytes.toString(family), m);
            });
        }
    }

    private static class ScanResult {
        private byte[] nextRowKey;

        private final List<RowDescr> rows = new ArrayList<>();
    }

    private static class TableDescrAndContent {
        private final TableDescr table;

        private final ScanResult scan;

        private final TableView tableView;

        public TableDescrAndContent(TableDescr descr, ScanResult scan, TableView tableView) {
            this.table = descr;
            this.scan = scan;
            this.tableView = tableView;
        }
    }

}