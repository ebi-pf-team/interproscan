package uk.ac.ebi.interpro.scan.web.io;

import org.springframework.context.ResourceLoaderAware;
import uk.ac.ebi.interpro.scan.web.model.SimpleProtein;

import java.io.IOException;

/**
 * @author Phil Jones
 *         Date: 23/02/12
 */
public interface CreateSimpleProteinFromMatchData extends ResourceLoaderAware {

    SimpleProtein queryByAccession(String ac) throws IOException;

    SimpleProtein queryByMd5(String md5) throws IOException;
}
