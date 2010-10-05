package uk.ac.ebi.interpro.scan.business.sequence;

import org.apache.commons.lang.SerializationUtils;
import uk.ac.ebi.interpro.scan.model.Protein;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFilePrecalculatedProteinLookup implements PrecalculatedProteinLookup {

    public ZipFilePrecalculatedProteinLookup(File dataFile) throws IOException {
        if (dataFile.exists()) f = new ZipFile(dataFile);
    }

    private ZipFile f;

    @Override
    public Protein getPrecalculated(Protein protein) {
        if (f == null) return null;
        String md5 = protein.getMd5();
        ZipEntry ze = f.getEntry(md5);
        if (ze == null) return null;
        Protein precalculated;
        try {
            precalculated = (Protein) SerializationUtils.deserialize(f.getInputStream(ze));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to deserialize protein for MD5:" + md5);
        }
        //for (Xref xref : protein.getCrossReferences()) precalculated.addCrossReferences(xref.getIdentifier());
        //for (Match match:precalculated.getMatches()) protein.addMatch(match);
        return precalculated;
    }
}
