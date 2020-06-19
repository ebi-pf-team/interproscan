package uk.ac.ebi.interpro.scan.persistence.installer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.interpro.scan.model.GoCategory;
import uk.ac.ebi.interpro.scan.model.GoXref;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Represents the implementation class of {@link uk.ac.ebi.interpro.scan.persistence.installer.Entry2GoDAOImpl}.
 * The Repository annotation makes it a candidate for component-scanning.
 * <p/>
 * The entry2pathway table version 33.0 contained 12.852 entries at the moment.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Repository
public class Entry2GoDAOImpl implements Entry2GoDAO {

    private static final Logger LOGGER = LogManager.getLogger(Entry2GoDAOImpl.class.getName());

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Resource
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public Entry2GoDAOImpl() {
    }

    public Collection<GoXref> getGoXrefsByEntryAc(String entryAc) {
        List<GoXref> result = null;
        try {
            SqlParameterSource namedParameters = new MapSqlParameterSource("entry_ac", entryAc);
            result = this.jdbcTemplate
                    .query("SELECT i2g.entry_ac, g.go_id, g.name, g.category from INTERPRO.INTERPRO2GO i2g JOIN go.terms@GOAPRO g ON i2g.go_id = g.go_id WHERE i2g.entry_ac = :entry_ac",
                            namedParameters,
                            new RowMapper<GoXref>() {
                                public GoXref mapRow(ResultSet rs, int rowNum) throws SQLException {
                                    String identifier = rs.getString("go_id");
                                    String name = rs.getString("name");
                                    String nameCode = rs.getString("category");
                                    GoCategory category = GoCategory.parseNameCode(nameCode);
                                    return new GoXref(identifier, name, category);
                                }
                            });

        } catch (Exception e) {
            LOGGER.warn("Could not perform database query. It might be that the JDBC connection could not build " +
                    "or is wrong configured. For more info take a look at the stack trace!", e);
        }
        return result;
    }

    public Map<String, Collection<GoXref>> getAllGoXrefs() {
        final Map<String, Collection<GoXref>> result = new HashMap<String, Collection<GoXref>>();
        try {
            this.jdbcTemplate
                    .query("SELECT i2g.entry_ac, g.go_id, g.name, g.category from INTERPRO.INTERPRO2GO i2g INNER JOIN INTERPRO.ENTRY e ON (e.entry_ac = i2g.entry_ac) JOIN go.terms@GOAPRO g ON i2g.go_id = g.go_id WHERE e.checked='Y'",
                            new MapSqlParameterSource(),
                            new RowCallbackHandler() {
                                @Override
                                public void processRow(ResultSet rs) throws SQLException {
                                    addNewXref(rs, result);
                                }
                            });

        } catch (Exception e) {
            LOGGER.warn("Could not perform database query. It might be that the JDBC connection could not build " +
                    "or is wrong configured. For more info take a look at the stack trace!", e);
        }
        return result;
    }

    public Map<String, Collection<GoXref>> getGoXrefs(Collection<String> entryAccessions) {
        final Map<String, Collection<GoXref>> result = new HashMap<String, Collection<GoXref>>();
        try {
            SqlParameterSource namedParameters = new MapSqlParameterSource("accessions", entryAccessions);
            this.jdbcTemplate
                    .query("SELECT i2g.entry_ac, g.go_id, g.name, g.category from INTERPRO.INTERPRO2GO i2g JOIN go.terms@GOAPRO g ON i2g.go_id = g.go_id WHERE i2g.entry_ac in (:accessions)",
                            namedParameters,
                            new RowCallbackHandler() {
                                @Override
                                public void processRow(ResultSet rs) throws SQLException {
                                    addNewXref(rs, result);
                                }
                            });

        } catch (Exception e) {
            LOGGER.warn("Could not perform database query. It might be that the JDBC connection could not build " +
                    "or is wrong configured. For more info take a look at the stack trace!", e);
        }
        return result;
    }

    private void addNewXref(ResultSet rs, final Map<String, Collection<GoXref>> result) throws SQLException {
        String entryAc = rs.getString("entry_ac");
        String identifier = rs.getString("go_id");
        String name = rs.getString("name");
        String nameCode = rs.getString("category");
        GoCategory category = GoCategory.parseNameCode(nameCode);
        GoXref newGoXRef = new GoXref(identifier, name, category);
        Set<GoXref> goXrefs = (Set<GoXref>) result.get(entryAc);
        if (goXrefs == null) {
            goXrefs = new HashSet<GoXref>();
        }
        goXrefs.add(newGoXRef);
        result.put(entryAc, goXrefs);
    }
}
