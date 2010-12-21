package uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.RowMapper;
import uk.ac.ebi.interpro.scan.io.ParseException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class loads Pfam seed alignment data into the Onion database
 * to be used for Pfam (HMMER3) post processing.
 *
 * @author Phil Jones
 * @version $Id: SeedAlignmentPersister.java,v 1.3 2009/11/09 13:34:28 craigm Exp $
 * @since 1.0
 */
public class SeedAlignmentPersister implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(SeedAlignmentPersister.class.getName());

    private Resource modelTextFile;

    private JdbcTemplate jdbcTemplate;


    @Required
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final Pattern MODEL_ACCESSION_EXTRACTOR_PATTERN = Pattern.compile("^\\#=GF\\s+AC\\s+([A-Z0-9]+).*$");

    /**
     * Group[0] "#=GS TDXH_AERPE/160-203    AC Q9Y9L0.1" at 650 - 689
     * <p/>
     * Group[1] "160"          Start coordinate
     * Group[2] "203"          Stop coordinate
     * Group[3] "Q9Y9L0"       UniProt Ac
     * Group[4] "1"            UniProt Ac version number
     */
    private static final Pattern SEED_ALIGNMENT_EXTRACTOR_PATTERN = Pattern.compile("^\\#=GS\\s+.+?/(\\d+)-(\\d+)\\s+AC\\s+([A-Z0-9]+)\\.(\\d+)$");

    private static final String MODEL_ACCESSION_LINE_START = "#=GF AC";

    private static final String SEED_LINE_START = "#=GS ";

    private static final String RECORD_END = "//";

    @Required
    public void setModelTextFile(Resource modelTextFile) {
        this.modelTextFile = modelTextFile;
    }

    public void loadNewSeedAlignments() throws IOException, SQLException {

        BufferedReader reader = null;
        final String sql = "select distinct p.md5 from uniparc.protein p inner join uniparc.xref on p.upi = xref.upi where xref.ac  = ? and xref.version = ?";
        final PreparedStatementCreatorFactory factory = new PreparedStatementCreatorFactory(sql, new int[]{Types.VARCHAR, Types.INTEGER});
        final RowMapper rowMapper = new RowMapper(){
            /**
             * Implementations must implement this method to map each row of data
             * in the ResultSet. This method should not call <code>next()</code> on
             * the ResultSet; it is only supposed to map values of the current row.
             *
             * @param rs     the ResultSet to map (pre-initialized for the current row)
             * @param rowNum the number of the current row
             * @return the result object for the current row
             * @throws java.sql.SQLException if a SQLException is encountered getting
             *                               column values (that is, there's no need to catch SQLException)
             */
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getString(1);
            }
        };
        try {
            reader = new BufferedReader(new FileReader(modelTextFile.getFile()));
            String modelAc = null;
            int lineNumber = 0;
            while (reader.ready()) {
                lineNumber++;
                String line = reader.readLine();
                if (line.startsWith(RECORD_END)) {
                    modelAc = null;
                }
                if (line.startsWith(MODEL_ACCESSION_LINE_START)) {
                    Matcher modelAccessionMatcher = MODEL_ACCESSION_EXTRACTOR_PATTERN.matcher(line);
                    if (modelAccessionMatcher.find()) {
                        modelAc = modelAccessionMatcher.group(1);
                    }
                }
                if (line.startsWith(SEED_LINE_START)) {
                    Matcher seedMatcher = SEED_ALIGNMENT_EXTRACTOR_PATTERN.matcher(line);
                    if (seedMatcher.matches()) {
                        if (modelAc == null) {
                            throw new ParseException("The Pfam.seed file contains an entry that does not appear to have a model accession.", modelTextFile.getFile().getAbsolutePath(), line, lineNumber);
                        }
                        final int startCoordinate = Integer.parseInt(seedMatcher.group(1));
                        final int stopCoordinate = Integer.parseInt(seedMatcher.group(2));
                        final String uniprotAc = seedMatcher.group(3);
                        final Integer versionNumber = Integer.parseInt(seedMatcher.group(4));
                        // Now query for the MD5 for this UniProt protein.
                        final Object[] params = new Object[]{uniprotAc, versionNumber};
                        List md5List = jdbcTemplate.query (factory.newPreparedStatementCreator(params), rowMapper);
                        if (md5List == null || md5List.size() == 0){
                            throw new IllegalStateException (String.format("%s.%d does not appear in UniParc.", uniprotAc, versionNumber));
                        }
                        if (md5List.size() > 1){
                            throw new IllegalStateException (String.format("%s.%d maps to %d sequences in UniParc.", uniprotAc, versionNumber, md5List.size()));
                        }
                        final SeedAlignment seedAlign =
                                new SeedAlignment(
                                        modelAc,
                                        (String) md5List.get(0),
                                        startCoordinate,
                                        stopCoordinate);
                        LOGGER.error(seedAlign);
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
}
