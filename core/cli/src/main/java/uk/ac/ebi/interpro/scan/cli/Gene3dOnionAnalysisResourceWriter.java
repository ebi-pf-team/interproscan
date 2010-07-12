package uk.ac.ebi.interpro.scan.cli;

import uk.ac.ebi.interpro.scan.io.AbstractResourceWriter;
import uk.ac.ebi.interpro.scan.model.PersistenceConversion;
import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;

/**
 * This implementation writes flat-files for import into Onion's analysis table.
 *
 * @author Antony Quinn
 * @version $Id$
 */
public class Gene3dOnionAnalysisResourceWriter extends AbstractResourceWriter<Gene3dHmmer3RawMatch> {

    protected String createLine(Gene3dHmmer3RawMatch m) {
        //CREATE TABLE "ONION"."GENE3D_HMMER3_ANALYSIS"
        // ("UPI" CHAR(13 BYTE) NOT NULL ENABLE,
        //  "METHOD_AC" VARCHAR2(25 BYTE) NOT NULL ENABLE,
        //  "RELNO_MAJOR" NUMBER(6,0) NOT NULL ENABLE,
        //  "RELNO_MINOR" NUMBER(6,0) NOT NULL ENABLE,
        //  "SEQ_START" NUMBER(6,0) NOT NULL ENABLE,
        //  "SEQ_END" NUMBER(6,0) NOT NULL ENABLE,
        //  "HMM_START" NUMBER(6,0) NOT NULL ENABLE,
        //  "HMM_END" NUMBER(6,0) NOT NULL ENABLE,
        //  "HMM_BOUNDS" CHAR(2 BYTE) NOT NULL ENABLE,
        //  "ENVELOPE_START" NUMBER(6,0) NOT NULL ENABLE,
        //  "ENVELOPE_END" NUMBER(6,0) NOT NULL ENABLE,
        //  "SCORE" FLOAT(126) NOT NULL ENABLE,
        //  "SEQSCORE" FLOAT(126) NOT NULL ENABLE,
        //  "SEQEVALUE" FLOAT(126),
        //  "DOMAIN_C_EVALUE" FLOAT(126),
        //  "DOMAIN_I_EVALUE" FLOAT(126),
        //  "ACC" FLOAT(126),
        //  "ALIGNMENT" VARCHAR2(4000 BYTE),
        String[] rel = m.getSignatureLibraryRelease().split("\\.");
        String relNoMajor = rel[0];
        String relNoMinor = rel[1];
        String[] line = {
                m.getSequenceIdentifier(),
                m.getModelId(),
                relNoMajor,
                relNoMinor,
                String.valueOf(m.getLocationStart()),
                String.valueOf(m.getLocationEnd()),
                String.valueOf(m.getHmmStart()),
                String.valueOf(m.getHmmEnd()),
                String.valueOf(m.getHmmBounds()),
                String.valueOf(m.getEnvelopeStart()),
                String.valueOf(m.getEnvelopeEnd()),
                String.valueOf(m.getLocationScore()),
                String.valueOf(m.getScore()),
                String.valueOf(PersistenceConversion.set(m.getEvalue())),
                String.valueOf(PersistenceConversion.set(m.getDomainCeValue())),
                String.valueOf(PersistenceConversion.set(m.getDomainIeValue())),
                String.valueOf(m.getExpectedAccuracy()),
                m.getCigarAlignment()
        };
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
