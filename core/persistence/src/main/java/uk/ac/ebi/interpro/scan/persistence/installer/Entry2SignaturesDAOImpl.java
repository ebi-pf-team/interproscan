package uk.ac.ebi.interpro.scan.persistence.installer;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Represents the implementation class of {@link Entry2SignaturesDAO}.
 * The Repository annotation makes it a candidate for component-scanning.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Repository
public class Entry2SignaturesDAOImpl implements Entry2SignaturesDAO {

    private static final Logger LOGGER = Logger.getLogger(Entry2SignaturesDAOImpl.class.getName());

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Resource
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public Entry2SignaturesDAOImpl() {
    }

    public Collection<String> getSignaturesByEntryAc(String entryAc) {
        List<String> result = null;
        try {
            SqlParameterSource namedParameters = new MapSqlParameterSource("entry_ac", entryAc);
            result = this.jdbcTemplate
                    .query("SELECT METHOD_AC FROM INTERPRO.ENTRY2METHOD WHERE ENTRY_AC = :entry_ac",
                            namedParameters,
                            new RowMapper<String>() {
                                public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                                    return rs.getString("method_ac");
                                }
                            });

        } catch (Exception e) {
            LOGGER.warn("Could not perform database query. It might be that the JDBC connection could not build " +
                    "or is incorrectly configured. For more information take a look at the stack trace!", e);
        }
        return result;
    }

    public Map<String, Collection<String>> getAllSignatures() {
        final Map<String, Collection<String>> result = new HashMap<String, Collection<String>>();
        try {
            this.jdbcTemplate
                    .query("SELECT E2M.* FROM INTERPRO.ENTRY2METHOD E2M INNER JOIN INTERPRO.ENTRY E ON (E.ENTRY_AC = E2M.ENTRY_AC) WHERE E.CHECKED = 'Y'",
                            new MapSqlParameterSource(),
                            new RowCallbackHandler() {
                                @Override
                                public void processRow(ResultSet rs) throws SQLException {
                                    String entryAc = rs.getString("entry_ac");
                                    String methodAc = rs.getString("method_ac");
                                    Collection<String> signatures = result.get(entryAc);
                                    if (signatures == null) {
                                        signatures = new HashSet<String>();
                                    }
                                    signatures.add(methodAc);
                                    result.put(entryAc, signatures);
                                }
                            });

        } catch (Exception e) {
            LOGGER.warn("Could not perform database query. It might be that the JDBC connection could not build " +
                    "or is incorrectly configured. For more information take a look at the stack trace!", e);
        }
        return result;
    }

    public Map<String, Collection<String>> getSignatures(Collection<String> entryAccessions) {
        final Map<String, Collection<String>> result = new HashMap<String, Collection<String>>();
        try {
            SqlParameterSource namedParameters = new MapSqlParameterSource("accessions", entryAccessions);
            this.jdbcTemplate
                    .query("SELECT * FROM INTERPRO.ENTRY2METHOD WHERE ENTRY_AC in (:accessions)",
                            namedParameters,
                            new RowCallbackHandler() {
                                @Override
                                public void processRow(ResultSet rs) throws SQLException {
                                    String entryAc = rs.getString("entry_ac");
                                    String methodAc = rs.getString("method_ac");
                                    Collection<String> signatures = result.get(entryAc);
                                    if (signatures == null) {
                                        signatures = new ArrayList<String>();
                                    }
                                    signatures.add(methodAc);
                                    result.put(entryAc, signatures);
                                }
                            });

        } catch (Exception e) {
            LOGGER.warn("Could not perform database query. It might be that the JDBC connection could not build " +
                    "or is incorrectly configured. For more information take a look at the stack trace!", e);
        }
        return result;
    }
}
