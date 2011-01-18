package uk.ac.ebi.interpro.scan.business.sequence;

import org.apache.commons.lang.SerializationUtils;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.ProteinXref;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFilePrecalculatedProteinLookup implements PrecalculatedProteinLookup {

    public ZipFilePrecalculatedProteinLookup(File dataFile) throws IOException {
        if (dataFile.exists()) {
            serializedProteinsZipFile = new ZipFile(dataFile);
        }
    }

    private ZipFile serializedProteinsZipFile;

    /**
     * If the protein required has been Serialized and stored in a zip file,
     * it can be looked up by MD5 and returned directly.
     *
     * @param protein being the Protein to search for in the precalculated file.
     * @return null if the protein does not exist in the zip file, null if the zip file does not
     *         exist or the Deserialized Protein if it is available.
     */
    @Override
    public Protein getPrecalculated(Protein protein) {
        if (serializedProteinsZipFile == null) {
            return null;
        }
        String md5 = protein.getMd5();
        ZipEntry zipEntry = serializedProteinsZipFile.getEntry(md5);
        if (zipEntry == null) {
            return null;
        }
        Protein precalculated;
        try {
            precalculated = (Protein) SerializationUtils.deserialize(serializedProteinsZipFile.getInputStream(zipEntry));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to deserialize protein for MD5:" + md5 + " from " + serializedProteinsZipFile.getName());
        }

        // Add any new Cross-references that are available for the Protein.
        for (ProteinXref xref : protein.getCrossReferences()) {
            // Cross references are stored as a HashSet, so can safely attempt to add new cross references here.
            precalculated.addCrossReference(
                    new ProteinXref(xref.getDatabaseName(), xref.getIdentifier(), xref.getName())
            );
        }
//        for (Match match:precalculated.getMatches()) protein.addMatch(match);
        return precalculated;
    }
}
