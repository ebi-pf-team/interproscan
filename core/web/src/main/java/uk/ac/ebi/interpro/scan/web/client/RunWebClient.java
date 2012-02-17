package uk.ac.ebi.interpro.scan.web.client;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.File;

/**
 * Main class to run HTMLUnit web client. Saves request result as HTML file.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class RunWebClient {
    public static void main(String[] args) throws Exception {
        final WebClient webClient = new WebClient();
        webClient.addRequestHeader("Accept", "*/*");
        final HtmlPage htmlPage = webClient.getPage("webpage");

        File outputFile = new File("protein_view.html");
        if (outputFile.exists()) {
            outputFile.delete();
        }
        htmlPage.save(outputFile);

        webClient.closeAllWindows();
    }

}
