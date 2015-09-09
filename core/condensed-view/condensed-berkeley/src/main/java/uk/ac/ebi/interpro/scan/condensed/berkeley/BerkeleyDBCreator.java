package uk.ac.ebi.interpro.scan.condensed.berkeley;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import freemarker.template.TemplateException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.condensed.berkeley.model.BerkeleyCondensedMarkup;
import uk.ac.ebi.interpro.scan.management.model.implementations.writer.ProteinMatchesHTMLResultWriter;
import uk.ac.ebi.interpro.scan.web.io.AnalyseMatchDataResult;
import uk.ac.ebi.interpro.scan.web.io.MatchDataRecord;
import uk.ac.ebi.interpro.scan.web.model.SimpleProtein;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Phil Jones
 */
public class BerkeleyDBCreator {

    /**
     * Set the logging level for this class to "TRACE" and it will run for a very small set of proteins.
     * <p/>
     * Otherwise, it will run for the whole lot! (Beware - takes a LONG time to run and consumes LOTS of disk space).
     */
    private static final Logger LOGGER = Logger.getLogger(BerkeleyDBCreator.class.getName());

    private static final String MAX_UPI_FOR_TRACE_LOGGING = "UPI0000001000";

    private static final String CREATE_TEMP_TABLE =
            "create global temporary table condensed_tmp_tab " +
                    "on commit preserve rows " +
                    "as " +
                    "select distinct " +
                    "      up.md5, " +
                    "      e_m.entry_ac, " +
                    "      e.short_name as entry_short_name, " +
                    "      e.name as entry_name, " +
                    "      cv_et.abbrev as entry_type, " +
                    "      m.method_ac, " +
                    "      m.name as method_name, " +
                    "      m_db.dbname as method_database_name, " +
                    "      p_m.pos_from, " +
                    "      p_m.pos_to, " +
                    "      p_m.score as match_score, " +
                    "      p.len " +
                    "      from  " +
                    "      uniparc.protein up  " +
                    "      INNER JOIN interpro.protein p  on p.crc64 = up.crc64 " +
                    // If logging at trace level, the next line will be added to reduce massively the number of proteins included.

                    "      INNER JOIN uniparc.xref x on p.protein_ac = x.ac and x.upi = up.upi " +
                    "      INNER JOIN interpro.match p_m ON p.protein_ac = p_m.protein_ac " +
                    "      INNER JOIN interpro.method m ON p_m.method_ac = m.method_ac " +
                    "      INNER JOIN interpro.cv_database m_db ON m.dbcode = m_db.dbcode " +
                    "      INNER JOIN interpro.entry2method e_m on m.method_ac = e_m.method_ac " +
                    "      INNER JOIN interpro.entry e ON e_m.entry_ac = e.entry_ac " +
                    "      INNER JOIN interpro.cv_entry_type cv_et ON e.entry_type = cv_et.code " +
                    "WHERE x.deleted = 'N' " +
                    "AND e.entry_type in ('D','R') " +
                    "AND e.checked = 'Y'" +
                    ((BerkeleyDBCreator.LOGGER.isTraceEnabled()) ? " and e_m.entry_ac in ('IPR018957','IPR001849')" : "");

    private static final String QUERY_TEMP_TABLE =
            "select " +
                    "md5,  " +                      // 1  MD5
                    "entry_ac, " +                  // 2  entry_ac
                    "entry_short_name, " +          // 3  entry short name
                    "entry_name, " +                // 4  entry name
                    "entry_type, " +                // 5  entry type
                    "method_ac, " +                 // 6  method accession
                    "method_name, " +               // 7  method name
                    "method_database_name, " +      // 8  method database name
                    "pos_from, " +                  // 9  start coordinate
                    "pos_to, " +                    // 10 stop coordinate
                    "match_score, " +               // 11 score
                    "len " +                        // 12 length

                    "from condensed_tmp_tab ORDER BY md5, entry_ac, method_ac, pos_from, pos_to";

    private static final String TRUNCATE_TEMPORARY_TABLE =
            "truncate table condensed_tmp_tab";


    /**
     * This is Oracles equivalent to the highly verbose "drop table if exists condensed_tmp_tab" used in MySQL.
     */
    private static final String DROP_TEMPORARY_TABLE =
            "BEGIN " +
                    "   EXECUTE IMMEDIATE 'DROP TABLE condensed_tmp_tab'; " +
                    "EXCEPTION " +
                    "   WHEN OTHERS THEN " +
                    "      IF SQLCODE != -942 THEN " +
                    "         RAISE; " +
                    "      END IF; " +
                    "END;";

    private static final String MD5_TO_UNIPROT_AC =
            "select " +
                    "distinct up.md5, p.protein_ac " +
                    "from " +
                    "      interpro.protein p " +
                    "      INNER JOIN uniparc.xref x on p.protein_ac = x.ac and x.deleted='N'" +
                    "      INNER JOIN uniparc.protein up on p.crc64 = up.crc64 and x.upi = up.upi " +


                    ((BerkeleyDBCreator.LOGGER.isTraceEnabled()) ?
                            " WHERE x.ac in (" +
                                    "select p_m.protein_ac from interpro.match p_m  " +
                                    "      INNER JOIN interpro.method m ON p_m.method_ac = m.method_ac " +
                                    "      INNER JOIN interpro.entry2method e_m on m.method_ac = e_m.method_ac" +
                                    "  WHERE e_m.entry_ac in ('IPR018957','IPR001849'))" : "") +


                    "      order by up.md5";

    // If logging at trace level, the next line will be added to reduce massively the number of proteins included.


    private static final int MD5 = 1;
    private static final int ENTRY_AC = 2;
    private static final int ENTRY_SHORT_NAME = 3;
    private static final int ENTRY_NAME = 4;
    private static final int ENTRY_TYPE = 5;
    private static final int METHOD_ACCESSION = 6;
    private static final int METHOD_NAME = 7;
    private static final int METHOD_DATABASE_NAME = 8;
    private static final int START_COORDINATE = 9;
    private static final int STOP_COORDINATE = 10;
    private static final int SCORE = 11;
    private static final int LEN = 12;

    private AnalyseMatchDataResult analyser;

    private ProteinMatchesHTMLResultWriter htmlWriter;

    @Required
    public void setAnalyser(final AnalyseMatchDataResult analyser) {
        if (analyser == null) {
            throw new IllegalArgumentException("Trying to inject a null analyser!");
        } else {
            System.out.println("Analyser injected OK.");
        }
        this.analyser = analyser;
    }

    @Required
    public void setHtmlWriter(ProteinMatchesHTMLResultWriter htmlWriter) {
        this.htmlWriter = htmlWriter;
    }

    public void run(String[] args) {
        if (args.length < 4) {
            throw new IllegalArgumentException("Please provide the following arguments:\n\npath to berkeleyDB directory\nInterPro DB URL (jdbc:oracle:thin:@host:port:SID)\nInterPro DB username\nInterPro DB password");
        }
        String directoryPath = args[0];
        String onionDBUrl = args[1];
        String onionUsername = args[2];
        String onionPassword = args[3];
        buildDatabase(directoryPath,
                onionDBUrl,
                onionUsername,
                onionPassword
        );
    }

    void buildDatabase(String directoryPath, String interproDBUrl, String interproUsername, String interproPassword) {
        long startMillis = System.currentTimeMillis();
        Environment myEnv = null;
        EntityStore store = null;
        Connection interproConn = null;
        if (LOGGER.isTraceEnabled()) LOGGER.trace("Trace enabled - fine logging on.");


        try {
            // First, create the populate the temporary table before create the BerkeleyDB, to prevent timeouts.
            Class.forName("oracle.jdbc.OracleDriver");
            interproConn = DriverManager.getConnection(interproDBUrl, interproUsername, interproPassword);
            Statement statement = null;


            try {
                statement = interproConn.createStatement();
                statement.execute(DROP_TEMPORARY_TABLE);
            } finally {
                if (statement != null) {
                    statement.close();
                }
            }

            statement = null;
            try {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Temp table creation SQL:");
                    LOGGER.trace(CREATE_TEMP_TABLE);
                }
                statement = interproConn.createStatement();
                statement.execute(CREATE_TEMP_TABLE);
            } finally {
                if (statement != null) {
                    statement.close();
                }
            }

            long now = System.currentTimeMillis();
            System.out.println((now - startMillis) + " milliseconds to create the temporary table.");
            startMillis = now;


            // Retrieve mappings from MD5 to accession
            final Map<String, Set<String>> md5ToAccession = new HashMap<String, Set<String>>();
            ResultSet rs = null;
            System.out.println("About to map MD5s to UniProt Accessions");
            try {
                statement = interproConn.createStatement();
                rs = statement.executeQuery(MD5_TO_UNIPROT_AC);
                while (rs.next()) {
                    final String md5 = rs.getString(1);
                    final String ac = rs.getString(2);
                    final Set<String> accessions;
                    if (md5ToAccession.containsKey(md5)) {
                        accessions = md5ToAccession.get(md5);
                    } else {
                        accessions = new HashSet<String>();
                        md5ToAccession.put(md5, accessions);
                    }
                    accessions.add(ac);
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (statement != null) {
                    statement.close();
                }
            }
            now = System.currentTimeMillis();
            System.out.println((now - startMillis) + " milliseconds to map MD5s to UniProt Accessions.");
            startMillis = now;

            // Make sure that the berkeley database directory is present and writable.
            File berkeleyDBDirectory = new File(directoryPath);
            if (berkeleyDBDirectory.exists()) {
                if (!berkeleyDBDirectory.isDirectory()) {
                    throw new IllegalStateException("The path " + directoryPath + " already exists and is not a directory, as required for a Berkeley Database.");
                }
                File[] directoryContents = berkeleyDBDirectory.listFiles();
                if (directoryContents != null && directoryContents.length > 0) {
                    throw new IllegalStateException("The directory " + directoryPath + " already has some contents.  The " + BerkeleyDBCreator.class.getSimpleName() + " class is expecting an empty directory path name as argument.");
                }
                if (!berkeleyDBDirectory.canWrite()) {
                    throw new IllegalStateException("The directory " + directoryPath + " is not writable.");
                }
            } else if (!(berkeleyDBDirectory.mkdirs())) {
                throw new IllegalStateException("Unable to create Berkeley database directory " + directoryPath);
            }

            // Open up the Berkeley Database
            EnvironmentConfig myEnvConfig = new EnvironmentConfig();
            StoreConfig storeConfig = new StoreConfig();

            myEnvConfig.setAllowCreate(true);
            storeConfig.setAllowCreate(true);
            storeConfig.setTransactional(false);
            // Open the environment and entity store
            myEnv = new Environment(berkeleyDBDirectory, myEnvConfig);
            store = new EntityStore(myEnv, "EntityStore", storeConfig);

            PrimaryIndex<String, BerkeleyCondensedMarkup> primIDX = store.getPrimaryIndex(String.class, BerkeleyCondensedMarkup.class);

            PreparedStatement ps = interproConn.prepareStatement(QUERY_TEMP_TABLE);
            rs = ps.executeQuery();

            int matchCount = 0;
            int uniqueSequenceCount = 0;

            BerkeleyCondensedMarkup current = null;
            Collection<MatchDataRecord> records = null;
            while (rs.next()) {
                final String md5 = rs.getString(MD5);
                if (current == null || !md5.equals(current.getMd5())) {
                    if (current != null) {
                        // Not the first - process the previous one.
                        processSequence(primIDX, current, records);
                    }

                    current = new BerkeleyCondensedMarkup();
                    records = new HashSet<MatchDataRecord>();
                    current.setMd5(md5);
                    current.setUniprotAcs(md5ToAccession.get(md5));
                    if (LOGGER.isDebugEnabled()) {
                        if (uniqueSequenceCount++ % 100000 == 0) {
                            LOGGER.debug(uniqueSequenceCount + " Unique sequences.");
                        }
                    }
                }
                // Add the necessary data to the records collection

                // Note that fields that are not useful for creating the condensed view are set
                // to default or null as appropriate.
                records.add(new MatchDataRecord(
                        md5,
                        md5,
                        md5,
                        rs.getInt(LEN),
                        null,
                        rs.getString(METHOD_ACCESSION),
                        rs.getString(METHOD_NAME),
                        rs.getString(METHOD_DATABASE_NAME),
                        rs.getInt(START_COORDINATE),
                        rs.getInt(STOP_COORDINATE),
                        rs.getDouble(SCORE),
                        rs.getString(ENTRY_AC),
                        rs.getString(ENTRY_SHORT_NAME),
                        rs.getString(ENTRY_NAME),
                        rs.getString(ENTRY_TYPE),
                        0,
                        "",
                        "",
                        false
                ));
                if (LOGGER.isDebugEnabled()) {
                    if (matchCount++ % 200000 == 0) {
                        LOGGER.debug(matchCount + " matches.");
                    }
                }
            }
            if (current != null) {
                processSequence(primIDX, current, records);
            }
            now = System.currentTimeMillis();
            System.out.println((now - startMillis) + " milliseconds to query the temporary table and create the BerkeleyDB.");
            startMillis = now;


            // Truncate the temporary table
            statement = null;
            try {
                statement = interproConn.createStatement();
                statement.execute(TRUNCATE_TEMPORARY_TABLE);
            } finally {
                if (statement != null) {
                    statement.close();
                }
            }

            now = System.currentTimeMillis();
            System.out.println((now - startMillis) + " milliseconds to truncate the temporary table.");
            startMillis = now;

            // And drop the table
            statement = null;
            try {
                statement = interproConn.createStatement();
                statement.execute(DROP_TEMPORARY_TABLE);
            } finally {
                if (statement != null) {
                    statement.close();
                }
            }

            now = System.currentTimeMillis();
            System.out.println((now - startMillis) + " milliseconds to drop the temporary table.");

            System.out.println("Finished building BerkeleyDB.");
        } catch (DatabaseException dbe) {
            throw new IllegalStateException("Error opening the BerkeleyDB environment", dbe);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (store != null) {
                try {
                    store.close();
                } catch (DatabaseException dbe) {
                    System.out.println("Unable to close the BerkeleyDB connection.");
                }
            }

            if (myEnv != null) {
                try {
                    // Finally, close environment.
                    myEnv.close();
                } catch (DatabaseException dbe) {
                    System.out.println("Unable to close the BerkeleyDB environment.");
                }
            }
        }

    }

    private static final Pattern POINTLESS_WHITESPACE = Pattern.compile("\\s\\s+");

    private void processSequence(
            PrimaryIndex<String, BerkeleyCondensedMarkup> primIDX,
            BerkeleyCondensedMarkup berkeleyCondensedMarkup,
            Collection<MatchDataRecord> records
    ) throws IOException, TemplateException {
        // Obtain match data from resultset and build SimpleProtein, followed by CondensedView
        final SimpleProtein sp = analyser.createSimpleProtein(records);
        String html = htmlWriter.write(sp);
        // Get rid of excessive whitespace
        html = POINTLESS_WHITESPACE.matcher(html).replaceAll(" ");
        if (LOGGER.isTraceEnabled()) {
            System.out.println(html);
            System.out.println();
        }
        berkeleyCondensedMarkup.setHtml(html);
        // Store this
        primIDX.put(berkeleyCondensedMarkup);
    }
}
