package uk.ac.ebi.interpro.scan.io.installer.interprodao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.interpro.scan.model.PathwayXref;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Represents the implementation class of {@link Entry2PathwayDAOImpl}.
 * The Repository annotation makes it a candidate for component-scanning.
 * <p/>
 * The entry2pathway table contains 12.852 entries at the moment.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Repository
public class Entry2PathwayDAOImpl implements Entry2PathwayDAO {
    private JdbcTemplate jdbcTemplate;

    @Resource
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Entry2PathwayDAOImpl() {
    }

    public Collection<PathwayXref> getPathwayXrefsByEntryAc(String entryAc) {
        List<PathwayXref> result = null;
        try {
            result = this.jdbcTemplate
                    .query(
                            "SELECT ENTRY_AC, DBCODE, AC, NAME FROM INTERPRO.ENTRY2PATHWAY WHERE ENTRY_AC=?",
                            new Object[]{"IPR018382"},
                            new RowMapper<PathwayXref>() {
                                public PathwayXref mapRow(ResultSet rs, int rowNum) throws SQLException {
                                    String name = rs.getString("name");
                                    String identifier = rs.getString("ac");
                                    String dbcode = rs.getString("dbcode");
                                    PathwayXref entry = new PathwayXref(dbcode, identifier, name);
                                    return entry;
                                }
                            });

        } catch (Exception e) {
//            log.warn("Could not perform database query. It might be that the JDBC connection could not build" +
//                    " or is wrong configured. For more info take a look at the stack trace!", e);
        }
        return result;
    }

    public boolean isDatabaseAlive() {
        try {
            Connection con = jdbcTemplate.getDataSource().getConnection();
            return true;
        } catch (SQLException e) {
//            log.error("Database is down! Could not perform any SQL query!", e);
        }
        return false;
    }
}
