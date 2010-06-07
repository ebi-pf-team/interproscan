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

    private SignatureLibrary library;

    private String releaseVersion;

    private Map<String, KdatSignatureData> kdatFileData;

    public PvalParser(SignatureLibrary library, String releaseVersion) {
        this.library = library;
        this.releaseVersion = releaseVersion;
    }

    @Transactional
    public SignatureLibraryRelease parse(Map<String, KdatSignatureData> kdatFileData, String printsPvalFilePath) throws IOException {
        SignatureLibraryRelease release = new SignatureLibraryRelease(library, releaseVersion);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(printsPvalFilePath));
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }


        return release;
    }
}
