package uk.ac.ebi.interpro.scan.precalc.server.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.ac.ebi.interpro.scan.precalc.server.service.MatchesService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * Created with IntelliJ IDEA.
 * User: craigm
 * Date: 14/03/13
 * Time: 11:37
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/version")
public class VersionController {

    private static final Logger LOGGER = LogManager.getLogger(VersionController.class.getName());

    // used to indicate to the client that the response is valid
    private static final String SERVER_VERSION_PREFIX = "SERVER:";

    @Autowired
    private MatchesService matchService;

    @RequestMapping
    public void getVersion(HttpServletResponse response) {
        response.setContentType("text/tab-separated-values");
        Writer out = null;
        try {
            out = response.getWriter();
            out.write(SERVER_VERSION_PREFIX + matchService.getServerVersion());

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
