package uk.ac.ebi.interpro.scan.io.gene3d;

import java.util.*;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.springframework.core.io.Resource;

/**
 * Reads model2sf files.
 *
 * @author Antony Quinn
 * @version $Id$
 */
public final class Model2SfReader {

    // TODO: Would be better to use extend a generic class like AbstractResourceReader

    private String prefix = "G3DSA:";

    public Map<String, String> read(Resource resource) throws IOException {
        if (resource == null) {
            throw new NullPointerException("Resource is null");
        }
        if (!resource.exists())  {
            throw new IllegalStateException(resource.getFilename() + " does not exist");
        }
        if (!resource.isReadable())  {
            throw new IllegalStateException(resource.getFilename() + " is not readable");
        }
        final Map<String, String> records = new HashMap<String, String>();
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            while (reader.ready()) {
                String line[] = reader.readLine().split(",");
                records.put(line[0], prefix + line[1]);  // model - signature
            }
        }
        finally {
            if (reader != null){
                reader.close();
            }
        }
        return Collections.unmodifiableMap(records);
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
