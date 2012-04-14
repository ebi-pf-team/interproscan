package uk.ac.ebi.interpro.scan.search;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests EBI Search web service client.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public class TextSearchTest {

    @Test
    public void getServiceEndPoint() {

        // Default (oddly, this is null)
        TextSearch client = new TextSearch();
        assertEquals("Service end points should match", null, client.getServiceEndPoint());

        // Custom
        String endPoint = "http://www.ebi.ac.uk/ebisearch/service.ebi";
        client = new TextSearch(endPoint);
        assertEquals("Service end points should match", endPoint, client.getServiceEndPoint());

    }

}
