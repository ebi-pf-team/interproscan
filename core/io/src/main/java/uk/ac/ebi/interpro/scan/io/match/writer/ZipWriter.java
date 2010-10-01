package uk.ac.ebi.interpro.scan.io.match.writer;

import org.apache.commons.lang.SerializationUtils;
import uk.ac.ebi.interpro.scan.io.unmarshal.xml.interpro.SignatureLibraryIntegratedMethods;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: dbinns
 * Date: 21-Jun-2010
 * Time: 17:09:13
 * To change this template use File | Settings | File Templates.
 */
public class ZipWriter implements ProteinWriter {

    ZipOutputStream os;

    //TODO - This ZipWriter serializes out the Protein object.  This currently gives no opportunity to include mappings
    //TODO to InterPro or GO - ....
    private boolean mapToInterProEntries;
    private boolean mapToGO;
    private Map<SignatureLibrary, SignatureLibraryIntegratedMethods> interProGoMapping;

    public ZipWriter(File file) throws FileNotFoundException {
        os = new ZipOutputStream(new FileOutputStream(file));
    }

    @Override
    public void write(Protein protein) throws IOException {
        os.putNextEntry(new ZipEntry(protein.getMd5()));
        os.write(SerializationUtils.serialize(protein));
        os.closeEntry();
    }

    @Override
    public void setMapToInterProEntries(boolean mapToInterProEntries) {
        this.mapToInterProEntries = mapToInterProEntries;
    }

    @Override
    public void setMapToGo(boolean mapToGO) {
        this.mapToGO = mapToGO;
    }

    @Override
    public void setInterProGoMapping(Map<SignatureLibrary, SignatureLibraryIntegratedMethods> interProGoMapping) {
        this.interProGoMapping = interProGoMapping;
    }

    @Override
    public void close() throws IOException {
        os.close();
    }
}
