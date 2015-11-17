package com.behavox.hbaseView.titan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
public class ConfigReloaderServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ConfigReloaderServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ConfigManager.getInstance().reload();

        log.info("UI config was reloaded");
    }
}
