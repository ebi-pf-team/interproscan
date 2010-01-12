package uk.ac.ebi.interpro.scan.io.sequence;

import uk.ac.ebi.interpro.scan.model.Protein;

import java.util.Collection;
import java.io.IOException;

import org.springframework.core.io.Resource;

/**
 * Sequence writer.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public interface SequenceWriter {

    public void write(Collection<Protein> proteins, Resource resource) throws IOException;

    public int getWidth();

    public boolean isIdNullable();

    public boolean isAddXrefs();

}
