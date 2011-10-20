package uk.ac.ebi.interpro.scan.web.io;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.*;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.core.io.InputStreamResource;
import uk.ac.ebi.interpro.scan.web.model.SimpleProtein;
import uk.ac.ebi.interpro.scan.web.model.SimpleStructuralMatch;

/**
* TODO: Add class description
*
* @author  Matthew Fraser
* @author  Antony Quinn
* @version $Id$
*/
public class CreateSimpleProteinFromMatchData {

    private static final Logger LOGGER = Logger.getLogger(CreateSimpleProteinFromMatchData.class);

    // TODO: Make this configurable
    private static final String MATCH_DATA_URL = "http://wwwdev.ebi.ac.uk/interpro/match/";
    private static final String STRUCTURAL_MATCH_DATA_URL = "http://wwwdev.ebi.ac.uk/interpro/structure/";
    private static final String EXTENSION = ".tsv";

    private final AnalyseMatchDataResult matchAnalyser;
    private final AnalyseStructuralMatchDataResult structuralMatchAnalyser;

    public CreateSimpleProteinFromMatchData(AnalyseMatchDataResult matchAnalyser,
                                            AnalyseStructuralMatchDataResult structuralMatchAnalyser) {
        this.matchAnalyser = matchAnalyser;
        this.structuralMatchAnalyser = structuralMatchAnalyser;
    }

    public SimpleProtein queryByAccession(String ac) throws IOException {
        return retrieveMatches(createMatchesUrl(ac, true), createStructuralMatchesUrl(ac, true));
    }

    public SimpleProtein queryByMd5(String md5) throws IOException {
        return retrieveMatches(createMatchesUrl(md5, false), createStructuralMatchesUrl(md5, false));
    }

    private SimpleProtein retrieveMatches(String matchesUrl, String structuralMatchesUrl) throws IOException {
        SimpleProtein protein = null;
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod();
        method.setURI(new URI(matchesUrl, false));

        try {
            // First query for match data
            int statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                LOGGER.error("Error getting " + matchesUrl);
                throw new HttpException(method.getStatusLine().toString());
            }
            protein =  this.matchAnalyser.parseMatchDataOutput(new InputStreamResource(method.getResponseBodyAsStream()));

            // Now query for structural match data
            method.setURI(new URI(structuralMatchesUrl, false));
            statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                LOGGER.error("Error getting " + structuralMatchesUrl);
                throw new HttpException(method.getStatusLine().toString());
            }

            // Add structural matches
            List<SimpleStructuralMatch> structuralMatches =
                    structuralMatchAnalyser.parseStructuralMatchDataOutput(new InputStreamResource(method.getResponseBodyAsStream()));
            for (SimpleStructuralMatch m : structuralMatches) {
                protein.getStructuralMatches().add(m);
            }
            
        }
        finally {
            method.releaseConnection();
        }

        return protein;
    }

    /**
     * Create URL required to call REST web service to get protein match information.
     *
     * Example:
     * http://wwwdev.ebi.ac.uk/interpro/match/P38398.tsv
     *
     * @param proteinAc
     * @param isProteinAc
     * @return
     */
    private String createMatchesUrl(String proteinAc, boolean isProteinAc) {
        // TODO: Use MD5 as filter if not proteinAc
        StringBuilder url = new StringBuilder()
                .append(MATCH_DATA_URL)
                .append(proteinAc) // PROTEIN ACCESSION
                .append(EXTENSION);
        return url.toString();
    }

    /**
         * Create URL required to call REST web service to get protein structural match information.
         *
         * Example:
         * http://wwwdev.ebi.ac.uk/interpro/structure/P38398.tsv
         *
         * @param proteinAc
         * @param isProteinAc
         * @return
         */
        private String createStructuralMatchesUrl(String proteinAc, boolean isProteinAc) {
            // TODO: Use MD5 as filter if not proteinAc
            StringBuilder url = new StringBuilder()
                    .append(STRUCTURAL_MATCH_DATA_URL)
                    .append(proteinAc) // PROTEIN ACCESSION
                    .append(EXTENSION);
            return url.toString();
        }


}
