package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.filter.RawMatchFilter;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.Set;

/**
 * Performs post-processing and data persistence.
 *
 * @author Antony Quinn
 * @version $Id$
 */
public abstract class FilterStep<T extends RawMatch, U extends Match> extends Step {

    private SignatureLibrary signatureLibrary;
    private String signatureLibraryRelease;
    private RawMatchFilter<T> filter;
    private RawMatchDAO<T> rawMatchDAO;
    private FilteredMatchDAO<T, U> filteredMatchDAO;

    @Required
    public void setSignatureLibrary(SignatureLibrary signatureLibrary) {
        this.signatureLibrary = signatureLibrary;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    public RawMatchFilter<T> getFilter() {
        return filter;
    }

    @Required
    public void setFilter(RawMatchFilter<T> filter) {
        this.filter = filter;
    }

    public SignatureLibrary getSignatureLibrary() {
        return signatureLibrary;
    }

    public String getSignatureLibraryRelease() {
        return signatureLibraryRelease;
    }

    @Required
    public void setRawMatchDAO(RawMatchDAO<T> rawMatchDAO) {
        this.rawMatchDAO = rawMatchDAO;
    }

    @Required
    public void setFilteredMatchDAO(FilteredMatchDAO<T, U> filteredMatchDAO) {
        this.filteredMatchDAO = filteredMatchDAO;
    }

    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {

        if (checkIfDoSkipRun(stepInstance.getBottomProtein(), stepInstance.getTopProtein())) {
            String key = getKey(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
            Utilities.verboseLog(10, "doSkipRun - step: "  + this.getId() + " - " +  key);
            return;
        }

        // Get raw matches
        Set<RawProtein<T>> rawProteins = rawMatchDAO.getProteinsByIdRange(
                stepInstance.getBottomProtein(),
                stepInstance.getTopProtein(),
                getSignatureLibraryRelease()
        );
        // Filter
        Set<RawProtein<T>> filteredProteins = getFilter().filter(rawProteins);
        // Persist
        filteredMatchDAO.persist(filteredProteins);
    }

}
