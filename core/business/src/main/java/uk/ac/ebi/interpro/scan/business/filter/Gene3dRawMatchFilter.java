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

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.io.gene3d.DomainFinderResourceWriter;
import uk.ac.ebi.interpro.scan.io.gene3d.DomainFinderRecord;
import uk.ac.ebi.interpro.scan.io.gene3d.DomainFinderResourceReader;
import uk.ac.ebi.interpro.scan.business.binary.SimpleBinaryRunner;
import uk.ac.ebi.interpro.scan.business.binary.BinaryRunner;

import java.util.Set;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashSet;
import java.io.IOException;
import java.io.File;


/**
 * Runs post-processing on raw results and parses results.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public class Gene3dRawMatchFilter implements RawMatchFilter<Gene3dHmmer3RawMatch> {

    // TODO: Write unit tests    
    
    BinaryRunner binaryRunner = new SimpleBinaryRunner();

    public void setBinaryRunner(BinaryRunner binaryRunner) {
        this.binaryRunner = binaryRunner;
    }

    public Set<RawProtein<Gene3dHmmer3RawMatch>> filter(Set<RawProtein<Gene3dHmmer3RawMatch>> rawProteins) {

        // Generate SSF file
        // TODO: Put this into DomainFinderRecord.valueOf
        Collection<DomainFinderRecord> records = new ArrayList<DomainFinderRecord>();
        for (RawProtein<Gene3dHmmer3RawMatch> p : rawProteins)    {
            for (Gene3dHmmer3RawMatch m : p.getMatches())   {
                records.add(DomainFinderRecord.valueOf(m));
            }
        }
        Resource ssfInputFile = createTemporaryResource(".input.ssf");
        DomainFinderResourceWriter writer = new DomainFinderResourceWriter();
        try {
            writer.write(ssfInputFile, records);
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }

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

        // Parse DF3 results
        DomainFinderResourceReader resourceReader = new DomainFinderResourceReader();
        Collection<DomainFinderRecord> results;
        try {
            results = resourceReader.read(ssfOutputFile);
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }

        // Delete
        if (binaryRunner.isDeleteTemporaryFiles())   {
            try {
                if (!ssfInputFile.getFile().delete())   {
                    System.err.println("Could not delete " + ssfInputFile.getDescription());
                }
                if (!ssfOutputFile.getFile().delete())   {
                    System.err.println("Could not delete " + ssfOutputFile.getDescription());
                }
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }        

        // Update raw matches with values from DomainFinder
        // TODO: Put this into DomainFinderRecord
        final Set<RawProtein<Gene3dHmmer3RawMatch>> filteredRawProteins =
                new HashSet<RawProtein<Gene3dHmmer3RawMatch>>();
        for (RawProtein<Gene3dHmmer3RawMatch> p : rawProteins)    {
            RawProtein<Gene3dHmmer3RawMatch> rp =
                    new RawProtein<Gene3dHmmer3RawMatch>(p.getProteinIdentifier());
            for (Gene3dHmmer3RawMatch m : p.getMatches())   {
                if (rp.getProteinIdentifier().equals(m.getSequenceIdentifier()))    {
                    for (DomainFinderRecord r : results)    {
                        if (m.getModel().equals(r.getModelId()))    {
                            Gene3dHmmer3RawMatch match = new Gene3dHmmer3RawMatch(
                                    m.getSequenceIdentifier(),
                                    m.getModel(),
                                    m.getSignatureLibraryRelease(),
                                    m.getLocationStart(),                                                               
                                    m.getLocationEnd(),
                                    m.getEvalue(),  // TODO: Do we need to read this from DF3??
                                    m.getScore(),
                                    m.getHmmStart(),
                                    m.getHmmEnd(),
                                    m.getHmmBounds(),
                                    m.getLocationScore(),
                                    m.getEnvelopeStart(),
                                    m.getEnvelopeEnd(),
                                    m.getExpectedAccuracy(),
                                    m.getFullSequenceBias(),
                                    m.getDomainCeValue(),
                                    m.getDomainIeValue(),
                                    m.getDomainBias(),
                                    m.getCigarAlignment());
                            //rp.addMatch(match);
                            if (!rp.getMatches().contains(match))   {
                                rp.addMatch(match);
                            }
                        }
                    }
                }
            }
            if (!rp.getMatches().isEmpty()) {
                filteredRawProteins.add(rp);
            }
        }

        return filteredRawProteins;

     }

    private Resource createTemporaryResource(String suffix)  {
        try {
            return new FileSystemResource(File.createTempFile("ipr-", suffix));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


}