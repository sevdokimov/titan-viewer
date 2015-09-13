package com.behavox.titanView;

import com.thinkaurelius.titan.core.TitanGraph;

import javax.servlet.*;
import java.io.IOException;

public class TransactionFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        TitanGraph graph = GraphManager.getInstance().getGraph();

        servletRequest.setAttribute("g", graph);

        boolean success = false;

        try {
            filterChain.doFilter(servletRequest, servletResponse);

            success = true;

            graph.commit();
        } finally {
            if (!success) {
                graph.rollback();
            }
        }


    }

    @Override
    public void destroy() {

    }
}
