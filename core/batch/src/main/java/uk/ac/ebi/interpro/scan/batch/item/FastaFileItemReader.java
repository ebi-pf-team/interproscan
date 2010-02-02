package uk.ac.ebi.interpro.scan.batch.item;

import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.separator.RecordSeparatorPolicy;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Xref;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Restartable {@link org.springframework.batch.item.ItemReader} that reads FASTA records from input
 * {@link #setResource(Resource)}.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
public class FastaFileItemReader implements ResourceAwareItemReaderItemStream<Protein>, InitializingBean {

    // TODO: Will this class be used often or at all? If not we should remove it (what's the use case?)
   
    // TODO: Document design (see also http://forum.springsource.org/showthread.php?t=73034)

    // TODO: Might be less confusing to avoid delegating to FlatFileItemReader and just reading file directly 

    private FlatFileItemReader<String> delegate = new FlatFileItemReader<String>();

    private String lastReadId = null;
   
    private static final String  REC_START         = ">";
    private static final Pattern RECORD_PATTERN    = Pattern.compile("(.*)" + REC_START + "(.*)");
    // Position of sequence and sequence ID in regex
    private static final int RECORD_PATTERN_GROUP_SEQUENCE    = 1;
    private static final int RECORD_PATTERN_GROUP_SEQUENCE_ID = 2;

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    public void afterPropertiesSet() throws Exception {
        // Dave Syer recommended we don't save state [http://forum.springsource.org/showthread.php?t=73034]
        // TODO: Try saving state to see what happens
        delegate.setSaveState(false);
        // Check resource exists 
        delegate.setStrict(true);
        delegate.setLineMapper(new PassThroughLineMapper());
        delegate.setRecordSeparatorPolicy(new FastaRecordSeparatorPolicy());
        delegate.afterPropertiesSet();
    }


    @Required
    public void setResource(Resource resource) {
        delegate.setResource(resource);
    }

    /**
     * Open the stream for the provided {@link org.springframework.batch.item.ExecutionContext}.
     *
     * @throws IllegalArgumentException if context is null
     */
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        delegate.open(executionContext);
    }

    /**
     * Indicates that the execution context provided during open is about to be saved. If any state is remaining, but
     * has not been put in the context, it should be added here.
     *
     * @param executionContext to be updated
     * @throws IllegalArgumentException if executionContext is null.
     */
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        delegate.update(executionContext);
    }

    /**
     * If any resources are needed for the stream to operate they need to be destroyed here. Once this method has been
     * called all other methods (except open) may throw an exception.
     */
    public void close() throws ItemStreamException {
        delegate.close();
    }

    /**
     * Reads a piece of input data and advance to the next one. Implementations
     * <strong>must</strong> return <code>null</code> at the end of the input
     * data set. In a transactional setting, caller might get the same item
     * twice from successive calls (or otherwise), if the first call was in a
     * transaction that rolled back.
     *
     * @throws Exception if an underlying resource is unavailable.
     */
    public Protein read() throws Exception {
        String record = delegate.read();
        if (record == null) {
            // No more records to process
            return null;
        }
        // Get sequence ID
        String id;
        if (record.startsWith(REC_START))   {
            // This is the first record in the file
            Matcher matcher = RECORD_PATTERN.matcher(record);
            if (matcher.matches())  {
                id = matcher.group(RECORD_PATTERN_GROUP_SEQUENCE_ID);
            }
            else    {
                throw new IllegalStateException("Could not find sequence ID in " + record + " using " + matcher);
            }
            // Get sequence and id of following sequence
            record = delegate.read();
        }
        else    {
            id = lastReadId;
        }
        // Get sequence
        String sequence;
        Matcher matcher = RECORD_PATTERN.matcher(record);
        if (matcher.matches())  {
            sequence   = matcher.group(RECORD_PATTERN_GROUP_SEQUENCE);
            lastReadId = matcher.group(RECORD_PATTERN_GROUP_SEQUENCE_ID);
        }
        else    {
            // Last record in file (just sequence, no ID)
            sequence = record;
        }
        // Return protien
        Protein protein = new Protein(sequence);
        protein.addCrossReference(new Xref(id));
        return protein;
    }

    private static final class FastaRecordSeparatorPolicy implements RecordSeparatorPolicy {

        /**
         * Signal the end of a record based on the content of a line, being the
         * latest line read from an input source. The input is what you would expect
         * from {@link java.io.BufferedReader#readLine()} - i.e. no line separator character
         * at the end. But it might have line separators embedded in it.
         *
         * @param line a String without a newline character at the end.
         * @return true if this line is the end of a record.
         */
        public boolean isEndOfRecord(String line) {            
            return line.contains(REC_START);
        }

        /**
         * Give the policy a chance to post-process a complete record, e.g. remove a
         * suffix.
         *
         * @param record the complete record.
         * @return a modified version of the record if desired.
         */
        public String postProcess(String record) {
            String nullString = "null";
            // Because we have no end-of-record character, we'll always get a null at the end of the string
            if (record != null && record.endsWith(nullString))    {
                return record.substring(0, record.lastIndexOf(nullString));
            }
            return record;
        }

        /**
         * Pre-process a record before another line is appended, in the case of a
         * multi-line record. Can be used to remove a prefix or line-continuation
         * marker. If a record is a single line this callback is not used (but
         * {@link #postProcess(String)} will be).
         *
         * @param record the current record.
         * @return the line as it should be appended to a record.
         */
        public String preProcess(String record) {
            return record;
        }

    }

}
