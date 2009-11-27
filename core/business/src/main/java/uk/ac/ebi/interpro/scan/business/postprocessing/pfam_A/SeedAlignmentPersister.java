package uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A;

import uk.ac.ebi.interpro.scan.io.ParseException;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.io.Serializable;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Required;

/**
 * This class loads Pfam seed alignment data into the Onion database
 * to be used for Pfam (HMMER3) post processing.
 *
 * @author Phil Jones
 * @version $Id: SeedAlignmentPersister.java,v 1.3 2009/11/09 13:34:28 craigm Exp $
 * @since 1.0
 */
public class SeedAlignmentPersister implements Serializable {

    private static Logger LOGGER = Logger.getLogger(SeedAlignmentPersister.class);

    private String releaseNumber;

    private Resource pfamASeedFile;

    private Resource modelTextFile;

    private static final Pattern MODEL_ACCESSION_EXTRACTOR_PATTERN = Pattern.compile("^\\#=GF\\s+AC\\s+([A-Z0-9]+).*$");

    /**
     *  Group[0] "#=GS TDXH_AERPE/160-203    AC Q9Y9L0.1" at 650 - 689
     *
     * Group[1] "160"          Start coordinate
     * Group[2] "203"          Stop coordinate
     * Group[3] "Q9Y9L0"       UniProt Ac
     * Group[4] "1"            UniProt Ac version number
     */
    private static final Pattern SEED_ALIGNMENT_EXTRACTOR_PATTERN = Pattern.compile("^\\#=GS\\s+.+?/(\\d+)-(\\d+)\\s+AC\\s+([A-Z0-9]+)\\.(\\d+)$");

    private static final String MODEL_ACCESSION_LINE_START = "#=GF AC";

    private static final String SEED_LINE_START            = "#=GS ";

    private static final String RECORD_END                 = "//";

    @Required
    public void setReleaseNumber(String releaseNumber) {
        this.releaseNumber = releaseNumber;
    }

    @Required
    public void setPfamASeedFile(Resource pfamASeedFile) {
        this.pfamASeedFile = pfamASeedFile;
    }

    @Required
    public void setModelTextFile(Resource modelTextFile) {
        this.modelTextFile = modelTextFile;
    }

    public void load() throws SQLException, IOException, ParseException {
        loadNewSeedAlignments();
        mapUniProtToMD5();
        LOGGER.error("Successful completion of Pfam "+ releaseNumber + " seed alignment loading.\nThis includes:\n1. Truncation of old data.\n2. Loading of seed alignment data from " + pfamASeedFile.getFile().getAbsolutePath() + " file.\n3. Updating of UniParc cross references.\n4. Analyze table to re-create indices.");
    }


    private void loadNewSeedAlignments() throws IOException, ParseException, SQLException {

        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(modelTextFile.getFile()));
            String modelAc = null;
            int lineNumber = 0;
            while (reader.ready()){
                lineNumber++;
                String line = reader.readLine();
                if (line.startsWith(RECORD_END)){
                    modelAc = null;
                }
                if (line.startsWith(MODEL_ACCESSION_LINE_START)){
                    Matcher modelAccessionMatcher =  MODEL_ACCESSION_EXTRACTOR_PATTERN.matcher(line);
                    if (modelAccessionMatcher.find()){
                        modelAc = modelAccessionMatcher.group(1);
                    }
                }
                if (line.startsWith(SEED_LINE_START)){
                    Matcher seedMatcher = SEED_ALIGNMENT_EXTRACTOR_PATTERN.matcher(line);
                    if (seedMatcher.matches()){
                        if (modelAc == null){
                            throw new ParseException("The Pfam.seed file contains an entry that does not appear to have a model accession.",modelTextFile.getFile().getAbsolutePath(), line, lineNumber);
                        }
                        final int startCoordinate     = Integer.parseInt( seedMatcher.group(1));
                        final int stopCoordinate      = Integer.parseInt( seedMatcher.group(2));
                        final String uniprotAc        =                   seedMatcher.group(3);
                        final int versionNumber       = Integer.parseInt( seedMatcher.group(4));

                        final SeedAlignment.ForPersistence seedAlign =
                                new SeedAlignment.ForPersistence(
                                        modelAc,
                                        uniprotAc,
                                        versionNumber,
                                        startCoordinate,
                                        stopCoordinate,
                                        releaseNumber);
                        //TODO - Store the seed alignment somehow?
                    }
                }
            }
        }
        finally{
            if (reader != null){
                reader.close();
            }
        }
    }

    /**
     * SQL statement to add UniParc accessions to the PFAM_SEEDS_HMMER3 table.
     * @throws SQLException in the event of a database level error.
     */
    private void mapUniProtToMD5() throws SQLException {
        // TODO
    }
}
