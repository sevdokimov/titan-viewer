package com.behavox.hbaseView.titan;

import com.behavox.hbaseView.HBaseManager;
import com.behavox.hbaseView.Utils;
import com.behavox.hbaseView.servlets.AbstractServlet;
import com.behavox.hbaseView.titan.json.FullVertexJson;
import com.behavox.hbaseView.titan.json.HalfEdgeJson;
import com.behavox.hbaseView.titan.json.ObjectJson;
import com.behavox.hbaseView.titan.json.ShortEdgeJson;
import com.behavox.hbaseView.titan.viewModel.TitanConfig;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.thinkaurelius.titan.core.TitanEdge;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.tinkerpop.gremlin.groovy.jsr223.GremlinGroovyScriptEngine;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 *
 */
@SuppressWarnings({"UnusedParameters", "unused"})
public class TitanDataServlet extends AbstractServlet {

    private static final Logger log = LoggerFactory.getLogger(TitanDataServlet.class);

    private static final Map<String, Method> methods = new HashMap<>();

    static {
        for (Method method : TitanDataServlet.class.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                Class<?>[] parameterTypes = method.getParameterTypes();

                if ((parameterTypes.length == 2
                        && parameterTypes[0].equals(TitanGraph.class)
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

    private final GremlinGroovyScriptEngine engine = new GremlinGroovyScriptEngine();

    @Override
    protected void processGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String contextPath = req.getRequestURI();

        int idx = contextPath.lastIndexOf('/');

        String methodName = contextPath.substring(idx + 1);

        Method method = methods.get(methodName);

        Object[] args;

        TitanGraph graph = null;

        if (method.getParameters().length == 2) {
            String table = req.getParameter("table");

            if (Strings.isNullOrEmpty(table)) {
                resp.sendError(404, "Required parameter 'table' is not present");

                return;
            }

            graph = GraphManager.getInstance().getGraph(table);

            args = new Object[]{graph, req};
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

            if (method.getReturnType().equals(Object.class)) {
                Utils.GSON.toJson(res, resp.getWriter());
            } else {
                Utils.GSON.toJson(res, method.getGenericReturnType(), resp.getWriter());
            }

        } catch (Throwable t) {
            log.error("Failed to handle data-request", t);

            if (graph != null) {
                graph.tx().rollback();
                graph = null;
            }

            resp.sendError(505, "Internal error: " + t.toString());
        } finally {
            if (graph != null)
                graph.tx().commit();
        }
    }

    protected TitanConfig getConfig() {
        return (TitanConfig) getRequest().getAttribute("cfg");
    }

    public QueryResult vertexList(TitanGraph g, HttpServletRequest req) {
        int limit = getIntParam("limit", 50);

        QueryResult res = new QueryResult();

        long startTime = System.currentTimeMillis();

        res.fillElements(g.vertices(), limit);

        res.executionTime = System.currentTimeMillis() - startTime;

        return res;
    }

    public QueryResult executeQuery(TitanGraph g, HttpServletRequest req) {
        int limit = getIntParam("limit", 50);

        QueryResult res = new QueryResult();

        String scriptSource = req.getParameter("query");
        if (scriptSource == null || scriptSource.isEmpty()) {
            res.error = "Script is empty";

            return res;
        }

        scriptSource = scriptSource.trim();

        CompiledScript compiledScript;

        try {
            compiledScript = engine.compile(scriptSource);
        } catch (ScriptException e) {
            res.error = TitanUtils.toString(e);

            return res;
        }

        final Bindings bindings = engine.createBindings();
        bindings.put("graph", g);
        bindings.put("g", g.traversal());

        long startTime = System.currentTimeMillis();

        try {
            Object r = compiledScript.eval(bindings);

            if (r instanceof GraphTraversal) {
                List<Object> itr = ((GraphTraversal) r).limit(limit).toList();

                res.fillElements(itr.iterator(), limit);
            }
            else {
                res.convertAndAdd(r);
            }
        } catch (ScriptException e) {
            res.error = TitanUtils.toString(e);
        }

        res.executionTime = System.currentTimeMillis() - startTime;

        return res;
    }

    public Object vertex(TitanGraph g, HttpServletRequest req) {
        long id = getLongParam("vId");

        Iterator<Vertex> itr = g.vertices(id);

        if (!itr.hasNext())
            return null;

        return new FullVertexJson((TitanVertex) itr.next());
    }

    public List<String> edgeLabels(TitanGraph g, HttpServletRequest req) {
        return TitanUtils.getEdgeLabels(g);
    }

    public String openGraph(TitanGraph g, HttpServletRequest req) {
        return "true";
    }

    public Map<String, EdgeListResult> vertexEdgesAllLabels(TitanGraph g, HttpServletRequest req) {
        long id = getLongParam("vId");

        Iterator<Vertex> itr = g.vertices(id);

        if (!itr.hasNext())
            return null;

        Vertex vertex = itr.next();

        int limit = getIntParam("limit", 10);

        boolean inVertex = "in".equals(req.getParameter("dir"));

        Map<String, EdgeListResult> res = new HashMap<>();

        for (String label : TitanUtils.getEdgeLabels(g)) {
            Iterator<Edge> edges = vertex.edges(inVertex ? Direction.IN : Direction.OUT, label);

            EdgeListResult edgeList = new EdgeListResult();

            while (edges.hasNext()) {
                Edge edge = edges.next();

                if (edgeList.edges.size() >= limit) {
                    edgeList.hasNext = true;

                    break;
                }

                HalfEdgeJson edgeJson = new HalfEdgeJson(vertex, edge);

                edgeList.edges.add(edgeJson);
            }

            if (!edgeList.edges.isEmpty())
                res.put(label, edgeList);
        }

        return res;
    }

    public Tables tableList(HttpServletRequest req) throws IOException {
        GraphManager gm = GraphManager.getInstance();

        Tables res = new Tables();

        gm.loadTables().stream().map(tableName -> new TitanTableDescr(tableName, gm.isGraphOpen(tableName))).forEach(res.titanTables::add);

        HBaseAdmin admin = HBaseManager.getInstance().getAdmin();

        for (NamespaceDescriptor descriptor : admin.listNamespaceDescriptors()) {
            String namespace = descriptor.getName();

            List<String> tables = new ArrayList<>();

            for (TableName tableName : admin.listTableNamesByNamespace(namespace)) {
                tables.add(tableName.getNameAsString());
            }

            tables.sort(null);

            res.hbaseTables.put(namespace, tables);
        }

        return res;
    }

    private static class QueryResult {
        public final List<Object> elements = new ArrayList<>();

        public boolean hasNext;

        private String error;

        private long executionTime;

        public void fillElements(Iterator<?> itr, int limit) {
            while (itr.hasNext()) {
                convertAndAdd(itr.next());

                if (elements.size() >= limit) {
                    hasNext = itr.hasNext();

                    break;
                }
            }
        }

        public void convertAndAdd(Object o) {
            if (o == null || o instanceof Number || o instanceof String || o instanceof Boolean) {
                elements.add(o);
            }
            else if (o instanceof TitanVertex) {
                elements.add(TitanUtils.format((TitanVertex) o));
            }
            else if (o instanceof TitanEdge) {
                elements.add(new ShortEdgeJson((TitanEdge) o));
            }
            else {
                elements.add(new ObjectJson(o));
            }
        }
    }

    private static class EdgeListResult {
        public final List<HalfEdgeJson> edges = new ArrayList<>();

        public boolean hasNext;
    }

    private static class TitanTableDescr {
        private final String name;
        private final boolean isOpen;

        public TitanTableDescr(String name, boolean isOpen) {
            this.name = name;
            this.isOpen = isOpen;
        }
    }

    private static class Tables {
        private List<TitanTableDescr> titanTables = new ArrayList<>();

        private Map<String, List<String>> hbaseTables = new HashMap<>();
    }
}
