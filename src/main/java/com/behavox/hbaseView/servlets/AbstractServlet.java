package com.behavox.hbaseView.servlets;

import com.behavox.hbaseView.titan.viewModel.Config;
import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
public abstract class AbstractServlet extends HttpServlet {

    private final ThreadLocal<HttpServletRequest> request = new ThreadLocal<>();

    protected abstract void processGet(HttpServletRequest req, HttpServletResponse resp) throws IOException;

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        request.set(req);

        processGet(req, resp);

        request.remove();
    }

    @NotNull
    protected HttpServletRequest getRequest() {
        return request.get();
    }

    protected Config getConfig() {
        return (Config) getRequest().getAttribute("cfg");
    }

    protected Long getLongParam(String name) {
        String paramStr = getRequest().getParameter(name);

        if (paramStr == null)
            return null;

        return Long.parseLong(paramStr);
    }

    protected int getIntParam(String name) {
        return Integer.parseInt(getRequest().getParameter(name));
    }

    protected int getIntParam(String name, int defVal) {
        String s = getRequest().getParameter(name);

        if (s == null)
            return defVal;

        return Integer.parseInt(s);
    }

}
