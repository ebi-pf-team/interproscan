package uk.ac.ebi.interpro.scan.condensed.berkeley.model;

import com.sleepycat.persist.model.*;
import uk.ac.ebi.interpro.scan.condensed.berkeley.StringSqueezer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.DataFormatException;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Phil Jones
 *         <p/>
 *         <p/>
 *         For the condensed view BerkeleyDB, creates a single set of rows for the matches to each
 *         unique sequence (keyed on MD5)
 *         <p/>
 *         Note: The @Entity annotation is a sleepcat (Berkeley) annotation which unfortunately looks
 *         like a Hibernate annotation :-)
 */
@Entity
public class BerkeleyCondensedMarkup {

    @PrimaryKey  // Primary index in the BerkeleyDB
    private String md5;

    @SecondaryKey(relate = Relationship.ONE_TO_MANY, name = "uniprot_ac")
    private Set<String> uniprotAcs = new HashSet<String>();

    private byte[] html;

    @NotPersistent
    private static final StringSqueezer SQUEEZE = new StringSqueezer();

    public Set<String> getUniprotAcs() {
        return uniprotAcs;
    }

    public void setUniprotAcs(Set<String> uniprotAc) {
        this.uniprotAcs = uniprotAc;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getHtml() {
        // Expand String from compressed byte array.
        try {
            return SQUEEZE.inflate(html);
        } catch (DataFormatException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    public void setHtml(String html) {
        // Compress String to compressed byte array.
        try {
            this.html = SQUEEZE.compress(html);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BerkeleyCondensedMarkup that = (BerkeleyCondensedMarkup) o;

        if (!md5.equals(that.md5)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return md5.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("BerkeleyCondensedMarkup");
        sb.append("{uniprotAc='").append(uniprotAcs).append('\'');
        sb.append(", md5='").append(md5).append('\'');
        sb.append(", html='").append(html).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
