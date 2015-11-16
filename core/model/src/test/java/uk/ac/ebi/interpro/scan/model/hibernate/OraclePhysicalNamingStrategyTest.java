package uk.ac.ebi.interpro.scan.model.hibernate;

import org.hibernate.boot.model.naming.Identifier;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class OraclePhysicalNamingStrategyTest {
    OraclePhysicalNamingStrategy oraclePhysicalNamingStrategy = new OraclePhysicalNamingStrategy();

    @Test
    public void basicTest() {
        final String tableName = "PFAM_HMMER3_RAW_MATCH";

        Identifier identifier = new Identifier(tableName, false);
        String out = oraclePhysicalNamingStrategy.toPhysicalTableName(identifier, null).getText();
        Assert.assertNotNull(out);
        Assert.assertEquals("pfam_hmmer3_raw_match", out);
    }
}
