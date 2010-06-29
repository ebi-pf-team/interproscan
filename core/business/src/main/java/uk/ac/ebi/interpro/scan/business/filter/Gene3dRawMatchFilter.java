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
import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.io.gene3d.DomainFinderResourceWriter;
import uk.ac.ebi.interpro.scan.io.gene3d.DomainFinderRecord;
import uk.ac.ebi.interpro.scan.io.gene3d.DomainFinderResourceReader;
import uk.ac.ebi.interpro.scan.business.binary.SimpleBinaryRunner;
import uk.ac.ebi.interpro.scan.business.binary.BinaryRunner;

import java.util.*;
import java.io.IOException;
import java.io.File;


/**
 * Runs post-processing on raw results and parses results.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public final class Gene3dRawMatchFilter implements RawMatchFilter<Gene3dHmmer3RawMatch> {

    private static final Logger LOGGER = Logger.getLogger(Gene3dRawMatchFilter.class);

    private String temporaryFilePath = null;
    
    BinaryRunner binaryRunner = new SimpleBinaryRunner();

    public void setBinaryRunner(BinaryRunner binaryRunner) {
        this.binaryRunner = binaryRunner;
    }

    @Override public Set<RawProtein<Gene3dHmmer3RawMatch>> filter(Set<RawProtein<Gene3dHmmer3RawMatch>> rawProteins) {

        // Create SSF file
        Resource ssfInputFile = createSsf(rawProteins);

        // Run DF3 on SSF file
        Resource ssfOutputFile = createTemporaryResource(".output.ssf");
        String additionalArguments;
        try {
            additionalArguments =
                    "-i " + ssfInputFile.getFile().getAbsolutePath() + " " + 
                    "-o " + ssfOutputFile.getFile().getAbsolutePath();
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }

        // Run command but don't capture output (DF3 writes to a file, not stdout)
        try {
            binaryRunner.run(additionalArguments);
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }

        // Parse and filter DF3 results
        final Set<RawProtein<Gene3dHmmer3RawMatch>> filteredRawProteins = filter(rawProteins, ssfOutputFile);

        // Delete
        if (binaryRunner.isDeleteTemporaryFiles())   {
            try {
                if (!ssfInputFile.getFile().delete())   {
                    LOGGER.warn("Could not delete " + ssfInputFile.getDescription());
                }
                if (!ssfOutputFile.getFile().delete())   {
                    LOGGER.warn("Could not delete " + ssfOutputFile.getDescription());
                }
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }        

        return filteredRawProteins;

     }

    private Resource createSsf(Set<RawProtein<Gene3dHmmer3RawMatch>> rawProteins) {
        // Generate SSF file
        Collection<DomainFinderRecord> records = new ArrayList<DomainFinderRecord>();
        for (RawProtein<Gene3dHmmer3RawMatch> p : rawProteins)    {
            for (Gene3dHmmer3RawMatch m : p.getMatches())   {
                records.add(DomainFinderRecord.valueOf(m));
            }
        }
        Resource ssfFile = createTemporaryResource(".input.ssf");
        DomainFinderResourceWriter writer = new DomainFinderResourceWriter();
        try {
            writer.write(ssfFile, records);
        }
        catch (IOException e) {
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
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }

        // Update raw matches with values from DomainFinder
        return filter(rawProteins, domainFinderRecords);

     }

    // Update raw matches with values from DomainFinder
    private Set<RawProtein<Gene3dHmmer3RawMatch>> filter(final Set<RawProtein<Gene3dHmmer3RawMatch>> rawProteins,
                                                         final Collection<DomainFinderRecord> domainFinderRecords) {
        final Set<RawProtein<Gene3dHmmer3RawMatch>> filteredProteins = new HashSet<RawProtein<Gene3dHmmer3RawMatch>>();
        final Set<String> matchKeys = new HashSet<String>();        
        for (RawProtein<Gene3dHmmer3RawMatch> p : rawProteins)    {
            String id = p.getProteinIdentifier();
            RawProtein<Gene3dHmmer3RawMatch> filteredProtein = new RawProtein<Gene3dHmmer3RawMatch>(id);
            // TODO: Sort p.getMatches() by location.start so we always get consistent results if need to choose between raw matches
            List<Gene3dHmmer3RawMatch> matchList = new ArrayList<Gene3dHmmer3RawMatch>(p.getMatches());
            Collections.sort(matchList, new Comparator<Gene3dHmmer3RawMatch>() {
                public int compare(Gene3dHmmer3RawMatch record1, Gene3dHmmer3RawMatch record2) {
                    return Integer.valueOf(record1.getLocationStart()).compareTo(record2.getLocationStart());
                }
            }
            );
            //for (Gene3dHmmer3RawMatch match : p.getMatches())   {
            for (Gene3dHmmer3RawMatch match : matchList)   {
                if (id.equals(match.getSequenceIdentifier()))    {
                    addRecord(filteredProtein, match, domainFinderRecords, matchKeys);
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
                          final Collection<DomainFinderRecord> domainFinderRecords,
                          final Set<String> matchKeys)  {
        for (DomainFinderRecord r : domainFinderRecords)    {
            // Parse segment boundaries
            String s = r.getSegmentBoundaries();
            String[] segments   = s.split(DomainFinderRecord.SEGMENT_BOUNDARY_SEPARATOR);
            int lowestBoundary  = Integer.valueOf(segments[0]);
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
            String matchKey = m.getSequenceIdentifier() + "-" + m.getModel() + "-" + r.getSegmentBoundaries();
            if (m.getSequenceIdentifier().equals(r.getSequenceId()) &&
                m.getModel().equals(r.getModelId()) && 
                m.getLocationStart() <= lowestBoundary &&
                m.getLocationEnd() >= highestBoundary &&
                m.getDomainIeValue() == r.getDomainIeValue() && 
                !matchKeys.contains(matchKey))    {
                // We should never find more than one raw match
                if (filteredProtein.getMatches().contains(m))   {
                    throw new IllegalStateException("Found duplicate filtered match: " + m);
                }
                // Get start and end coordinates for each split domain
                // For example "10:20:30:40" represents two domains: (start=10,end=20) and (start=30,end=40)
                // The domain string has already been split, so we have:
                // segments[0]=10, segments[1]=20, segments[2]=30, segments[4]=40
                // The even numbered array indexes contain the start position, and
                // the odd numbered indexes contain the end position
                int splitDomainStart = 0;
                for (int i = 0; i < segments.length; i++)   {
                    int number = Integer.valueOf(segments[i]);
                    int splitDomainEnd;
                    // Even numbers (array index 0, 2, 4 ...etc) = start
                    if (i % 2 == 0)    {
                        splitDomainStart = number;
                    }
                    // Odd numbers (array index 1, 3, 5 ...etc) = end
                    else    {
                        splitDomainEnd = number;
                        // Create match for each split domain
                        Gene3dHmmer3RawMatch match = new Gene3dHmmer3RawMatch(
                                m.getSequenceIdentifier(),
                                m.getModel(),
                                m.getSignatureLibraryRelease(),
                                splitDomainStart,
                                splitDomainEnd,
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
        }
    }

    public void setTemporaryFilePath(String temporaryFilePath) {
        this.temporaryFilePath = temporaryFilePath;
    }

    private Resource createTemporaryResource(String suffix)  {
        try {
            File file;
            if (temporaryFilePath == null)  {
                file = File.createTempFile("ipr-", suffix);
            }
            else    {
                file = new File(temporaryFilePath + suffix);
            }
            return new FileSystemResource(file);
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


}