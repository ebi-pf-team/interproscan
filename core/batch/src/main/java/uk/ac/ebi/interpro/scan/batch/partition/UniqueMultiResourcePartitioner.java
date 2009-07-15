package uk.ac.ebi.interpro.scan.batch.partition;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.Resource;

import java.util.Map;
import java.util.UUID;
import java.io.File;

/**
 * Wrapper for {@link MultiResourcePartitioner} that adds {@link UUID} and file ID to execution context.
 *
 * @author  Antony Quinn
 * @version $Id: UniqueMultiResourcePartitioner.java,v 1.3 2009/06/19 09:20:33 aquinn Exp $
 * @since   1.0
 */
public class UniqueMultiResourcePartitioner implements Partitioner {

    public static final String FILE_NAME_KEY = "fileName";
    public static final String FILE_ID_KEY   = "fileId";
    public static final String UUID_KEY      = "uuid";

    private MultiResourcePartitioner delegate = new MultiResourcePartitioner();

    public void setResources(Resource[] resources) {
		delegate.setResources(resources);
	}

    public Map<String, ExecutionContext> partition(int gridSize) {
        delegate.setKeyName(FILE_NAME_KEY);
        Map<String, ExecutionContext> map = delegate.partition(gridSize);
        for (ExecutionContext context : map.values())   {
            context.put(UUID_KEY, UUID.randomUUID().toString());
            context.put(FILE_ID_KEY, getFileId((String)context.get(FILE_NAME_KEY)));
        }
        return map;
    }

    // Returns file name minus extension, eg. "file:/tmp/blah.txt" returns "blah"
    private String getFileId(String fileName)  {
        String name = new File(fileName).getName();
        int dotIndex = name.lastIndexOf(".");
        if (dotIndex > 0)   {
            return name.substring(0, dotIndex);            
        }
        return name;
    }
}
