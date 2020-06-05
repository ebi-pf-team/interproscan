package uk.ac.ebi.interpro.scan.io.prosite;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.AbstractModelFileParser;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to parse Prosite dat files and HAMAP prf files, so Signatures / Models can be loaded into I5.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PrositeDatFileParser extends AbstractModelFileParser {

    private static final Logger LOGGER = LogManager.getLogger(PrositeDatFileParser.class.getName());

    private static final String END_OF_RECORD = "//";

    private static final Pattern ID_LINE_ALL = Pattern.compile("^ID\\s+([^;]+);.*$");
    private static final Pattern ID_LINE_PROFILE = Pattern.compile("^ID\\s+([^;]+);\\s*MATRIX\\.\\s*$");
    private static final Pattern ID_LINE_PATTERN = Pattern.compile("^ID\\s+([^;]+);\\s*PATTERN\\.\\s*$");
    private static final Pattern ACCESSION_PATTERN = Pattern.compile("^AC\\s+([^;]+);.*$");
    private static final Pattern DESC_LINE = Pattern.compile("^DE\\s+(.*)$");

    public enum PrositeModelType {
        PATTERNS(ID_LINE_PATTERN),
        PROFILES(ID_LINE_PROFILE),
        ALL(ID_LINE_ALL),;

        private Pattern idLinePattern;

        PrositeModelType(Pattern idLinePattern) {
            this.idLinePattern = idLinePattern;
        }

        public Pattern getIdLinePattern() {
            return idLinePattern;
        }
    }

    private PrositeModelType prositeModelType;

    @Required
    public void setPrositeModelType(PrositeModelType prositeModelType) {
        this.prositeModelType = prositeModelType;
    }

    /**
     * Method to parse a model file and return a SignatureLibraryRelease.
     *
     * @return a complete SignatureLibraryRelease object
     */
    @Override
    public SignatureLibraryRelease parse() throws IOException {
        LOGGER.debug("Starting to parse hmm file.");
        SignatureLibraryRelease release = new SignatureLibraryRelease(library, releaseVersion);

        for (Resource modelFile : modelFiles) {
            BufferedReader reader = null;
            try {
                String accession = null, id = null, description = null;

                reader = new BufferedReader(new InputStreamReader(modelFile.getInputStream()));
                int lineNumber = 0;
                String line;
                while ((line = reader.readLine()) != null) {
                    if (LOGGER.isDebugEnabled() && lineNumber++ % 10000 == 0) {
                        LOGGER.debug("Parsed " + lineNumber + " lines of the HMM file.");
                        LOGGER.debug("Parsed " + release.getSignatures().size() + " signatures.");
                    }
                    line = line.trim();
                    // Speed things up a LOT - there are lots of lines we are not
                    // interested in parsing, so just check the first char of each line
                    if (line.length() > 0) {
                        switch (line.charAt(0)) {
                            case '/':
                                // Looks like an end of record marker - just to check:
                                if (END_OF_RECORD.equals(line.trim())) {
                                    if (accession != null && id != null) {
                                        release.addSignature(createSignature(accession, id, description, release));
                                    }
                                    accession = null;
                                    id = null;
                                    description = null;
                                }
                                break;
                            case 'I':
                                if (id == null) {
                                    id = extractValue(prositeModelType.getIdLinePattern(), line, 1);
                                }
                                break;
                            case 'A':
                                if (accession == null) {
                                    accession = extractValue(ACCESSION_PATTERN, line, 1);
                                }
                                break;
                            case 'D':
                                if (description == null) {
                                    description = extractValue(DESC_LINE, line, 1);
                                }
                                break;
                        }
                    }
                }
            }
            finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }
        return release;
    }

    private String extractValue(Pattern pattern, String line, int groupNumber) {
        Matcher matcher = pattern.matcher(line);
        return (matcher.matches())
                ? matcher.group(groupNumber)
                : null;
    }

    private Signature createSignature(String accession, String name, String description, SignatureLibraryRelease release) {
        Model model = new Model(accession, name, description);
        return new Signature(accession, name, null, description, null, release, Collections.singleton(model));
    }
}
