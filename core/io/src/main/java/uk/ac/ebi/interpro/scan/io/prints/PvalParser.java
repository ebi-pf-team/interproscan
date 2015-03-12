package uk.ac.ebi.interpro.scan.io.prints;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
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
import java.util.Map;
import java.util.Set;

/**
 * Parser for pval files that contain PRINTS models. (Models in PRINTS parlance!)
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PvalParser extends AbstractModelFileParser {

    // /ebi/production/interpro/data/members/prints/40.0/print.pval

    private static final Logger LOGGER = Logger.getLogger(PvalParser.class.getName());

    private static final String LINE_SIG_NAME = "gc";
    private static final String LINE_SIG_ACCESSION = "gx";
    private static final String LINE_SIG_DESCRIPTION = "gi";

    private KdatParser kdatParser;


    @Required
    public void setKdatParser(KdatParser kdatParser) {
        this.kdatParser = kdatParser;
    }

    @Transactional
    public SignatureLibraryRelease parse() throws IOException {

        if (kdatParser == null) {
            throw new IllegalStateException("The Pval file data cannot be loaded unless there is a kdat file parser injected.");
        }
        Map<String, String> kdatFileData = kdatParser.parse();

        if (modelFiles == null) {
            throw new NullPointerException("Resource is null");
        }
        for (Resource modelFile: modelFiles) {
            if (!modelFile.exists()) {
                throw new IllegalStateException(modelFile.getFilename() + " does not exist");
            }
            if (!modelFile.isReadable()) {
                throw new IllegalStateException(modelFile.getFilename() + " is not readable");
            }
        }

        SignatureLibraryRelease release = new SignatureLibraryRelease(library, releaseVersion);

        for (Resource modelFile : modelFiles) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(modelFile.getInputStream()));
                String line, sigAcc = null, sigName = null, sigDescription = null;
                while ((line = reader.readLine()) != null) {
                    if (LOGGER.isDebugEnabled()) {
                        if (line.indexOf(';') > -1) {
                            LOGGER.debug(line);
                        }
                    }
                    if (line.startsWith(LINE_SIG_NAME)) {
                        createSignature(release, kdatFileData, sigAcc, sigName, sigDescription);

                        // Now clear / reset all local variables.
                        sigName = extractLineContent(line);
                        sigAcc = null;
                        sigDescription = null;
                    } else if (line.startsWith(LINE_SIG_ACCESSION)) {
                        sigAcc = extractLineContent(line);
                    } else if (line.startsWith(LINE_SIG_DESCRIPTION)) {
                        sigDescription = extractLineContent(line);
                    }
                }
                createSignature(release, kdatFileData, sigAcc, sigName, sigDescription);
            }
            finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }
        return release;
    }

    private String extractLineContent(String line) {
        return line.substring(3).trim();
    }

    private void createSignature(final SignatureLibraryRelease release, Map<String, String> kdatFileData, final String sigAcc, final String sigName, final String sigDescription) {
        if (sigName != null) {   // If sigName is null, this is the very first one in the file, so nothing to do.
            String sigAbstract = (storeAbstract) ? kdatFileData.get(sigAcc) : null;

            final Set<Model> model = Collections.singleton(
                    new Model(sigAcc, sigName, sigDescription)
            );

            release.addSignature(
                    new Signature(sigAcc, sigName, null, sigDescription, sigAbstract, release, model)
            );
        }
    }


}
