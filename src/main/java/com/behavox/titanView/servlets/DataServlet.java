package com.behavox.titanView.servlets;

import com.behavox.titanView.GraphManager;
import com.behavox.titanView.Utils;
import com.behavox.titanView.json.FullVertexJson;
import com.behavox.titanView.json.ShortEdgeJson;
import com.behavox.titanView.json.ShortVertexJson;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.thinkaurelius.titan.core.TitanEdge;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

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
public class DataServlet extends AbstractServlet {

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
            if (graph != null) {
                graph.rollback();
                graph = null;
            }

            throw Throwables.propagate(t);
        } finally {
            if (graph != null)
                graph.commit();
        }
    }

    public Object vertexList(TitanGraph g, HttpServletRequest req) {
        int limit = getIntParam("limit", 50);

        VertexListResult res = new VertexListResult();

        for (Iterator<Vertex> itr = g.getVertices().iterator(); itr.hasNext(); ) {
            TitanVertex vertex = (TitanVertex) itr.next();

            res.vertexes.add(Utils.format(vertex));

            if (res.vertexes.size() >= limit) {
                res.hasNext = itr.hasNext();

                break;
            }
        }

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

                ShortEdgeJson edgeJson = new ShortEdgeJson(vertex, (TitanEdge) edge);

                edgeList.edges.add(edgeJson);
            }

            if (!edgeList.edges.isEmpty())
                res.put(label, edgeList);
        }

        return res;
    }

    public List<String> tableList(HttpServletRequest req) throws IOException {
        return GraphManager.getInstance().loadTables();
    }

    private static class VertexListResult {
        public final List<ShortVertexJson> vertexes = new ArrayList<>();

        public boolean hasNext;
    }

    private static class EdgeListResult {
        public final List<ShortEdgeJson> edges = new ArrayList<>();

        public boolean hasNext;
    }
}
