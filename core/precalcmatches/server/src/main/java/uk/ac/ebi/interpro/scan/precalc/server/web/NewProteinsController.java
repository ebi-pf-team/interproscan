package uk.ac.ebi.interpro.scan.precalc.server.web;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ebi.interpro.scan.precalc.server.service.MatchesService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Controller
@RequestMapping("/notCalculated")
public class NewProteinsController {

    private MatchesService matchesService;

    private static final Logger LOGGER = Logger.getLogger(NewProteinsController.class.getName());

    @Autowired
    public NewProteinsController(MatchesService matchesService) {
        Assert.notNull(matchesService, "'matchesService' must not be null");
        this.matchesService = matchesService;
    }

    @RequestMapping
    public void getProteinsToAnalyse(HttpServletResponse response,
                                     @RequestParam(value = "md5", required = true) String[] md5Array) {
        List<String> md5sToCalculate = matchesService.notPrecalculated(Arrays.asList(md5Array));
        response.setContentType("text/tab-separated-values");
        Writer out = null;
        try {
            out = response.getWriter();
            for (String md5 : md5sToCalculate) {
                out.write(md5);
                out.write('\n');
            }

        } catch (IOException e) {
            LOGGER.error("IOException thrown when attempting to output MD5s to run through analysis.");
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LOGGER.error("IOException thrown when attempting to close the response Writer.");
                }
            }
        }
    }
}
