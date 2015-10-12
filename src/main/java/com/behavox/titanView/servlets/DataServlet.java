package com.behavox.titanView.servlets;

import com.behavox.titanView.GraphManager;
import com.behavox.titanView.Utils;
import com.behavox.titanView.json.FullVertexJson;
import com.behavox.titanView.json.ObjectJson;
import com.behavox.titanView.json.HalfEdgeJson;
import com.behavox.titanView.json.ShortEdgeJson;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.thinkaurelius.titan.core.TitanEdge;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.gremlin.groovy.jsr223.GremlinGroovyScriptEngine;
import com.tinkerpop.gremlin.java.GremlinPipeline;
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
import java.util.stream.Collectors;

/**
 *
 */
@SuppressWarnings({"UnusedParameters", "unused"})
public class DataServlet extends AbstractServlet {

    private static final Logger log = LoggerFactory.getLogger(DataServlet.class);

    private static final Map<String, Method> methods = new HashMap<>();

    static {
        for (Method method : DataServlet.class.getDeclaredMethods()) {
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
                graph.rollback();
                graph = null;
            }

            resp.sendError(505, "Internal error: " + t.toString());
        } finally {
            if (graph != null)
                graph.commit();
        }
    }

    public QueryResult vertexList(TitanGraph g, HttpServletRequest req) {
        int limit = getIntParam("limit", 50);

        QueryResult res = new QueryResult();

        long startTime = System.currentTimeMillis();

        res.fillElements(g.getVertices().iterator(), limit);

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
            res.error = Utils.toString(e);

            return res;
        }

        final Bindings bindings = engine.createBindings();
        bindings.put("g", g);

        long startTime = System.currentTimeMillis();

        try {
            Object r = compiledScript.eval(bindings);

            if (r instanceof GremlinPipeline) {
                Iterator<Object> itr = ((GremlinPipeline) r).iterator();

                res.fillElements(itr, limit);
            }
            else {
                res.convertAndAdd(r);
            }
        } catch (ScriptException e) {
            res.error = Utils.toString(e);
        }

        res.executionTime = System.currentTimeMillis() - startTime;

        return res;
    }

    public Object vertex(TitanGraph g, HttpServletRequest req) {
        long id = getLongParam("vId");

        TitanVertex vertex = g.getVertex(id);

        return vertex == null ? null : new FullVertexJson(vertex);
    }

    public List<String> edgeLabels(TitanGraph g, HttpServletRequest req) {
        return Utils.getEdgeLabels(g);
    }

    public String openGraph(TitanGraph g, HttpServletRequest req) {
        return "true";
    }

    public Map<String, EdgeListResult> vertexEdgesAllLabels(TitanGraph g, HttpServletRequest req) {
        long id = getLongParam("vId");

        TitanVertex vertex = g.getVertex(id);

        if (vertex == null)
            return null;

        int limit = getIntParam("limit", 10);

        boolean inVertex = "in".equals(req.getParameter("dir"));

        Map<String, EdgeListResult> res = new HashMap<>();

        for (String label : Utils.getEdgeLabels(g)) {
            Iterable<Edge> edges = vertex.getEdges(inVertex ? Direction.IN : Direction.OUT, label);

            EdgeListResult edgeList = new EdgeListResult();

            for (Edge edge : edges) {
                if (edgeList.edges.size() >= limit) {
                    edgeList.hasNext = true;

                    break;
                }

                HalfEdgeJson edgeJson = new HalfEdgeJson(vertex, (TitanEdge) edge);

                edgeList.edges.add(edgeJson);
            }

            if (!edgeList.edges.isEmpty())
                res.put(label, edgeList);
        }

        return res;
    }

    public List<TitanTableDescr> tableList(HttpServletRequest req) throws IOException {
        GraphManager gm = GraphManager.getInstance();
        return gm.loadTables().stream().map(tableName -> new TitanTableDescr(tableName, gm.isGraphOpen(tableName)))
                .collect(Collectors.toList());
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
                elements.add(Utils.format((TitanVertex) o));
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
}
