package uk.ac.ebi.interpro.scan.condensed.server.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.ac.ebi.interpro.scan.condensed.server.web.service.CondensedService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Phil Jones
 */
@Controller
public class CondensedHtmlController {

    @Autowired
    private CondensedService condensedService;

    @RequestMapping("/html/{id}")
    public void getHtml(final HttpServletResponse response, @PathVariable final String id) throws IOException {
        final PrintWriter out = response.getWriter();
        out.write(condensedService.getCondensedHtml(id));
    }

}
