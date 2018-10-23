package uk.ac.ebi.interpro.scan.precalc.server.web;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.KVSequenceEntry;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.KVSequenceEntryXML;
import uk.ac.ebi.interpro.scan.precalc.server.service.MatchesService;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

/**
 * MVC Controller for the Precalculated matches
 * web service.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */

@Controller
@RequestMapping("/matches")
public class MatchesController {

    private static final Logger LOGGER = Logger.getLogger(MatchesController.class.getName());

    private MatchesService matchService;

    private Jaxb2Marshaller berkeleyJaxb2;

    @Autowired
    public void setMatchService(MatchesService matchService) {
        this.matchService = matchService;
    }

    @Autowired
    public void setBerkeleyJaxb2(Jaxb2Marshaller berkeleyJaxb2) {
        this.berkeleyJaxb2 = berkeleyJaxb2;
    }

    @RequestMapping
    public void getMatches(HttpServletResponse response,
                           @RequestParam(value = "md5", required = true) String[] md5Array) {
        List<KVSequenceEntry> matches = matchService.getMatches(Arrays.asList(md5Array));
        KVSequenceEntryXML matchXML = new KVSequenceEntryXML(matches);
        response.setContentType("application/xml");
        Writer out = null;
        try {
            out = response.getWriter();
            berkeleyJaxb2.marshal(matchXML, new StreamResult(out));
        } catch (IOException e) {
            LOGGER.error("IOException thrown when attempting to output 'BerkeleyMatchXML' in response to query: ");
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LOGGER.error("Unable to close the response Writer stream.");
                }
            }
        }
    }
}
