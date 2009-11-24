package uk.ac.ebi.interpro.scan.management.model.implementations;

import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.business.sequence.fasta.WriteFastaFile;

import java.util.List;
import java.util.UUID;
import java.io.File;
import java.io.Serializable;

import org.springframework.beans.factory.annotation.Required;

/**
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class WriteFastaFileStep extends Step<WriteFastaFileStepInstance, WriteFastaFileStepExecution> implements Serializable {

    private String fastaFilePathTemplate;

    @Required
    public void setFastaFilePathTemplate(String fastaFilePathTemplate) {
        this.fastaFilePathTemplate = fastaFilePathTemplate;
    }

    public String getFastaFilePathTemplate() {
        return fastaFilePathTemplate;
    }
}

