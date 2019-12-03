package uk.ac.ebi.interpro.scan.io.superfamily.model;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.io.AbstractModelFileParser;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse SuperFamily model.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class SuperFamilyModelParser extends AbstractModelFileParser {

    /*
     * NOTE: Didn't extend HmmerModelParser because of these irregularities:
     * - Need to append SSF on the front of the signature accession.
     * - In the SuperFamily hmmlib file, NAME refers to the model Id - not the signature name!
     * - Signatures can contain multiple models described in various records - can't just focus on 1 record at a time.
     */

    private static final Logger LOGGER = Logger.getLogger(SuperFamilyModelParser.class.getName());

    /**
     * In SuperFamily this represents the model Id.
     */
    private static final Pattern NAME_LINE = Pattern.compile("^NAME\\s+(.+)$");

    /**
     * In SuperFamily this represents the signature name and model name.
     */
    private static final Pattern DESC_LINE = Pattern.compile("^DESC\\s+(.+)$");

    /**
     * Matches the signature accession line (but needs "SSF" prefix adding) and if there is a version number it will be
     * stripped off (but doesn't really apply here).
     */
    private static final Pattern ACCESSION_PATTERN = Pattern.compile("^ACC\\s+([A-Z0-9]+)\\.?.*$");

    private static final Pattern LENGTH_LINE = Pattern.compile("^LENG\\s+([0-9]+)$");

    private static final String END_OF_RECORD = "//";

    private final Map<String, Signature> signatures = new HashMap<>();


    @Transactional
    public SignatureLibraryRelease parse() throws IOException {
        LOGGER.debug("Starting to parse hmm file.");
        SignatureLibraryRelease release = new SignatureLibraryRelease(library, releaseVersion);

        for (Resource modelFile : modelFiles) {
            BufferedReader reader = null;
            try {
                String accession = null, name = null, description = null;
                Integer length = null;

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
                                if (END_OF_RECORD.equals(line)) {
                                    processRecord(release, accession, name, description, length);
                                    accession = null;
                                    name = null;
                                    description = null;
                                    length = null;
                                }
                                break;
                            case 'A':
                                if (accession == null) {
                                    accession = extractValue(ACCESSION_PATTERN, line, 1);
                                    if (accession != null && !accession.startsWith("SSF")) {
                                        // In the SuperFamily HMM file, the accession is stored as "ACC   81321".
                                        // Need to prefix the accession with "SSF" to make "SSF81321" as required by InterPro!
                                        accession = "SSF" + accession;
                                    }
                                }
                                break;
                            case 'D':
                                if (description == null) {
                                    description = extractValue(DESC_LINE, line, 1);
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
                processRecord(release, accession, name, description, length);

            } finally {
                if (reader != null) {
                    reader.close();
                }
            }

            // Now add the signatures to the release
            for (Signature signature : signatures.values()) {
                release.addSignature(signature);
            }
        }

        return release;
    }

    /**
     * Add record details to the signatures object. Could involve adding a new signature and model to the existing
     * signatures object, or just adding a new model to an existing signature.
     *
     * @param release     Signature library release details
     * @param accession   Signature accession, if NULL then this method does nothing
     * @param name        Model Id
     * @param description Signature and model names
     * @param length      Model HMM length
     */
    private void processRecord(SignatureLibraryRelease release, String accession, String name, String description, Integer length) {
        if (accession != null) {
            Model model = new Model(name, description, null, length);
            if (signatures.containsKey(accession)) {
                Signature signature = signatures.get(accession);
                signature.addModel(model);
            } else {
                Signature signature = new Signature(accession, description, null, null, null, release, new HashSet<>());
                signature.addModel(model);
                signatures.put(accession, signature);
            }
        }
    }

    private String extractValue(Pattern pattern, String line, int groupNumber) {
        Matcher matcher = pattern.matcher(line);
        return (matcher.matches())
                ? matcher.group(groupNumber)
                : null;
    }


}
