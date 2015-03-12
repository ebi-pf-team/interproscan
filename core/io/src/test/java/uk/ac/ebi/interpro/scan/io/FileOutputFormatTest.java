package uk.ac.ebi.interpro.scan.io;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.interpro.scan.io.tmhmm.TMHMMPredictionTableParser;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for {@link FileOutputFormat}.
 *
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class FileOutputFormatTest {

    @Test
    public void testStringToFileOutputFormat() {
        assertEquals("Default format expected!", FileOutputFormat.TSV, FileOutputFormat.stringToFileOutputFormat("dsfsd"));
        assertEquals("TSV format expected!", FileOutputFormat.TSV, FileOutputFormat.stringToFileOutputFormat("tsv"));
        assertEquals("XML format expected!", FileOutputFormat.XML, FileOutputFormat.stringToFileOutputFormat("xml"));
        assertEquals("XML slim format expected!", FileOutputFormat.XML_SLIM, FileOutputFormat.stringToFileOutputFormat("xml-slim"));
        assertEquals("GFF3 format expected!", FileOutputFormat.GFF3, FileOutputFormat.stringToFileOutputFormat("gff3"));
        assertEquals("GFF3 partial format expected!", FileOutputFormat.GFF3_PARTIAL, FileOutputFormat.stringToFileOutputFormat("gff3-partial"));
        assertEquals("HTML format expected!", FileOutputFormat.HTML, FileOutputFormat.stringToFileOutputFormat("html"));
        assertEquals("SVG format expected!", FileOutputFormat.SVG, FileOutputFormat.stringToFileOutputFormat("svg"));
        assertEquals("RAW format expected!", FileOutputFormat.RAW, FileOutputFormat.stringToFileOutputFormat("raw"));
    }

    @Test
    public void testGetFileExtension() {
        assertEquals("TSV format expected!", "tsv", FileOutputFormat.TSV.getFileExtension());
        assertEquals("XML format expected!", "xml", FileOutputFormat.XML.getFileExtension());
        assertEquals("XML slim format expected!", "xml-slim", FileOutputFormat.XML_SLIM.getFileExtension());
        assertEquals("GFF3 format expected!", "gff3", FileOutputFormat.GFF3.getFileExtension());
        assertEquals("GFF3 partial format expected!", "gff3-partial", FileOutputFormat.GFF3_PARTIAL.getFileExtension());
        assertEquals("HTML format expected!", "html", FileOutputFormat.HTML.getFileExtension());
        assertEquals("SVG format expected!", "svg", FileOutputFormat.SVG.getFileExtension());
        assertEquals("RAW format expected!", "raw", FileOutputFormat.RAW.getFileExtension());
    }
}
