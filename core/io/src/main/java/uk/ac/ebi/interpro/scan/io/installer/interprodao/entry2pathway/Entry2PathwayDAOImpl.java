package uk.ac.ebi.interpro.scan.io.installer.interprodao.entry2pathway;

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

/**
 * Represents the implementation class of {@link uk.ac.ebi.interpro.scan.io.installer.interprodao.entry2pathway.Entry2PathwayDAOImpl}.
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
        try {
//            this.jdbcTemplate.queryForObject("SELECT submitterid, first_name, surname, password, email_address FROM " + Submitter.TABLE_NAME + " WHERE ACTIVE='Y' AND email_address=?",
//                    new Object[]{entryAc.toUpperCase()},
//                    new RowMapper<Submitter>() {
//                        public Submitter mapRow(ResultSet rs, int rowNum) throws SQLException {
//                            Submitter submitter = new Submitter();
//                            submitter.setSubmitterId(Integer.parseInt(rs.getString("submitterid")));
//                            submitter.setFirstName(rs.getString("first_name"));
//                            submitter.setSurname(rs.getString("surname"));
//                            submitter.setEmailAddress(rs.getString("email_address"));
//                            submitter.setPassword(rs.getString("password"));
//                            return submitter;
//                        }
//                    });

        } catch (Exception e) {
//            log.warn("Could not perform database query. It might be that the JDBC connection could not build" +
//                    " or is wrong configured. For more info take a look at the stack trace!", e);
        }
        return null;
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