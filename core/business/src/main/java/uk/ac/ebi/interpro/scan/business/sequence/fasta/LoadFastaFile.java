package uk.ac.ebi.interpro.scan.business.sequence.fasta;

import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.business.sequence.AbstractProteinLoader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: phil
 * Date: 14-Nov-2009
 * Time: 09:27:14
 */
public class LoadFastaFile extends AbstractProteinLoader {

    private Resource fastaFile;

    public LoadFastaFile(Resource fastaFile){
        this.fastaFile = fastaFile;
    }

    public void loadSequences() {
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(fastaFile.getFile()));
            String currentId = null;
            final StringBuffer currentSequence = new StringBuffer();
            while (reader.ready()){
                String line = reader.readLine();
                if (line.length() > 0){
                    if ('>' == line.charAt(0)){
                        // Found ID line.
                        // Store previous record, if it exists.
                        if (currentId != null){
                            store (currentSequence.toString(), currentId);
                            currentSequence.delete(0, currentSequence.length());
                        }
                        currentId = line.substring(1).trim();
                    }
                    else {
                        // must be a sequence line.
                        currentSequence.append (line.trim());
                    }
                }
            }
            // Store the final record (if there were any at all!)
            if (currentId != null){
                store (currentSequence.toString(), currentId);
            }
        }
        catch (FileNotFoundException e) {
            throw new IllegalStateException ("Could not locate fasta file.", e);
        }
        catch (IOException e) {
            throw new IllegalStateException ("Could not read file.", e);
        }  finally{
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new IllegalStateException ("Unable to close reader to fasta file.", e);
                }
            }
        }
    }



}
