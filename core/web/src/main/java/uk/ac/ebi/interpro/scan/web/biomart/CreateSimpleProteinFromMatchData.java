package uk.ac.ebi.interpro.scan.web.biomart;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.*;

import org.apache.log4j.Logger;
import org.springframework.core.io.InputStreamResource;
import uk.ac.ebi.interpro.scan.web.ProteinViewController;

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

    private final AnalyseMatchDataResult analyser;

    public CreateSimpleProteinFromMatchData(AnalyseMatchDataResult analyser) {
        this.analyser = analyser;
    }

    public ProteinViewController.SimpleProtein queryByAccession(String ac) throws IOException {
        return retrieveMatches(createMatchesUrl(ac, true));
    }

    public ProteinViewController.SimpleProtein queryByMd5(String md5) throws IOException {
        return retrieveMatches(createMatchesUrl(md5, false));
    }

    private ProteinViewController.SimpleProtein retrieveMatches(String url) throws IOException {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod();
        method.setURI(new URI(url, false));
        try {
            int statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                LOGGER.error("Error getting " + url);
                throw new HttpException(method.getStatusLine().toString());
            }
            return this.analyser.parseMatchDataOutput(new InputStreamResource(method.getResponseBodyAsStream()));
        }
        finally {
            method.releaseConnection();
        }
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
        private String createStructuralMatchUrl(String proteinAc, boolean isProteinAc) {
            // TODO: Use MD5 as filter if not proteinAc
            StringBuilder url = new StringBuilder()
                    .append(STRUCTURAL_MATCH_DATA_URL)
                    .append(proteinAc) // PROTEIN ACCESSION
                    .append(EXTENSION);
            return url.toString();
        }


}
