package uk.ac.ebi.interpro.scan.io.match.writer;

import uk.ac.ebi.interpro.scan.io.unmarshal.xml.interpro.SignatureLibraryIntegratedMethods;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

/**
 * Write proteins to output
 */
public interface ProteinWriter extends Closeable {

    void write(Protein protein) throws IOException;

    void setMapToInterProEntries(boolean mapToInterProEntries);

    void setMapToGo(boolean mapToGO);

    void setInterProGoMapping(Map<SignatureLibrary, SignatureLibraryIntegratedMethods> interProGoMapping);
}
