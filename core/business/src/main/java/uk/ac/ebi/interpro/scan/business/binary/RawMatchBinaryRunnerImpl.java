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
package uk.ac.ebi.interpro.scan.business.binary;

import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.io.match.MatchParser;

import java.util.Set;
import java.io.IOException;

/**
 * Runs binary on FASTA file and parses results.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public final class RawMatchBinaryRunnerImpl<T extends RawMatch>
        extends AbstractBinaryRunner
        implements RawMatchBinaryRunner<T> {

    // TODO: Write unit tests

    private MatchParser<T> parser;

    /**
     * Returns result of running binary on FASTA file with given models.
     *
     * @param  fastaFile FASTA file
     * @param  modelFile Model file, for example HMMs
     * @return result of running binary on FASTA file with given models
     * @throws IOException if cannot run binary
     */
    public Set<RawProtein<T>> process(Resource fastaFile, Resource modelFile) throws IOException {

        final String additionalArguments =
                new StringBuilder()
                        .append(modelFile.getFile().getAbsolutePath())
                        .append(' ')
                        .append(fastaFile.getFile().getAbsolutePath())
                        .toString();
      
        return parser.parse(run(additionalArguments));

    }

    @Override public void setParser(MatchParser<T> parser) {
        this.parser = parser;
    }

// To get fragments of HMM files:
    // grep -n <ID> <file>
    // tail -n +linenum <file> | head -n numlines    

}
