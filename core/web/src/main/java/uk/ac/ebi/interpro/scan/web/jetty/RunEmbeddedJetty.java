package uk.ac.ebi.interpro.scan.web.jetty;


import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
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
            if (handler instanceof ContextHandler.Context) {
                ContextHandler.Context context = (ContextHandler.Context) handler;
                servletContext = context.getContext("/");
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
