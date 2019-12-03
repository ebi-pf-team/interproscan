package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3;

import org.hibernate.procedure.internal.Util;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.ParseException;
import uk.ac.ebi.interpro.scan.io.match.MatchParser;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.DomainMatch;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.HmmSearchRecord;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.SequenceMatch;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for HMMER3 output, based upon the working parser used in Onion.
 * This has been designed to work as fast as possible, parsing a file
 * most of which is ignored.
 *
 * @author Phil Jones
 * @author David Binns
 * @author Antony Quinn
 * @version $Id$
 * @since 1.0-SNAPSHOT
 * <p>
 * <p>
 * Internal pipeline statistics summary:
 * -------------------------------------
 * Query model(s):                            1  (242 nodes)
 * Target sequences:                          1  (2923 residues)
 * Passed MSV filter:                         1  (1); expected 0.0 (0.02)
 * Passed bias filter:                        1  (1); expected 0.0 (0.02)
 */
public class Hmmer3MatchParser<T extends RawMatch> implements MatchParser {

    private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(Hmmer3MatchParser.class.getName());

    private static final String END_OF_RECORD = "//";

    /**
     * DON'T GET RID OF THIS!  If HMMER3 is working properly, this is used to
     * correctly parse the file.  At the moment, beta 3 contains a bug, so the inclusion
     * threshold line is useless.  The code below has a line commented out which can
     * easily be put back to use the inclusion threshold.
     */
//    private static final String END_OF_GOOD_SEQUENCE_MATCHES = "inclusion threshold";

    private static final String DOMAIN_SECTION_START = ">> ";

    private static final String DOMAIN_ALIGNMENT_SECTION_START = "  ==";

    private static final String START_OF_DOMAIN_ALIGNMENT_SECTION = "Alignments for each domain";

    private static final String END_OF_OUTPUT_FILE = "[ok]";

    //Output File to write Gene3D parser output in ssf format suitable for Domain Finder input
    //File ssfFile = new File("C:\\Manjula\\input_for_DF.txt");

    // Group 1: Uniparc protein accession
    private static final Pattern DOMAIN_SECTION_START_PATTERN = Pattern.compile("^>>\\s+(\\S+).*$");

    /**
     * This interface has a single method that
     * takes the HmmsearchOutputMethod object, containing sequence
     * and domain matches and converts it to RawProtein
     * objects.  The converter MAY perform additional steps, such as
     * filtering the raw results by specific criteria, such as GA value
     * cutoff.
     */
    private Hmmer3ParserSupport<T> hmmer3ParserSupport;

    private final SignatureLibrary signatureLibrary;
    private final String signatureLibraryRelease;

    private boolean useHmmsearch = true;

    private Hmmer3MatchParser() {
        signatureLibrary = null;
        signatureLibraryRelease = null;
    }

    public Hmmer3MatchParser(SignatureLibrary signatureLibrary, String signatureLibraryRelease) {
        this.signatureLibrary = signatureLibrary;
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    public void setUseHmmsearch(boolean useHmmsearch) {
        this.useHmmsearch = useHmmsearch;
    }

    public SignatureLibrary getSignatureLibrary() {
        return signatureLibrary;
    }

    public String getSignatureLibraryRelease() {
        return signatureLibraryRelease;
    }

    @Required
    public void setParserSupport(Hmmer3ParserSupport<T> hmmer3ParserSupport) {
        this.hmmer3ParserSupport = hmmer3ParserSupport;
    }

    public Set<RawProtein<T>> parse(InputStream is) throws IOException {
        //if i5 is running in single sequence mode then set this
        if (Utilities.isRunningInSingleSeqMode() && ! useHmmsearch) {
            Hmmer3ScanMatchParser hmmer3ScanMatchParser = new Hmmer3ScanMatchParser(signatureLibrary, signatureLibraryRelease);
            hmmer3ScanMatchParser.setParserSupport(hmmer3ParserSupport);
            return (hmmer3ScanMatchParser.parse(is));
        }
        Hmmer3SearchMatchParser hmmer3ScanMatchParser = new Hmmer3SearchMatchParser(signatureLibrary, signatureLibraryRelease);
        hmmer3ScanMatchParser.setParserSupport(hmmer3ParserSupport);
        return (hmmer3ScanMatchParser.parse(is));

    }
}
