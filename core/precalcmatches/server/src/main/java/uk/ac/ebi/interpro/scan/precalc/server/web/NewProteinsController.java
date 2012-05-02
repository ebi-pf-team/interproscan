package uk.ac.ebi.interpro.scan.precalc.server.web;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ebi.interpro.scan.precalc.server.service.MatchesService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

/**
 * Controller for simple service that kicks back the MD5 checksums of any sequences
 * that have already been run through the analysis pipeline and therefore do not require
 * reanalysis.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Controller
@RequestMapping("/isPrecalculated")
public class NewProteinsController {

    private MatchesService matchService;

    private static final Logger LOGGER = Logger.getLogger(NewProteinsController.class.getName());

    @Autowired
    public void setMatchService(MatchesService matchService) {
        this.matchService = matchService;
    }

    @RequestMapping
    public void getProteinsToAnalyse(HttpServletResponse response,
                                     @RequestParam(value = "md5", required = true) String[] md5Array) {
        List<String> precalculatedMD5s = matchService.isPrecalculated(Arrays.asList(md5Array));
        response.setContentType("text/tab-separated-values");
        Writer out = null;
        try {
            out = response.getWriter();
            for (String md5 : precalculatedMD5s) {
                out.write(md5);
                out.write('\n');
            }

        } catch (IOException e) {
            LOGGER.error("IOException thrown when attempting to output precalculated MD5s.");
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
