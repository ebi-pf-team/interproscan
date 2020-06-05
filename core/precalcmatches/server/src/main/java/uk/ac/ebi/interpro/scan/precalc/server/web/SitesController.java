package uk.ac.ebi.interpro.scan.precalc.server.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.KVSequenceEntry;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.KVSequenceEntryXML;
import uk.ac.ebi.interpro.scan.precalc.server.service.MatchesService;

import uk.ac.ebi.interpro.scan.util.Utilities;

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
@RequestMapping("/sites")
public class SitesController {

    private static final Logger LOGGER = LogManager.getLogger(SitesController.class.getName());

    private MatchesService matchService;

    private Jaxb2Marshaller berkeleyJaxb2;

    @Autowired
    public void setMatchService(MatchesService matchService) {
        this.matchService = matchService;
        this.matchService.setName("sites");
    }

    @Autowired
    public void setBerkeleyJaxb2(Jaxb2Marshaller berkeleyJaxb2) {
        this.berkeleyJaxb2 = berkeleyJaxb2;
    }

    @RequestMapping
    public void getSites(HttpServletResponse response,
                           @RequestParam(value = "md5", required = true) String[] md5Array) {
        long startGetSiteMatches = System.currentTimeMillis();
        List<KVSequenceEntry> sites = matchService.getSites(Arrays.asList(md5Array));
        long timeToGetSiteMatches = System.currentTimeMillis() - startGetSiteMatches;
        //matchService.countMatchesRequests(md5Array.length, timeToGetMatches);
        //Integer timeProcessingPartitionSeconds = (int) timeProcessingPartition / 1000;
        //TODO, this is usefull to check the performance of site lookup
        //System.out.println(Utilities.getTimeNow() + " Took  " + timeToGetSiteMatches + " millis to get  site matches  for  " + md5Array.length  + " md5s");
        //System.out.println(Utilities.getTimeNow() + "sites count: " + sites.size());

        KVSequenceEntryXML siteXML = new KVSequenceEntryXML(sites);
        response.setContentType("application/xml");
        Writer out = null;
        try {
            out = response.getWriter();
            berkeleyJaxb2.marshal(siteXML, new StreamResult(out));
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
