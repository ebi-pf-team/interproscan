package uk.ac.ebi.interpro.scan.io;

import org.springframework.core.io.Resource;

import java.util.Collection;
import java.io.IOException;

/**
 * File reader.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public interface ResourceReader<T> {
    
    public Collection<T> read(Resource resource) throws IOException;

}