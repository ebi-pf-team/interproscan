package uk.ac.ebi.interpro.scan.io.model;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.Model;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Collections;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;

import org.apache.log4j.Logger;

/**
 * Parses an hmm file and creates Signature / Method objects
 * appropriately.
 * User: phil
 * Date: 11-Nov-2009
 * Time: 20:35:32
 */
public class Hmmer3ModelLoader implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(Hmmer3ModelLoader.class);

    private SignatureLibrary library;

    private String releaseVersion;

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
    private static final String END_OF_RECORD = "//";


    public Hmmer3ModelLoader(SignatureLibrary library, String releaseVersion){
        this.library = library;
        this.releaseVersion = releaseVersion;
    }

    @Transactional
    public SignatureLibraryRelease parse(String hmmFilePath) throws IOException {
        LOGGER.debug("Starting to parse hmm file.");
        SignatureLibraryRelease release = new SignatureLibraryRelease(library, releaseVersion);
        BufferedReader reader = null;
        try{
            String accession = null, name = null, description = null;
            StringBuffer modelBuf = new StringBuffer();

            reader = new BufferedReader (new FileReader(hmmFilePath));
            int lineNumber = 0;
            while (reader.ready()){
                if (LOGGER.isDebugEnabled() && lineNumber++ % 10000 == 0){
                    LOGGER.debug("Parsed " + lineNumber + " lines of the HMM file.");
                    LOGGER.debug("Parsed " + release.getSignatures().size() + " signatures.");
                }
                String line = reader.readLine();

                // Load the model line by line into a temporary buffer.
                // TODO - won't break anything, but needs some work.  Need to grab the hmm file header first!
                modelBuf.append(line);
                modelBuf.append('\n');
                // Speed things up a LOT - there are lots of lines we are not
                // interested in parsing, so just check the first char of each line
                if (line.length() > 0){
                    switch (line.charAt(0)){
                        case '/':
                            // Looks like an end of record marker - just to check:
                            if (END_OF_RECORD.equals(line.trim())){
                                if (accession != null){
                                    release.addSignature(createSignature(accession, name, description, release, modelBuf));
                                }
                                accession = null;
                                name = null;
                                description = null;
                            }
                            break;
                        case 'A':
                            if (accession == null){
                                accession = extractValue (ACCESSION_PATTERN, line, 1);
                            }
                            break;
                        case 'D':
                            if (description == null){
                                description = extractValue(DESC_LINE, line, 1);
                            }
                            break;
                        case 'N':
                            if (name == null){
                                name = extractValue(NAME_LINE, line, 1);
                            }
                            break;
                    }
                }
            }
            // Dont forget the last one, just in case that final end of record
            // marker is missing!
            if (accession != null){
                release.addSignature(createSignature(accession, name, description, release, modelBuf));
            }
        }
        finally {
            if (reader != null){
                reader.close();
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

    protected Signature createSignature(String accession, String name, String description, SignatureLibraryRelease release, StringBuffer modelBuf){
//        Model model = new Model(accession, name, description, modelBuf.toString());
        Model model = new Model(accession, name, description, null);
        modelBuf.delete(0, modelBuf.length());
        return new Signature(accession, name, null, description, null, release, Collections.singleton(model));
    }
}
