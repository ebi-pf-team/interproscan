package uk.ac.ebi.interpro.scan.web.biomart;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.*;

import org.apache.log4j.Logger;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.ResourceReader;
import uk.ac.ebi.interpro.scan.web.ProteinViewController;

/**
* TODO: Add class description
*
* @author  Matthew Fraser
* @author  Antony Quinn
* @version $Id$
*/
public class CreateSimpleProteinFromBioMartQuery {

    private static final Logger LOGGER = Logger.getLogger(CreateSimpleProteinFromBioMartQuery.class);

    // TODO: Make this configurable
    private static final String BIOMART_URL = "http://www.ebi.ac.uk/interpro/biomart/martservice?query=";

    private final AnalyseBioMartQueryResult analyser;

    public CreateSimpleProteinFromBioMartQuery(AnalyseBioMartQueryResult analyser) {
        this.analyser = analyser;
    }

    public ProteinViewController.SimpleProtein queryByAccession(String ac) throws IOException {
        return retrieve(createUrl(ac, true));
    }

    public ProteinViewController.SimpleProtein queryByMd5(String md5) throws IOException {
        return retrieve(createUrl(md5, false));
    }

    private ProteinViewController.SimpleProtein retrieve(String url) throws IOException {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod();
        method.setURI(new URI(url, false));
        try {
            int statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                LOGGER.error("Error getting " + url);
                throw new HttpException(method.getStatusLine().toString());
            }
            return this.analyser.parseBioMartQueryOutput(new InputStreamResource(method.getResponseBodyAsStream()));
        }
        finally {
            method.releaseConnection();
        }
    }

    // Example query:
    // http://www.ebi.ac.uk/interpro/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName = "default" formatter = "TSV" header = "0" uniqueRows = "0" count = "" datasetConfigVersion = "0.6" ><Dataset name = "protein" interface = "default" ><Filter name = "protein_accession" value = "P38398"/><Attribute name = "protein_accession" /><Attribute name = "protein_name" /><Attribute name = "md5" /><Attribute name = "method_id" /><Attribute name = "method_name" /><Attribute name = "method_database_name" /><Attribute name = "pos_from" /><Attribute name = "pos_to" /><Attribute name = "match_score" /><Attribute name = "entry_ac" /><Attribute name = "entry_short_name" /><Attribute name = "entry_name" /><Attribute name = "entry_type" /></Dataset></Query>
    private String createUrl(String proteinAc, boolean isProteinAc) {
        // TODO: Use MD5 as filter if not proteinAc
        StringBuilder bioMartQuery = new StringBuilder()
                .append(BIOMART_URL)
                .append("<?xml version='1.0' encoding='UTF-8'?>")
                .append("<!DOCTYPE Query>")
                .append("<Query  virtualSchemaName = 'default' formatter = 'TSV' header = '0' uniqueRows = '0' count = '' datasetConfigVersion = '0.6' >")
                .append("<Dataset name = 'protein' interface = 'default' >")
                .append("<Filter name = 'protein_accession' value = '")
                .append(proteinAc) // PROTEIN ACCESSION
                .append("'/>")
                .append("<Attribute name = 'protein_accession' />")
                .append("<Attribute name = 'protein_name' />")
                .append("<Attribute name = 'md5' />")
                .append("<Attribute name = 'method_id' />")
                .append("<Attribute name = 'method_name' />")
                .append("<Attribute name = 'method_database_name' />")
                .append("<Attribute name = 'pos_from' />")
                .append("<Attribute name = 'pos_to' />")
                .append("<Attribute name = 'match_score' />")
                .append("<Attribute name = 'entry_ac' />")
                .append("<Attribute name = 'entry_short_name' />")
                .append("<Attribute name = 'entry_name' />")
                .append("<Attribute name = 'entry_type' />")
                .append("</Dataset>")
                .append("</Query>");
        return bioMartQuery.toString();
    }



}
