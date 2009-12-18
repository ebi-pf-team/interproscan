package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3;

import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.Hmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.persistence.DAOManager;
import uk.ac.ebi.interpro.scan.io.match.hmmer3.Hmmer3SearchMatchParser;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 23-Nov-2009
 * Time: 15:36:48
 */
@Entity
@DiscriminatorValue("parse_hmmer3")
public class ParseHMMER3OutputStepExecution extends StepExecution<ParseHMMER3OutputStepInstance> implements Serializable {

    protected ParseHMMER3OutputStepExecution(ParseHMMER3OutputStepInstance stepInstance) {
        super(stepInstance);
    }

    /**
     * DO NOT USE - For JPA only.
     */
    protected ParseHMMER3OutputStepExecution() {
    }


    /**
     * This method is called to execute the action that the StepExecution must perform.
     * This method should typically perform its activity in a try / catch / finally block
     * that sets the state of the step execution appropriately.
     * <p/>
     * Note that the implementation DOES have access to the protected stepInstance,
     * and from their to the protected Step, to allow it to access parameters for execution.
     * @param daoManager mandatory to allows storage of results.
     */
    @Override
    public void execute(DAOManager daoManager) {

        this.setToRun();
        InputStream is = null;
        try{
            Thread.sleep(2000);  // Have a snooze to allow NFS to catch up.
            if (daoManager == null){
                throw new IllegalStateException ("The Hmmer3 Output Parser cannot run without a DAOManager.");
            }
            if (daoManager.getRawMatchDAO() == null){
                throw new IllegalStateException ("The DAOManager is present, but does not contain a RawMatchDAO.");
            }
            is = new FileInputStream(this.getStepInstance().getHmmerOutputFilePath());
            Hmmer3SearchMatchParser parser = this.getStepInstance().getStep().getParser();
            Set<RawProtein<Hmmer3RawMatch>> parsedResults = parser.parse(is);

            daoManager.getRawMatchDAO().insertRawSequenceIdentifiers(parsedResults);
            this.completeSuccessfully();
        } catch (Exception e) {
            this.fail();
            System.out.println("Doh.  Hmmer output parsing failed.");
            e.printStackTrace();
            LOGGER.error ("Doh." , e);
        }
        finally {
            if (is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.error ("Duh - parsed OK, but can't close the input stream?" , e);
                }
            }
        }
    }
}
