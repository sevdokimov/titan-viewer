package com.behavox.titanView;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.StdErrLog;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.Properties;

public class TitanViewerMain {

    private static final Logger log = LoggerFactory.getLogger(TitanViewerMain.class);

    private static Server server;

    public static void startup(int port) throws Exception {
        Properties p = new Properties();

        p.setProperty("org.eclipse.jetty.LEVEL", "WARN");
        p.setProperty("org.eclipse.jetty.util.log.LEVEL", "OFF");
        p.setProperty("org.eclipse.jetty.util.component.LEVEL", "OFF");

        StdErrLog.setProperties(p);

        URL webXmlRes = TitanViewerMain.class.getClassLoader().getResource("webapp/WEB-INF/web.xml");

        String webXmlStr = webXmlRes.toString();

        Server srv = new Server(port);

        WebAppContext webAppCtx = new WebAppContext();
        webAppCtx.setContextPath("/");
        webAppCtx.setParentLoaderPriority(true);

        if (webXmlStr.startsWith("file:/")) {
            // Run from IDEA.
            File webApp = new File(webXmlRes.getFile()).getParentFile().getParentFile();
            File classes = webApp.getParentFile();
            File target = classes.getParentFile();
            File moduleRoot = target.getParentFile();

            webAppCtx.setResourceBase(moduleRoot.getAbsolutePath() + "/src/main/webapp");

            URL classesUrl = TitanViewerMain.class.getProtectionDomain().getCodeSource().getLocation();

            webAppCtx.getMetaData().setWebInfClassesDirs(Collections.singletonList(Resource.newResource(classesUrl)));

//            Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(srv);
//            classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration",
//                    "org.eclipse.jetty.plus.webapp.EnvConfiguration",
//                    "org.eclipse.jetty.plus.webapp.PlusConfiguration");
//            classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
//                    "org.eclipse.jetty.annotations.AnnotationConfiguration");

            webAppCtx.setConfigurations(new Configuration[]
                    {
                            new AnnotationConfiguration(),
                            new WebInfConfiguration(),
                            new WebXmlConfiguration(),
                            new MetaInfConfiguration(),
                            new FragmentConfiguration(),
                            new EnvConfiguration(),
                            new PlusConfiguration(),
                            new JettyWebXmlConfiguration()
                    });
        }
        else {
            // Run from JAR
            URL webAppUrl = new URL(webXmlStr.substring(0, webXmlStr.length() - "WEB-INF/web.xml".length()));

            webAppCtx.setBaseResource(Resource.newResource(webAppUrl));
        }

        srv.setHandler(webAppCtx);

        srv.start();

        server = srv;
    }

    public static void main(String[] args) throws Exception {
        log.info("");
        log.info("");
        log.info("");
        log.info("+-------------------------------------------------------+");
        log.info("|              Starting application                     |");
        log.info("+-------------------------------------------------------+");

        int port = Integer.parseInt(System.getProperty("port", "8060"));

        startup(port);

        System.out.println("http://localhost:" + port);
    }

}
