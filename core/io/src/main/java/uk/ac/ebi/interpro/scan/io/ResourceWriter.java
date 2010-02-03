package uk.ac.ebi.interpro.scan.io;

import org.springframework.core.io.Resource;

import java.util.Collection;
import java.io.IOException;

/**
 * File writer.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public interface ResourceWriter<T> {

    public void write(Resource resource, Collection<T> records) throws IOException;

    public void write(Resource resource, Collection<T> records, boolean append) throws IOException;

}