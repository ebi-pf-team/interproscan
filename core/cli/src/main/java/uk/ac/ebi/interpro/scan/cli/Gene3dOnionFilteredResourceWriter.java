package uk.ac.ebi.interpro.scan.cli;

import uk.ac.ebi.interpro.scan.io.AbstractResourceWriter;
import uk.ac.ebi.interpro.scan.model.PersistenceConversion;
import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;

import java.io.IOException;
import java.util.Calendar;

/**
 * This implementation writes flat-files for import into Onion's IPRScan table.
 *
 * @author Antony Quinn
 * @version $Id$
 */
public class Gene3dOnionFilteredResourceWriter extends AbstractResourceWriter<Gene3dHmmer3RawMatch> {

    private Gene3dSupport support;

    public void setSupport(Gene3dSupport support) {
        this.support = support;
    }

//   analysis_type_id  NUMBER(2)       NOT NULL,
//   upi               CHAR(13)	     NOT NULL,
//   method_ac         VARCHAR2(25)    NOT NULL,
//   relno_major       NUMBER(6)	     NOT NULL,
//   relno_minor       NUMBER(6)	     NOT NULL,
//   seq_start         NUMBER(6)	     NOT NULL,
//   seq_end           NUMBER(6)	     NOT NULL,
//   hmm_start         NUMBER(6)	     NULL,
//   hmm_end           NUMBER(6)	     NULL,
//   hmm_bounds        VARCHAR2(20)    NULL,
//   score             FLOAT(126)      NULL,
//   seqscore          FLOAT(126)      NULL,
//   evalue	     FLOAT(126)	     NULL,
//   status            CHAR(1)         NOT NULL,
//   timestamp	     DATE	     NOT NULL

    @Override
    protected String createLine(Gene3dHmmer3RawMatch m) {
        String analysisTypeId = "54"; // TODO: Inject
        String status = "T";  // TODO: Inject
        Calendar calendar = Calendar.getInstance();
        java.sql.Timestamp timestamp = new java.sql.Timestamp(calendar.getTime().getTime());
        String[] rel = m.getSignatureLibraryRelease().split("\\.");
        String relNoMajor = rel[0];
        String relNoMinor = rel[1];
        String signatureAc;
        try {
            signatureAc = support.findSignatureAccession(m.getModelId());
        }
        catch (IOException e) {
            throw new IllegalStateException("Could not find signature accession for " + m.getModelId());
        }
        String[] line = {
                analysisTypeId,
                m.getSequenceIdentifier(),
                signatureAc,
                relNoMajor,
                relNoMinor,
                String.valueOf(m.getLocationStart()),
                String.valueOf(m.getLocationEnd()),
                String.valueOf(m.getHmmStart()),
                String.valueOf(m.getHmmEnd()),
                String.valueOf(m.getHmmBounds()),
                String.valueOf(m.getLocationScore()),
                String.valueOf(m.getScore()),
                String.valueOf(PersistenceConversion.set(m.getEvalue())),
                status,
                timestamp.toString()
        };
        // TODO: Replace with org.springframework.util.StringUtils.arrayToDelimitedString()        
        StringBuilder builder = new StringBuilder();
        int last = line.length - 1;
        for (int i = 0; i < line.length; i++) {
            builder.append(line[i]);
            if (i < last) {
                builder.append("\t");
            }
        }
        return builder.toString();
    }

}
