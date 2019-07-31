package uk.ac.ebi.interpro.scan.model.hibernate;

import org.hibernate.boot.model.naming.Identifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class OraclePhysicalNamingStrategyTest {
    OraclePhysicalNamingStrategy oraclePhysicalNamingStrategy = new OraclePhysicalNamingStrategy();

    @Test
    public void basicTest() {
        final String tableName = "PFAM_HMMER3_RAW_MATCH";

        Identifier identifier = new Identifier(tableName, false);
        String out = oraclePhysicalNamingStrategy.toPhysicalTableName(identifier, null).getText();
        assertNotNull(out);
        assertEquals(out, "pfam_hmmer3_raw_match");
    }
}
