package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.filter.Gene3dRawMatchFilter;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Hmmer3Match;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;

import java.util.Set;

/**
 * Represents parsing step for Gene3D.
 *
 * @author Antony Quinn
 * @version $Id$
 */
//public class Gene3dHmmer3FilterStep extends FilterStep<Gene3dHmmer3RawMatch, Hmmer3Match> {
public class Gene3dHmmer3FilterStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(Gene3dHmmer3FilterStep.class.getName());

    private SignatureLibrary signatureLibrary;
    private String signatureLibraryRelease;

    private String ssfOutputFileTemplate;
    private String ssfInputFileTemplate;
    private String binaryOutputFileTemplate;

    private Gene3dRawMatchFilter filter;
    private RawMatchDAO<Gene3dHmmer3RawMatch> rawMatchDAO;
    private FilteredMatchDAO<Gene3dHmmer3RawMatch, Hmmer3Match> filteredMatchDAO;

    @Required
    public void setSignatureLibrary(SignatureLibrary signatureLibrary) {
        this.signatureLibrary = signatureLibrary;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    public Gene3dRawMatchFilter getFilter() {
        return filter;
    }

    @Required
    public void setFilter(Gene3dRawMatchFilter filter) {
        this.filter = filter;
    }

    @Required
    public void setSsfOutputFileTemplate(String ssfOutputFileTemplate) {
        this.ssfOutputFileTemplate = ssfOutputFileTemplate;
    }

    @Required
    public void setSsfInputFileTemplate(String ssfInputFileTemplate) {
        this.ssfInputFileTemplate = ssfInputFileTemplate;
    }

    @Required
    public void setBinaryOutputFileTemplate(String binaryOutputFileTemplate) {
        this.binaryOutputFileTemplate = binaryOutputFileTemplate;
    }

    public SignatureLibrary getSignatureLibrary() {
        return signatureLibrary;
    }

    public String getSignatureLibraryRelease() {
        return signatureLibraryRelease;
    }

    @Required
    public void setRawMatchDAO(RawMatchDAO<Gene3dHmmer3RawMatch> rawMatchDAO) {
        this.rawMatchDAO = rawMatchDAO;
    }

    @Required
    public void setFilteredMatchDAO(FilteredMatchDAO<Gene3dHmmer3RawMatch, Hmmer3Match> filteredMatchDAO) {
        this.filteredMatchDAO = filteredMatchDAO;
    }

    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        // Get raw matches
        Set<RawProtein<Gene3dHmmer3RawMatch>> rawProteins = rawMatchDAO.getProteinsByIdRange(
                stepInstance.getBottomProtein(),
                stepInstance.getTopProtein(),
                getSignatureLibraryRelease()
        );
        // Check we have correct data
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("In execute() method of Gene3dHmmer3FilterStep.java (Gene3D Post Processing.)");
            LOGGER.debug("DAO returned " + rawProteins.size() + " raw proteins to filter."); // 4
        }
        filter.setSsfInputFilePath(stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, ssfInputFileTemplate));
        filter.setSsfOutputFilePath(stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, ssfOutputFileTemplate));
        filter.setBinaryPipedOutputFilePath(stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, binaryOutputFileTemplate));

        // Filter
        Set<RawProtein<Gene3dHmmer3RawMatch>> filteredProteins = filter.filter(rawProteins);
        // Persist
        filteredMatchDAO.persist(filteredProteins);
    }

}
