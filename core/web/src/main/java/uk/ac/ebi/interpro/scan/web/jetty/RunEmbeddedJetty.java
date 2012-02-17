package uk.ac.ebi.interpro.scan.web.jetty;


import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import javax.servlet.ServletContext;

/**
 * Main class to run embedded Jetty server.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class RunEmbeddedJetty {

    public static void main(String[] args) throws Exception {
        runSpringConfig();
    }

    private static void runSpringConfig() throws Exception {
        ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext("classpath*:embedded-jetty-context.xml");
        ctx.registerShutdownHook();
        Server jettyServer = (Server) ctx.getBean("jettyServer");

        ServletContext servletContext = null;

        for (Handler handler : jettyServer.getHandlers()) {
            if (handler instanceof Context) {
                Context context = (Context) handler;
                servletContext = context.getServletContext();
            }
        }

        XmlWebApplicationContext wctx = new XmlWebApplicationContext();
        wctx.setParent(ctx);
        wctx.setConfigLocation("");
        wctx.setServletContext(servletContext);
        wctx.refresh();

        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, wctx);

        ctx.start();

        jettyServer.start();
    }
}