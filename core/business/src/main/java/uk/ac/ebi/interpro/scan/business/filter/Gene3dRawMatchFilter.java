/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.interpro.scan.business.filter;

import org.apache.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.business.binary.BinaryRunner;
import uk.ac.ebi.interpro.scan.business.binary.SimpleBinaryRunner;
import uk.ac.ebi.interpro.scan.io.gene3d.DomainFinderRecord;
import uk.ac.ebi.interpro.scan.io.gene3d.DomainFinderResourceReader;
import uk.ac.ebi.interpro.scan.io.gene3d.DomainFinderResourceWriter;
import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * Runs post-processing on raw results and parses results.
 *
 * @author Antony Quinn
 * @version $Id$
 */
public final class Gene3dRawMatchFilter implements RawMatchFilter<Gene3dHmmer3RawMatch> {

    // TODO This class seems to only be used by a unit test, not by the application itself! The app uses the Gene3DPostProcessing class instead.

    private static final Logger LOGGER = Logger.getLogger(Gene3dRawMatchFilter.class.getName());

    /**
     * Currently required for use in "cli" module, although may review this.
     */
    private String temporaryFilePath = null;

    /**
     * For use by I5, to allow I5 to control temporary file locations
     */
    private String ssfInputFilePath;

    /**
     * For use by I5, to allow I5 to control temporary file locations
     */
    private String ssfOutputFilePath;

    /**
     * For use by I5, to allow I5 to control temporary file locations
     */
    private String binaryPipedOutputFilePath;

    BinaryRunner binaryRunner = new SimpleBinaryRunner();

    public void setBinaryRunner(BinaryRunner binaryRunner) {
        this.binaryRunner = binaryRunner;
    }

    public void setSsfInputFilePath(String ssfInputFilePath) {
        this.ssfInputFilePath = ssfInputFilePath;
    }

    public void setSsfOutputFilePath(String ssfOutputFilePath) {
        this.ssfOutputFilePath = ssfOutputFilePath;
    }

    public void setBinaryPipedOutputFilePath(String binaryPipedOutputFilePath) {
        this.binaryPipedOutputFilePath = binaryPipedOutputFilePath;
    }

    @Override
    public Set<RawProtein<Gene3dHmmer3RawMatch>> filter(Set<RawProtein<Gene3dHmmer3RawMatch>> rawProteins) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Gener3dRawMatchFilter.filter() method called.  Parameters for running:");
            LOGGER.debug("temporaryFilePath = " + temporaryFilePath);
            LOGGER.debug("ssfInputFilePath = " + ssfInputFilePath);
            LOGGER.debug("binaryPipedOutputFilePath = " + binaryPipedOutputFilePath);
            LOGGER.debug("binaryRunner loaded? " + (binaryRunner != null));
        }

        // Create SSF file
        final Resource ssfInputFile = createSsf(rawProteins);

        // Run DF3 on SSF file
        final Resource ssfOutputFile = createTemporaryResource(".output.ssf", ssfOutputFilePath);

        if (binaryPipedOutputFilePath != null) {
            binaryRunner.setTemporaryFilePath(binaryPipedOutputFilePath);
        }


        final StringBuilder additionalArguments = new StringBuilder();
        try {
            additionalArguments
                    .append("-i ")
                    .append(ssfInputFile.getFile().getAbsolutePath())
                    .append(' ')
                    .append("-o ")
                    .append(ssfOutputFile.getFile().getAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        // Run command but don't capture output (DF3 writes to a file, not stdout)
        try {
            binaryRunner.run(additionalArguments.toString());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        // Parse and filter DF3 results
        final Set<RawProtein<Gene3dHmmer3RawMatch>> filteredRawProteins = filter(rawProteins, ssfOutputFile);

        // Delete
        if (binaryRunner.isDeleteTemporaryFiles()) {
            try {
                if (!ssfInputFile.getFile().delete()) {
                    LOGGER.warn("Could not delete " + ssfInputFile.getDescription());
                }
                if (!ssfOutputFile.getFile().delete()) {
                    LOGGER.warn("Could not delete " + ssfOutputFile.getDescription());
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        return filteredRawProteins;

    }

    private Resource createSsf(Set<RawProtein<Gene3dHmmer3RawMatch>> rawProteins) {
        // Generate SSF file
        Collection<DomainFinderRecord> records = new ArrayList<DomainFinderRecord>();
        for (RawProtein<Gene3dHmmer3RawMatch> p : rawProteins) {
            for (Gene3dHmmer3RawMatch m : p.getMatches()) {
                records.add(DomainFinderRecord.valueOf(m));
            }
        }
        // Modified to use I5 temporary file location system, if available.
        Resource ssfFile = createTemporaryResource(".input.ssf", ssfInputFilePath);

        DomainFinderResourceWriter writer = new DomainFinderResourceWriter();
        try {
            writer.write(ssfFile, records);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return ssfFile;
    }

    // Package-private so we can test

    Set<RawProtein<Gene3dHmmer3RawMatch>> filter(Set<RawProtein<Gene3dHmmer3RawMatch>> rawProteins, Resource ssfFile) {

        // Parse DF3 results
        DomainFinderResourceReader resourceReader = new DomainFinderResourceReader();
        Collection<DomainFinderRecord> domainFinderRecords;
        try {
            domainFinderRecords = resourceReader.read(ssfFile);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        // Map of sequence ID's to the set of DomainFinderRecord's for each particular sequence
        Map<String, Set<DomainFinderRecord>> domainFinderRecordsMap = new HashMap<>(rawProteins.size());
        for (DomainFinderRecord dfr : domainFinderRecords) {
            String id = dfr.getSequenceId();
            if (domainFinderRecordsMap.containsKey(id)) {
                domainFinderRecordsMap.get(id).add(dfr);
            }
            else {
                Set<DomainFinderRecord> newSet = new HashSet<>();
                newSet.add(dfr);
                domainFinderRecordsMap.put(id, newSet);
            }
        }


        // Update raw matches with values from DomainFinder
        return filter(rawProteins, domainFinderRecordsMap);

    }

    // Update raw matches with values from DomainFinder

    private Set<RawProtein<Gene3dHmmer3RawMatch>> filter(final Set<RawProtein<Gene3dHmmer3RawMatch>> rawProteins,
                                                         final Map<String, Set<DomainFinderRecord>> domainFinderRecordsMap) {
        final Set<RawProtein<Gene3dHmmer3RawMatch>> filteredProteins = new HashSet<RawProtein<Gene3dHmmer3RawMatch>>();
        final Set<String> matchKeys = new HashSet<String>();
        for (RawProtein<Gene3dHmmer3RawMatch> p : rawProteins) {
            String id = p.getProteinIdentifier();
            RawProtein<Gene3dHmmer3RawMatch> filteredProtein = new RawProtein<>(id);
            // Sort p.getMatches() by location.start so we always get consistent results if need to choose between raw matches
            List<Gene3dHmmer3RawMatch> matchList = new ArrayList<>(p.getMatches());
            if (matchList.size() > 1) {
                Collections.sort(matchList, new Comparator<Gene3dHmmer3RawMatch>() {
                    public int compare(Gene3dHmmer3RawMatch record1, Gene3dHmmer3RawMatch record2) {
                        return Integer.valueOf(record1.getLocationStart()).compareTo(record2.getLocationStart());
                    }
                });
            }
            for (Gene3dHmmer3RawMatch match : matchList) {
                if (id.equals(match.getSequenceIdentifier())) {
                    addRecord(filteredProtein, match, domainFinderRecordsMap, matchKeys);
                }
            }
            if (!filteredProtein.getMatches().isEmpty()) {
                filteredProteins.add(filteredProtein);
            }
        }
        return filteredProteins;
    }

    private void addRecord(final RawProtein<Gene3dHmmer3RawMatch> filteredProtein,
                           final Gene3dHmmer3RawMatch m,
                           final Map<String, Set<DomainFinderRecord>> domainFinderRecordsMap,
                           final Set<String> matchKeys) {
        final String mSeqId = m.getSequenceIdentifier();
        final String mModelId = m.getModelId();
        final String matchKeyPrefix = mSeqId + '-' + mModelId + '-';
        final Set<DomainFinderRecord> domainFinderRecords = domainFinderRecordsMap.get(mSeqId);
        if (domainFinderRecords != null) {
            for (DomainFinderRecord r : domainFinderRecords) {
                // Parse segment boundaries
                String s = r.getSegmentBoundaries();
                String[] segments = s.split(DomainFinderRecord.SEGMENT_BOUNDARY_SEPARATOR);
                int lowestBoundary = Integer.valueOf(segments[0]);
                int highestBoundary = Integer.valueOf(segments[segments.length - 1]);
                // Match up the DomainFinder record with the corresponding raw match
                // Check for "greater than" or "less than" positions, because DomainFinder may have split the domain
                // match into smaller chunks.
                // In this example, the original raw match as reported by HMMER (position 4-41) is split into
                // two matches (positions 6-10 and 20-26). To marry up the raw match with the
                // filtered matches (so we can get the evalue, score ...etc),  we need to check that the raw match
                // start (4) is equal to or less than the lowest filtered match boundary (6) and
                // the end (41) is equal to or greater than the highest filtered match boundary (26).
                //                     4                                  41
                // raw match:      |---####################################--------|
                //                       6   10        20    26
                // filtered match: |-----#####---------#######---------------------|
                // ... where #=domain
                // Track matches that we've added in case there are two raw matches with the same e-value that fall within
                // the greater-than/less-than boundaries but have different start and end positions -- in these cases
                // "just take the first match" (Source: Craig McAnulla, June 2010)
                String matchKey = matchKeyPrefix + r.getSegmentBoundaries();
                if (mModelId.equals(r.getModelId()) &&
                        m.getLocationStart() <= lowestBoundary &&
                        m.getLocationEnd() >= highestBoundary &&
                        m.getDomainIeValue() == r.getDomainIeValue() &&
                        !matchKeys.contains(matchKey)) {
                    // We should never find more than one raw match
                    if (filteredProtein.getMatches().contains(m)) {
                        throw new IllegalStateException("Found duplicate filtered match: " + m);
                    }
                    // Get start and end coordinates for each split domain
                    // For example "10:20:30:40" represents two domains: (start=10,end=20) and (start=30,end=40)
                    // The domain string has already been split, so we have:
                    // segments[0]=10, segments[1]=20, segments[2]=30, segments[4]=40
                    // The even numbered array indexes contain the start position, and
                    // the odd numbered indexes contain the end position
                    int splitDomainStart = 0;
                    for (int i = 0; i < segments.length; i++) {
                        int number = Integer.valueOf(segments[i]);
                        int splitDomainEnd;
                        // Even numbers (array index 0, 2, 4 ...etc) = start
                        if (i % 2 == 0) {
                            splitDomainStart = number;
                        }
                        // Odd numbers (array index 1, 3, 5 ...etc) = end
                        else {
                            // Create match for each split domain
                            if (number > splitDomainStart) {  // Exclude split domain matches where the length is 1.
                                Gene3dHmmer3RawMatch match = new Gene3dHmmer3RawMatch(
                                        mSeqId,
                                        mModelId,
                                        m.getSignatureLibraryRelease(),
                                        splitDomainStart,
                                        number,
                                        m.getEvalue(),
                                        m.getScore(),
                                        m.getHmmStart(),       // TODO: What should HMM start and end be for split domains?
                                        m.getHmmEnd(),
                                        m.getHmmBounds(),
                                        m.getLocationScore(),
                                        m.getEnvelopeStart(),  // TODO: What should env start and end be for split domains?
                                        m.getEnvelopeEnd(),
                                        m.getExpectedAccuracy(),
                                        m.getFullSequenceBias(),
                                        m.getDomainCeValue(),
                                        m.getDomainIeValue(),
                                        m.getDomainBias(),
                                        m.getCigarAlignment());
                                // Add match
                                filteredProtein.addMatch(match);
                                matchKeys.add(matchKey);
                            }
                        }
                    }
                    break; // Found the DomainFinder record with the corresponding raw match
                }
            }
        }
    }

    public void setTemporaryFilePath(String temporaryFilePath) {
        this.temporaryFilePath = temporaryFilePath;
    }

    /**
     * @param suffix                        the suffix for the file name.
     * @param defaultFullyQualifiedFilePath which, if not null, provides the fully qualified path for the temporary file.
     * @return the Resource object
     */
    private Resource createTemporaryResource(String suffix, final String defaultFullyQualifiedFilePath) {
        try {
            File file;
            if (defaultFullyQualifiedFilePath != null) {
                // I5 Mode.
                file = new File(defaultFullyQualifiedFilePath);
            } else if (temporaryFilePath == null) {
                file = File.createTempFile("ipr-", suffix);
            } else {
                file = new File(temporaryFilePath + suffix);
            }
            if (file.exists()) {
                LOGGER.warn("Temporary file " + file.getAbsolutePath() + " already exists.  Deleting.");
                if (!file.delete()) {
                    throw new IllegalStateException("Cannot delete pre-existing file " + file.getAbsolutePath() + ".  Check file permissions. Exiting.");
                }
            }
            if (!file.createNewFile()) {
                throw new IllegalStateException("Cannot write to path " + file.getAbsolutePath() + ". Unrecoverable error - exiting.");
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Temporary file created OK: " + file.getAbsolutePath());
            }

            return new FileSystemResource(file);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


}
