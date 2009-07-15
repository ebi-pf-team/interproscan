package uk.ac.ebi.interpro.scan.batch.tasklet;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.*;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

/**
 * Tests {@link FileWritingSystemCommandTasklet}.
 *
 * @author  Antony Quinn
 * @version $Id: FileWritingSystemCommandTaskletTest.java,v 1.3 2009/06/18 10:53:08 aquinn Exp $
 * @since   1.0
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class FileWritingSystemCommandTaskletTest {

    @Autowired
    @Qualifier("goodCommand")
    private Tasklet goodCommand;

    @Autowired
    @Qualifier("badCommand")
    private Tasklet badCommand;

    @Autowired
    @Qualifier("badSequence")
    private Tasklet badSequence;

    private StepContribution stepContribution;

    @Before public void setUp() throws Exception  {
        stepContribution = new StepContribution(MetaDataInstanceFactory.createStepExecution());
    }

    @Test public void testExecuteGood() throws Exception {
        assertEquals(RepeatStatus.FINISHED, goodCommand.execute(stepContribution, null));
    }

    @Test(expected=IOException.class)
    public void testExecuteBadCommand() throws Exception {
        badCommand.execute(stepContribution, null);
    }

    @Test(expected=IOException.class)
    public void testExecuteBadSequence() throws Exception {
        badSequence.execute(stepContribution, null);
    }

}