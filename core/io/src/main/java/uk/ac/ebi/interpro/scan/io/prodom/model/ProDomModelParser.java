package uk.ac.ebi.interpro.scan.io.prodom.model;

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
 * Parses a prodom.ipr file and creates Signature / Method objects appropriately.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ProDomModelParser extends AbstractModelFileParser {


    /*
     * Example prodom.ipr input file:
     *
     * >IGJ_RABIT#PD021296#1#136 | 136 | pd_PD021296;sp_IGJ_RABIT_P23108; | (6)  J IMMUNOGLOBULIN CHAIN GLYCOPROTEIN SEQUENCING DIRECT IGJ_PREDICTED ACID PYRROLIDONE CARBOXYLIC
     * EDESTVLVDNKCQCVRITSRIIRDPDNPSEDIVERNIRIIVPLNTRENISDPTSPLRTE
     * FKYNLANLCKKCDPTEIELDNQVFTASQSNICPDDDYSETCYTYDRNKCYTTLVPITHR
     * GGTRMVKATLTPDSCYPD
     * >IGJ_HUMAN#PD021296#2#137 | 136 | pd_PD021296;sp_IGJ_HUMAN_P01591; | (6)  J IMMUNOGLOBULIN CHAIN GLYCOPROTEIN SEQUENCING DIRECT IGJ_PREDICTED ACID PYRROLIDONE CARBOXYLIC
     * EDERIVLVDNKCKCARITSRIIRSSEDPNEDIVERNIRIIVPLNNRENISDPTSPLRTR
     * FVYHLSDLCKKCDPTEVELDNQIVTATQSNICDEDSATETCYTYDRNKCYTAVVPLVYG
     * GETKMVETALTPDACYPD
     *
     */
    private static final Logger LOGGER = LogManager.getLogger(ProDomModelParser.class.getName());

    private static final Pattern LINE_PATTERN = Pattern.compile("^>");
    private static final Pattern ACCESSION_PATTERN = Pattern.compile("^pd_PD\\d+;sp_");
    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("^\\(\\d+\\)\\s+");


    @Transactional
    public SignatureLibraryRelease parse() throws IOException {
        LOGGER.debug("Starting to parse prodom.ipr file.");
        SignatureLibraryRelease release = new SignatureLibraryRelease(library, releaseVersion);

        for (Resource modelFile : modelFiles) {
            BufferedReader reader = null;
            try {
                StringBuffer modelBuffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(modelFile.getInputStream()));
                int lineNumber = 0;
                String line;
                while ((line = reader.readLine()) != null) {
                    if (LOGGER.isDebugEnabled() && lineNumber++ % 10000 == 0) {
                        LOGGER.debug("Parsed " + lineNumber + " lines of the prodom.ipr file.");
                        LOGGER.debug("Parsed " + release.getSignatures().size() + " signatures.");
                    }

                    Matcher data = LINE_PATTERN.matcher(line);
                    if (data.find()) {
                        String accession = null;
                        String description = null;

                        // Load the model line by line into a temporary buffer.
                        line = line.trim();
                        modelBuffer.append(line);
                        modelBuffer.append('\n');

                        // Now parse the model line
                        String[] values = line.split("\\|");
                        int i = 0;
                        while (i < values.length) {
                            switch (i) {
                                case 2:
                                    // Accession
                                    // Example: PD021296
                                    String text = values[2]; // Example: pd_PD021296;sp_IGJ_RABIT_P23108;
                                    if (text == null) {
                                        LOGGER.warn("ProDom model parser could not extract the accession from NULL text "
                                                + " on line number " + lineNumber + " - so this can't be added to the database");
                                    }
                                    else {
                                        text = text.trim();
                                        Matcher accMatcher = ACCESSION_PATTERN.matcher(text);
                                        if (accMatcher.find()) {
                                            accession = text.substring(3, text.indexOf(';'));
                                        }
                                        else {
                                            LOGGER.warn("ProDom model parser could not extract the accession from this text: "
                                                    + text + " on line number " + lineNumber + " - so this can't be added to the database");
                                        }
                                    }
                                    break;
                                case 3:
                                    // Description
                                    // Example: J IMMUNOGLOBULIN CHAIN GLYCOPROTEIN ...
                                    String text2 = values[3]; // Example: (6) J IMMUNOGLOBULIN CHAIN GLYCOPROTEIN ...
                                    if (text2 == null) {
                                        LOGGER.warn("ProDom model parser could not extract the description from NULL text "
                                                + " on line number " + lineNumber + " - so this can't be added to the database");
                                    }
                                    else {
                                        text2 = text2.trim();
                                        Matcher descMatcher = DESCRIPTION_PATTERN.matcher(text2);
                                        int index = text2.indexOf(')') + 1;
                                        if (descMatcher.find() && index < text2.length()) {
                                            description = text2.substring(index);
                                            description = description.trim();
                                        }
                                        else {
                                            LOGGER.warn("ProDom model parser could not extract the description from this text: "
                                                    + text2 + " on line number " + lineNumber + " - so this can't be added to the database");
                                        }
                                    }
                                    break;

                            }
                            i++;
                        }

                        // Now create the signature
                        if (accession != null) {
                            release.addSignature(createSignature(accession, null, description, release, modelBuffer));
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

    protected Signature createSignature(String accession, String name, String description, SignatureLibraryRelease release, StringBuffer modelBuffer) {
        Model model = new Model(accession, name, description);
        modelBuffer.delete(0, modelBuffer.length());
        return new Signature(accession, name, null, description, null, release, Collections.singleton(model));
    }


}
