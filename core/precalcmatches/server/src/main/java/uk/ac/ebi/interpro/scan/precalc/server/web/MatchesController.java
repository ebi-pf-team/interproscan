package uk.ac.ebi.interpro.scan.precalc.server.web;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatchXML;
import uk.ac.ebi.interpro.scan.precalc.server.service.MatchesService;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
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

    private MatchesService matchesService;

    private Jaxb2Marshaller marshaller;

    @Autowired
    public MatchesController(MatchesService matchesService, Jaxb2Marshaller marshaller) {
        Assert.notNull(matchesService, "'matchesService' must not be null");
        Assert.notNull(marshaller, "'unmarshaller' must not be null");
        this.matchesService = matchesService;
        this.marshaller = marshaller;
    }

    @RequestMapping
    public void getMatches(HttpServletResponse response,
                           @RequestParam(value = "md5", required = true) String[] md5Array) {
        List<BerkeleyMatch> matches = matchesService.getMatches(Arrays.asList(md5Array));
        BerkeleyMatchXML matchXML = new BerkeleyMatchXML(matches);
        response.setContentType("application/xml");
        try {
            marshaller.marshal(matchXML, new StreamResult(response.getWriter()));
        } catch (IOException e) {
            LOGGER.error("IOException thrown when attempting to output 'BerkeleyMatchXML' in response to query: ");
        }
    }
}
