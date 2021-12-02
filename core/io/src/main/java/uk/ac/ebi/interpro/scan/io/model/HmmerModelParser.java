package uk.ac.ebi.interpro.scan.io.model;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;
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
 * Parses an hmm file and creates Signature / Method objects
 * appropriately.
 * User: phil
 * Date: 11-Nov-2009
 * Time: 20:35:32
 */
public class HmmerModelParser extends AbstractModelFileParser {

    private static final Logger LOGGER = LogManager.getLogger(HmmerModelParser.class.getName());

    /**
     * Matches the name line.
     * Group 1: Model name.
     */
    private static final Pattern NAME_LINE = Pattern.compile("^NAME\\s+(.+)$");

    /**
     * Matches the Description line.
     * Group 1: Description.
     */
    private static final Pattern DESC_LINE = Pattern.compile("^DESC\\s+(.+)$");

    /**
     * Matches the Accession line.
     * Group 1: Accession with the version stripped off. (TODO - Is this the correct behaviour for all member databases?)
     */
    private static final Pattern ACCESSION_PATTERN = Pattern.compile("^ACC\\s+([A-Z0-9]+)\\.?.*$");

    private static final Pattern ACCESSION_PATTERN_PIRSR = Pattern.compile("^ACC\\s+(.+)$");

    private static final Pattern LENGTH_LINE = Pattern.compile("^LENG\\s+([0-9]+)$");

    private static final String END_OF_RECORD = "//";

    private static final String NON_ASCII = "[^\\x00-\\x7F]";

    @Transactional
    public SignatureLibraryRelease parse() throws IOException {
        LOGGER.debug("Starting to parse hmm file.");
        SignatureLibraryRelease release = new SignatureLibraryRelease(library, releaseVersion);

        for (Resource modelFile : modelFiles) {
            BufferedReader reader = null;
            try {
                String accession = null, name = null, description = null;
                Integer length = null;
                StringBuffer modelBuf = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(modelFile.getInputStream()));
                int lineNumber = 0;
                String line;
                while ((line = reader.readLine()) != null) {
                    if (LOGGER.isDebugEnabled() && lineNumber++ % 10000 == 0) {
                        LOGGER.debug("Parsed " + lineNumber + " lines of the HMM file.");
                        LOGGER.debug("Parsed " + release.getSignatures().size() + " signatures.");
                    }
                    line = line.trim();
                    // Load the model line by line into a temporary buffer.
                    // TODO - won't break anything, but needs some work.  Need to grab the hmm file header first!
                    modelBuf.append(line);
                    modelBuf.append('\n');
                    // Speed things up a LOT - there are lots of lines we are not
                    // interested in parsing, so just check the first char of each line
                    if (line.length() > 0) {
                        switch (line.charAt(0)) {
                            case '/':
                                // Looks like an end of record marker - just to check:
                                if (END_OF_RECORD.equals(line.trim())) {
                                    if (accession != null) {
                                        release.addSignature(createSignature(accession, name, description, length, release, modelBuf));
                                    } else if (name != null) {
                                        release.addSignature(createSignature(name, null, description, length, release, modelBuf));
                                    }
                                    accession = null;
                                    name = null;
                                    description = null;
                                    length = null;
                                }
                                break;
                            case 'A':
                                if (accession == null) {
                                    accession = extractValue(ACCESSION_PATTERN, line, 1);
                                    if (accession != null && accession.startsWith("PIRSR")){  //deal with the PIRSR specific pattern
                                        accession = extractValue(ACCESSION_PATTERN_PIRSR, line, 1);
                                    }
                                }
                                break;
                            case 'D':
                                if (description == null) {
                                    description = extractValue(DESC_LINE, line, 1);
                                    if (description != null && description.length() > 0) {
                                        description = description.replaceAll(NON_ASCII, "???"); // Replace unknown characters
                                    }
                                }
                                break;
                            case 'N':
                                if (name == null) {
                                    name = extractValue(NAME_LINE, line, 1);
                                }
                                break;
                            case 'L':
                                if (length == null) {
                                    length = Integer.parseInt(extractValue(LENGTH_LINE, line, 1));
                                }
                                break;
                        }
                    }
                }
                // Dont forget the last one, just in case that final end of record
                // marker is missing!
                if (accession != null) {
                    release.addSignature(createSignature(accession, name, description, length, release, modelBuf));
                } else if (name != null) {
                    release.addSignature(createSignature(name, null, description, length, release, modelBuf));
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

    protected Signature createSignature(String accession, String name, String description, int length, SignatureLibraryRelease release, StringBuffer modelBuf) {
//        Model model = new Model(accession, name, description, modelBuf.toString());
        Model model = new Model(accession, name, description, length);
        modelBuf.delete(0, modelBuf.length());
        return new Signature(accession, name, null, description, null, release, Collections.singleton(model));
    }


}
