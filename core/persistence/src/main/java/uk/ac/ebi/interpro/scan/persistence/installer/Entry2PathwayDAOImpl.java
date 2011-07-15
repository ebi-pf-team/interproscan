package uk.ac.ebi.interpro.scan.persistence.installer;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.interpro.scan.model.GoXref;
import uk.ac.ebi.interpro.scan.model.PathwayXref;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Represents the implementation class of {@link Entry2PathwayDAOImpl}.
 * The Repository annotation makes it a candidate for component-scanning.
 * <p/>
 * The entry2pathway table version 33.0 contained 12.852 entries at the moment.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Repository
public class Entry2PathwayDAOImpl implements Entry2PathwayDAO {

    private static final Logger LOGGER = Logger.getLogger(Entry2PathwayDAOImpl.class.getName());

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Resource
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public Entry2PathwayDAOImpl() {
    }

    public Collection<PathwayXref> getPathwayXrefsByEntryAc(String entryAc) {
        List<PathwayXref> result = null;
        try {
            SqlParameterSource namedParameters = new MapSqlParameterSource("entry_ac", entryAc);
            result = this.jdbcTemplate
                    .query("SELECT DBCODE, AC, NAME FROM INTERPRO.ENTRY2PATHWAY WHERE ENTRY_AC = :entry_ac",
                            namedParameters,
                            new RowMapper<PathwayXref>() {
                                public PathwayXref mapRow(ResultSet rs, int rowNum) throws SQLException {
                                    String name = rs.getString("name");
                                    String identifier = rs.getString("ac");
                                    String dbcode = rs.getString("dbcode");
                                    return new PathwayXref(dbcode, identifier, name);
                                }
                            });

        } catch (Exception e) {
            LOGGER.warn("Could not perform database query. It might be that the JDBC connection could not build " +
                    "or is wrong configured. For more info take a look at the stack trace!", e);
        }
        return result;
    }

    public Map<String, Collection<PathwayXref>> getAllPathwayXrefs() {
        final Map<String, Collection<PathwayXref>> result = new HashMap<String, Collection<PathwayXref>>();
        try {
            this.jdbcTemplate
                    .query("SELECT * FROM INTERPRO.ENTRY2PATHWAY",
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

    public Map<String, Collection<PathwayXref>> getPathwayXrefs(Collection<String> entryAccessions) {
        final Map<String, Collection<PathwayXref>> result = new HashMap<String, Collection<PathwayXref>>();
        try {
            SqlParameterSource namedParameters = new MapSqlParameterSource("accessions", entryAccessions);
            this.jdbcTemplate
                    .query("SELECT * FROM INTERPRO.ENTRY2PATHWAY WHERE ENTRY_AC in (:accessions)",
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

    private void addNewXref(ResultSet rs, final Map<String, Collection<PathwayXref>> result) throws SQLException {
        String entryAcc = rs.getString("entry_ac");
        String name = rs.getString("name");
        String identifier = rs.getString("ac");
        String dbcode = rs.getString("dbcode");
        PathwayXref newPathway = new PathwayXref(dbcode, identifier, name);
        List<PathwayXref> pathways = (List<PathwayXref>) result.get(entryAcc);
        if (pathways == null) {
            pathways = new ArrayList<PathwayXref>();
        }
        pathways.add(newPathway);
        result.put(entryAcc, pathways);
    }
}