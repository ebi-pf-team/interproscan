package uk.ac.ebi.interpro.scan.web.biomart;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.*;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.ResourceReader;
import uk.ac.ebi.interpro.scan.web.ProteinViewController;

/**
* Created by IntelliJ IDEA.
* User: matthew
* Date: 23-Sep-2011
* Time: 13:04:19
* To change this template use File | Settings | File Templates.
*/
public class CreateSimpleProteinFromBioMartQuery {

    private static final Logger LOGGER = Logger.getLogger(CreateSimpleProteinFromBioMartQuery.class.getName());

    private String proteinAc;
    private AnalyseBioMartQueryResult analyser;

    public CreateSimpleProteinFromBioMartQuery(String proteinAc, AnalyseBioMartQueryResult analyser) {
        this.proteinAc = proteinAc;
        this.analyser = analyser;
    }

    public ProteinViewController.SimpleProtein sendBioMartQuery() {

        // Example query:
        // http://www.ebi.ac.uk/interpro/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName = "default" formatter = "TSV" header = "0" uniqueRows = "0" count = "" datasetConfigVersion = "0.6" ><Dataset name = "protein" interface = "default" ><Filter name = "protein_accession" value = "P38398"/><Attribute name = "protein_accession" /><Attribute name = "protein_name" /><Attribute name = "md5" /><Attribute name = "method_id" /><Attribute name = "method_name" /><Attribute name = "method_database_name" /><Attribute name = "pos_from" /><Attribute name = "pos_to" /><Attribute name = "match_score" /><Attribute name = "entry_ac" /><Attribute name = "entry_short_name" /><Attribute name = "entry_name" /><Attribute name = "entry_type" /></Dataset></Query>

        StringBuilder bioMartQuery = new StringBuilder()
                .append("http://www.ebi.ac.uk/interpro/biomart/martservice?query=")
                .append("<?xml version='1.0' encoding='UTF-8'?>")
                .append("<!DOCTYPE Query>")
                .append("<Query  virtualSchemaName = 'default' formatter = 'TSV' header = '0' uniqueRows = '0' count = '' datasetConfigVersion = '0.6' >")
                .append("<Dataset name = 'protein' interface = 'default' >")
                .append("<Filter name = 'protein_accession' value = '")
                .append(this.proteinAc) // PROTEIN ACCESSION
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

        ProteinViewController.SimpleProtein protein = null;
        try {
            // TODO: Remote server should return something meaningful if ac not recognised
            // TODO: Configure Spring MVC URL via Tomcat Context Parameter? Or at least allow override of default?
            String url = bioMartQuery.toString();

            protein = sendGetRequest(url);
        }
        catch (HttpException e) {
            // TODO: Log exception and show better message (use 404.xhtml?)
            LOGGER.warn("No protein information for " + this.proteinAc + " <!-- " + e.getMessage() + " -->");
        }
        catch (IOException e) {
            // TODO: Log exception and show better message (use 404.xhtml?)
            LOGGER.warn("No protein information for " + this.proteinAc + " <!-- " + e.getMessage() + " -->");
        }
        return protein;
    }

    private ProteinViewController.SimpleProtein sendGetRequest(String url) throws IOException {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod();
        method.setURI(new URI(url, false));
        try {
            int statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                throw new HttpException(method.getStatusLine().toString());
            }
            Resource resource = (Resource)method.getResponseBodyAsStream();
            analyser.setResource(resource);
            ResourceReader<BioMartQueryRecord> reader = new BioMartQueryResourceReader();
            analyser.setReader(reader);
            return analyser.parseBioMartQueryOutput();
        }
        finally {
            method.releaseConnection();
        }
    }



}
