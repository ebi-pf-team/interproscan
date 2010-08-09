package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer2;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.match.MatchParser;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Parser for HMMER 2 hmmpfam binary output.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 *        <p/>
 *        Example output (TIGRfam example):
 *        <p/>
 *        hmmpfam - search one or more sequences against HMM database
 *        HMMER 2.3.2 (Oct 2003)
 *        Copyright (C) 1992-2003 HHMI/Washington University School of Medicine
 *        Freely distributed under the GNU General Public License (GPL)
 *        - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 *        HMM file:                 /home/pjones/projects/i5_resources/support/data/tigrfam/TIGRFAMs_9.0_HMM.LIB.bin
 *        Sequence file:            temp/maple_20100806_132340769_7ny6/jobTIGRFAM/000000001001_000000002000.fasta
 *        - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 *        <p/>
 *        Query sequence: 1011
 *        Accession:      [none]
 *        Description:    [none]
 *        <p/>
 *        Scores for sequence family classification (score includes all domains):
 *        Model    Description                                    Score    E-value  N
 *        -------- -----------                                    -----    ------- ---
 *        TIGR01163 rpe: ribulose-phosphate 3-epimerase            504.4   5.5e-149   1
 *        <p/>
 *        Parsed for domains:
 *        Model    Domain  seq-f seq-t    hmm-f hmm-t      score  E-value
 *        -------- ------- ----- -----    ----- -----      -----  -------
 *        TIGR01163   1/1       5   216 ..     1   216 []   504.4 5.5e-149
 *        //
 *        <p/>
 *        Query sequence: 1012
 *        Accession:      [none]
 *        Description:    [none]
 *        <p/>
 *        Scores for sequence family classification (score includes all domains):
 *        Model           Description                             Score    E-value  N
 *        --------        -----------                             -----    ------- ---
 *        TIGR03593       yidC_nterm: membrane protein insertas   513.0   1.4e-151   1
 *        TIGR03592       yidC_oxa1_cterm: membrane protein ins   398.7   3.5e-117   1
 *        <p/>
 *        Parsed for domains:
 *        Model           Domain  seq-f seq-t    hmm-f hmm-t      score  E-value
 *        --------        ------- ----- -----    ----- -----      -----  -------
 *        TIGR03593         1/1       3   352 ..     1   398 []   513.0 1.4e-151
 *        TIGR03592         1/1     353   533 ..     1   231 []   398.7 3.5e-117
 *        //
 *        <p/>
 *        Query sequence: 1013
 *        Accession:      [none]
 *        Description:    [none]
 *        <p/>
 *        Scores for sequence family classification (score includes all domains):
 *        Model    Description                                    Score    E-value  N
 *        -------- -----------                                    -----    ------- ---
 *        [no hits above thresholds]
 *        <p/>
 *        Parsed for domains:
 *        Model    Domain  seq-f seq-t    hmm-f hmm-t      score  E-value
 *        -------- ------- ----- -----    ----- -----      -----  -------
 *        [no hits above thresholds]
 *        //
 */
public class HmmPfamParser<T extends RawMatch> implements MatchParser {

    private static final Logger LOGGER = Logger.getLogger(HmmPfamParser.class.getName());

    private static final String END_OF_RECORD = "//";

    private static final String DOMAIN_SECTION_START = "Parsed for domains:";

    /**
     * Enum of states that the parser may be in - used to minimise parsing time.
     */
    private enum ParsingStage {
        LOOKING_FOR_METHOD_ACCESSION,
        LOOKING_FOR_SEQUENCE_MATCHES,
        LOOKING_FOR_DOMAIN_SECTION,
        LOOKING_FOR_DOMAIN_DATA_LINE,
        PARSING_DOMAIN_ALIGNMENTS,
        FINISHED_SEARCHING_RECORD

    }

    private SignatureLibrary signatureLibrary;

    private String signatureLibraryRelease;

    public SignatureLibrary getSignatureLibrary() {
        return signatureLibrary;
    }

    @Required
    public void setSignatureLibrary(SignatureLibrary signatureLibrary) {
        this.signatureLibrary = signatureLibrary;
    }

    public String getSignatureLibraryRelease() {
        return signatureLibraryRelease;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @Override
    public Set<RawProtein<T>> parse(InputStream is) throws IOException {
        Map<String, RawProtein<T>> rawResults = new HashMap<String, RawProtein<T>>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            int lineNumber = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.startsWith(END_OF_RECORD)) {
                    // Process a complete record - store all sequence / domain scores
                    // for the method.
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return new HashSet<RawProtein<T>>(rawResults.values());
    }
}
