package uk.ac.ebi.interpro.scan.precalc.server.web;

import javax.servlet.*;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 03/05/12
 */
public class HealthFilter implements Filter {

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setAttribute("health", true);
        chain.doFilter(request, response);
    }

    public void init(FilterConfig filterconfig)
            throws ServletException {
    }

    public void destroy() {
    }
}
