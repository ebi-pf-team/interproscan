package uk.ac.ebi.interpro.scan.io.unmarshal.xml.interpro;

import uk.ac.ebi.interpro.scan.io.serialization.ObjectSerializerDeserializer;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

/**
 * Simple main class to allow an InterPro XML file to be
 * parsed and to provide the mapping file from member database
 * signatures to InterPro entries and GO mappings.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class BuildNewInterProGOMappingsMain {

    public static void main(String[] args)
            throws IOException, XMLStreamException {

        if (args.length < 2){
            printUsageAndExit();
        }

        final String interproXmlFile = args[0];
        final String outputFile = args[1];

        final InterProXMLUnmarshaller unmarshaller = new InterProXMLUnmarshaller();
        final ObjectSerializerDeserializer<Map<SignatureLibrary, SignatureLibraryIntegratedMethods>> serializerDeserializer = new ObjectSerializerDeserializer<Map<SignatureLibrary, SignatureLibraryIntegratedMethods>>();
        serializerDeserializer.setCompressedUsingGzip(outputFile.endsWith(".gz"));
        serializerDeserializer.setOverWrite(true);
        serializerDeserializer.setFileName(outputFile);
        BufferedInputStream bis = null;
        try {
            if (interproXmlFile.endsWith("gz")) {
                bis = new BufferedInputStream(new GZIPInputStream(new FileInputStream(interproXmlFile)));
            }
            else if (interproXmlFile.endsWith("zip")) {
                bis = new BufferedInputStream(new ZipInputStream(new FileInputStream(interproXmlFile)));
            }
            else {
                bis = new BufferedInputStream(new FileInputStream(interproXmlFile));
            }

            Map<SignatureLibrary, SignatureLibraryIntegratedMethods> unmarshalledData = unmarshaller.unmarshal(bis);
            System.out.println("Parsed " + interproXmlFile);
            // Serialize out un-marshaled data
            serializerDeserializer.serialize(unmarshalledData);
            System.out.println("Serialized out to " + outputFile);
        }
        finally {
            if (bis != null) {
                bis.close();
            }
        }
    }

    private static void printUsageAndExit() {
        System.out.println("This class requires two arguments:");
        System.out.println("1. The path / filename of the interpro XML file.");
        System.out.println("2. The path / filename of the compressed mapping file\n(must be gzipped and end in the extension .gz)");
        System.out.println();
        System.out.println("NOTES:");
        System.out.println("The input file (InterPro XML file) may be uncompressed, gzipped or zipped, so long as it uses an appropriate file extension (.xml, .gz or .zip)");
        System.out.println();
        System.out.println("The compressed mapping file should be named / versioned using the pattern 'interproEntryGoMapping-30.0.gz'\nusing the correct version number.\nAt present it is normally located in core/jms-implementation/support-mini-x86-32/");
        System.out.println("You can then configure the path / name of the mapping file used in the interpro.properties file that InterProScan is run with.");
        System.exit(1);
    }
}
