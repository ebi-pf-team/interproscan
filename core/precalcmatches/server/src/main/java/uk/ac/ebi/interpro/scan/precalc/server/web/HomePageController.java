package uk.ac.ebi.interpro.scan.precalc.server.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Phil Jones
 *         Date: 01/06/12
 */
@Controller
public class HomePageController {

    /**
     * Displays a human-readable home page to reassure people that the service is up.
     *
     * @return a human-readable home page to reassure people that the service is up.
     */
    @RequestMapping("/")
    public void displayHomePage(HttpServletResponse response) throws IOException {
        final PrintWriter writer = response.getWriter();
        writer.write("<!DOCTYPE html>\n<html>\n<head>\n<title>InterProScan 5 Pre-calculated Match Lookup Service</title>\n</head>\n");
        writer.write("<body>\n<h3>InterProScan 5 Pre-calculated Match Lookup Service</h3>");
        writer.write("<p>This is the landing page of the InterProScan 5 Pre-calculated Match Lookup Service.</p>");
        writer.write("<p>Please note that this is a REST web service that has been designed specifically for use");
        writer.write(" by InterProScan 5 installations.</p>\n");
        writer.write("</body>\n</html>\n");
        writer.close();
    }
}
