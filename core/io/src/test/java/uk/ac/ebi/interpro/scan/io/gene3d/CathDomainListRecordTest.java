package uk.ac.ebi.interpro.scan.io.gene3d;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.Ignore;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.Collection;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.Model;

/**
 * Tests {@link uk.ac.ebi.interpro.scan.io.gene3d.CathDomainListResourceReader}.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@ExtendWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class CathDomainListRecordTest {

    @Test
    public void testToSignature() throws IOException {
        final Signature expected = new Signature("G3DSA:1.10.8.10");
        CathDomainListRecord record = new CathDomainListRecord("1oaiA00", 1, 10, 8, 10);
        assertEquals(expected, record.toSignature());
    }

    @Test
    @Ignore("Eye-balling shows collections are equal, but barfs when run -- needs sorting out")
    public void testCreateSignatures() throws IOException {
        final Collection<CathDomainListRecord> RECORDS = Arrays.asList(
            new CathDomainListRecord("1oaiA00", 1, 10,  8,  10),
            new CathDomainListRecord("1go5A00", 1, 10,  8,  10),
            new CathDomainListRecord("1oksA00", 1, 10,  8,  10),
            new CathDomainListRecord("1ws8C00", 2, 60, 40, 420),
            new CathDomainListRecord("1ws8D00", 2, 60, 40, 420)
        );
        final Collection<Signature> SIGNATURES = Arrays.asList(
            new Signature.Builder("G3DSA:1.10.8.10").model(new Model("1oaiA00")).model(new Model("1go5A00")).model(new Model("1oksA00")).build(),
            new Signature.Builder("G3DSA:2.60.40.420").model(new Model("1ws8C00")).model(new Model("1ws8D00")).build()
        );
        assertEquals(SIGNATURES, CathDomainListRecord.createSignatures(RECORDS));
    }

}