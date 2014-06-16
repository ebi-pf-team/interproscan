package uk.ac.ebi.interpro.scan.management.model.implementations.pirsf.hmmer2;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.business.postprocessing.pirsf.hmmer2.SubfamilyPersistenceProcessor;
import uk.ac.ebi.interpro.scan.io.pirsf.hmmer2.PirsfMatchTempParser;
import uk.ac.ebi.interpro.scan.io.pirsf.hmmer2.PirsfSubfamilyFileParser;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Hmmer2Match;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer2RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents the persistence step of the filtered raw matches at the end of the post processing workflow.
 * Supports PIRSF version 2.75 and higher (subfamily integration).
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0
 */

public class PirsfSubfamPersistStep extends PirsfPersistStep {

    private static final Logger LOGGER = Logger.getLogger(PirsfSubfamPersistStep.class.getName());

    // Matches passed the sub family post processing
    private String subfamMatchesFileName;

    //The processing (algorithm) is different between PIRSF version 2.74 and 2.75 (and higher). Since version 2.75 and higher subfamilies are supported as well.
    private String signatureLibraryRelease;

    private String subFamilyMapFileName;

    private PirsfSubfamilyFileParser subfamilyFileParser;

    private FilteredMatchDAO<PIRSFHmmer2RawMatch, Hmmer2Match> filteredMatchDAO;

    private SubfamilyPersistenceProcessor persistenceProcessor;

    @Required
    public void setSubfamMatchesFileName(String subfamMatchesFileName) {
        this.subfamMatchesFileName = subfamMatchesFileName;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @Required
    public void setSubFamilyMapFileName(String subFamilyMapFileName) {
        this.subFamilyMapFileName = subFamilyMapFileName;
    }

    @Required
    public void setSubfamilyFileParser(PirsfSubfamilyFileParser subfamilyFileParser) {
        this.subfamilyFileParser = subfamilyFileParser;
    }

    @Required
    public void setFilteredMatchDAO(FilteredMatchDAO<PIRSFHmmer2RawMatch, Hmmer2Match> filteredMatchDAO) {
        this.filteredMatchDAO = filteredMatchDAO;
    }

    @Required
    public void setPersistenceProcessor(SubfamilyPersistenceProcessor persistenceProcessor) {
        this.persistenceProcessor = persistenceProcessor;
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     * <p/>
     * If an error occurs that cannot be immediately recovered from, the implementation
     * of this method MUST throw a suitable Exception, as the call
     * to execute is performed within a transaction with the reply to the JMSBroker.
     * <p/>
     * Implementations of this method MAY call delayForNfs() before starting, if, for example,
     * they are operating of file system resources.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        double signatureLibraryReleaseValue = Double.parseDouble(signatureLibraryRelease);
        if (signatureLibraryReleaseValue >= 2.75d) {
            final Set<RawProtein<PIRSFHmmer2RawMatch>> resultRawMatches = new HashSet<RawProtein<PIRSFHmmer2RawMatch>>();
            final Map<String, RawProtein<PIRSFHmmer2RawMatch>> proteinIdToProteinMap = new HashMap<String, RawProtein<PIRSFHmmer2RawMatch>>();

            // Retrieve list of filtered matches from temporary file - blast wasn't required for these
            final String filteredMatchesFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, filteredMatchesFileName);
            try {
                Set<RawProtein<PIRSFHmmer2RawMatch>> filteredRawProteins = PirsfMatchTempParser.parse(filteredMatchesFilePath);
                copySetToMap(proteinIdToProteinMap, filteredRawProteins);
            } catch (IOException e) {
                throw new IllegalStateException("IOException thrown when parsing filtered matches file " + filteredMatchesFilePath);
            }

            // Retrieve list of filtered matches from temporary file - blast WAS required for these
            final String blastedMatchesFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, blastedMatchesFileName);
            try {
                Set<RawProtein<PIRSFHmmer2RawMatch>> blastedRawProteins = PirsfMatchTempParser.parse(blastedMatchesFilePath);
                copySetToMap(proteinIdToProteinMap, blastedRawProteins);
            } catch (IOException e) {
                throw new IllegalStateException("IOException thrown when parsing blasted matches file " + blastedMatchesFilePath);
            }

            // Retrieve list of subfamily matches from temporary file
            Set<RawProtein<PIRSFHmmer2RawMatch>> subfamRawProteins;
            final String subfamMatchesFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, subfamMatchesFileName);
            try {
                subfamRawProteins = PirsfMatchTempParser.parse(subfamMatchesFilePath);
            } catch (IOException e) {
                throw new IllegalStateException("IOException thrown when parsing subfamily matches file " + subfamMatchesFilePath);
            }

            // Retrieve sub family to super family map
            final String subFamilyMapFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, subFamilyMapFileName);
            final Resource subFamMapFileResource = new FileSystemResource(subFamilyMapFilePath);
            final Map<String, String> subfamToSuperFamMap;
            try {
                subfamToSuperFamMap = subfamilyFileParser.parse(subFamMapFileResource);
            } catch (IOException e) {
                throw new IllegalStateException("IOException thrown when parsing subfamily-superfamily map file " + subFamMapFileResource);
            }

            persistenceProcessor.process(subfamRawProteins, proteinIdToProteinMap, subfamToSuperFamMap);
            //Copy protein map to result set
            resultRawMatches.addAll(proteinIdToProteinMap.values());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("PIRSF: Retrieved " + resultRawMatches.size() + " proteins.");
                int matchCount = 0;
                for (final RawProtein rawProtein : resultRawMatches) {
                    matchCount += rawProtein.getMatches().size();
                }
                LOGGER.debug("PIRSF: A total of " + matchCount + " raw matches.");
            }

            // Persist the remaining (filtered) raw matches
            LOGGER.info("Persisting filtered raw matches...");
            filteredMatchDAO.persist(resultRawMatches);
        } else {
            throw new IllegalStateException("Step instance with ID " + stepInstance.getId() + " only supports signature library release version >= 2.75");
        }
    }

    /**
     * At this stage, any protein of the map should only have 1 attached match (always the best match).
     *
     * @param proteinIdToProteinMap
     * @param rawProteinSet
     */
    private void copySetToMap(final Map<String, RawProtein<PIRSFHmmer2RawMatch>> proteinIdToProteinMap,
                              final Set<RawProtein<PIRSFHmmer2RawMatch>> rawProteinSet) {
        LOGGER.debug("Copy raw proteins to a hash map...");
        for (RawProtein<PIRSFHmmer2RawMatch> rawProtein : rawProteinSet) {
            //Check if matches size is 1
            if (SubfamilyPersistenceProcessor.isExpectedMatchSize(rawProtein)) {
                proteinIdToProteinMap.put(rawProtein.getProteinIdentifier(), rawProtein);
            } else {
                LOGGER.warn("Didn't copy raw protein with ID " + rawProtein.getProteinIdentifier() + " into the map!");
            }
        }
    }
}