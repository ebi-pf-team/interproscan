package uk.ac.ebi.interpro.scan.io.prints;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * Parser for pval files that contain PRINTS models. (Models in PRINTS parlance!)
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PvalParser implements Serializable {

    // /ebi/production/interpro/data/members/prints/40.0/print.pval

    private static final Logger LOGGER = Logger.getLogger(PvalParser.class);

    private static final String LINE_SIG_NAME = "gc";
    private static final String LINE_SIG_ACCESSION = "gx";
    private static final String LINE_SIG_DESCRIPTION = "gi";
    private static final String LINE_SIG_MODEL_COUNT = "gn";
    private static final String LINE_MODEL_ACCESSION = "mx";
    private static final String LINE_MODEL_NAME = "mi";

    private SignatureLibrary library;

    private String releaseVersion;

    private Map<String, String> kdatFileData;

    public PvalParser(SignatureLibrary library, String releaseVersion) {
        this.library = library;
        this.releaseVersion = releaseVersion;
    }

    @Transactional
    public SignatureLibraryRelease parse(Map<String, String> kdatFileData, String printsPvalFilePath) throws IOException {
        SignatureLibraryRelease release = new SignatureLibraryRelease(library, releaseVersion);

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(printsPvalFilePath));
            String line, sigAcc = null, sigName = null, sigDescription = null,
                    modelAcc = null, modelName = null;
            // TODO - work out how to add individual models.
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(LINE_SIG_NAME)) {
                    createSignature(release, sigAcc, sigName, sigDescription, modelAcc, modelName);
                }
            }
            createSignature(release, sigAcc, sigName, sigDescription, modelAcc, modelName);
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
        return release;
    }

    private void createSignature(SignatureLibraryRelease release, String sigAcc, String sigName, String sigDescription, String modelAcc, String modelName) {

    }
}
