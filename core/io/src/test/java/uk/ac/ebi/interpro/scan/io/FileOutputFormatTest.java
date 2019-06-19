package uk.ac.ebi.interpro.scan.io;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import uk.ac.ebi.interpro.scan.io.tmhmm.TMHMMPredictionTableParser;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link FileOutputFormat}.
 *
 * @author Maxim Scheremetjew
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class FileOutputFormatTest {

    @Test
    public void testStringToFileOutputFormat() {
        assertEquals( FileOutputFormat.TSV, FileOutputFormat.stringToFileOutputFormat("dsfsd"), "Default format expected!");
        assertEquals( FileOutputFormat.TSV, FileOutputFormat.stringToFileOutputFormat("tsv"), "TSV format expected!");
        assertEquals( FileOutputFormat.XML, FileOutputFormat.stringToFileOutputFormat("xml"), "XML format expected!");
        assertEquals( FileOutputFormat.XML_SLIM, FileOutputFormat.stringToFileOutputFormat("xml-slim"), "XML slim format expected!");
        assertEquals( FileOutputFormat.GFF3, FileOutputFormat.stringToFileOutputFormat("gff3"), "GFF3 format expected!");
        assertEquals( FileOutputFormat.GFF3_PARTIAL, FileOutputFormat.stringToFileOutputFormat("gff3-partial"), "GFF3 partial format expected!");
        assertEquals( FileOutputFormat.HTML, FileOutputFormat.stringToFileOutputFormat("html"), "HTML format expected!");
        assertEquals( FileOutputFormat.SVG, FileOutputFormat.stringToFileOutputFormat("svg"), "SVG format expected!");
        assertEquals( FileOutputFormat.RAW, FileOutputFormat.stringToFileOutputFormat("raw"), "RAW format expected!");
    }

    @Test
    public void testGetFileExtension() {
        assertEquals( "tsv", FileOutputFormat.TSV.getFileExtension(), "TSV format expected!");
        assertEquals( "xml", FileOutputFormat.XML.getFileExtension(), "XML format expected!");
        assertEquals( "xml-slim", FileOutputFormat.XML_SLIM.getFileExtension(), "XML slim format expected!");
        assertEquals( "gff3", FileOutputFormat.GFF3.getFileExtension(), "GFF3 format expected!");
        assertEquals( "gff3-partial", FileOutputFormat.GFF3_PARTIAL.getFileExtension(), "GFF3 partial format expected!");
        assertEquals( "html", FileOutputFormat.HTML.getFileExtension(), "HTML format expected!");
        assertEquals( "svg", FileOutputFormat.SVG.getFileExtension(), "SVG format expected!");
        assertEquals( "raw", FileOutputFormat.RAW.getFileExtension(), "RAW format expected!");
    }
}
