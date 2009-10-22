package uk.ac.ebi.interpro.scan.model.raw;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: thimma
 * Date: 22-Oct-2009
 * Time: 16:30:07
 * To change this template use File | Settings | File Templates.
 */
public final class RawMatchKey implements Serializable {

        private String seqIdentifier;
        private String method_ac;
        private String generator;
        private String dbVersion;
        private long seq_start;

    public String getSeqIdentifier() {
        return seqIdentifier;
    }

    public void setSeqIdentifier(String seqIdentifier) {
        this.seqIdentifier = seqIdentifier;
    }

    public String getMethod_ac() {
        return method_ac;
    }

    public void setMethod_ac(String method_ac) {
        this.method_ac = method_ac;
    }

    public String getGenerator() {
        return generator;
    }

    public void setGenerator(String generator) {
        this.generator = generator;
    }

    public String getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(String dbVersion) {
        this.dbVersion = dbVersion;
    }

    public long getSeq_start() {
        return seq_start;
    }

    public void setSeq_start(long seq_start) {
        this.seq_start = seq_start;
    }

    //default equals and override method based on field types
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RawMatchKey)) return false;

        RawMatchKey that = (RawMatchKey) o;

        if (seq_start != that.seq_start) return false;
        if (dbVersion != null ? !dbVersion.equals(that.dbVersion) : that.dbVersion != null) return false;
        if (generator != null ? !generator.equals(that.generator) : that.generator != null) return false;
        if (method_ac != null ? !method_ac.equals(that.method_ac) : that.method_ac != null) return false;
        if (seqIdentifier != null ? !seqIdentifier.equals(that.seqIdentifier) : that.seqIdentifier != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = seqIdentifier != null ? seqIdentifier.hashCode() : 0;
        result = 31 * result + (method_ac != null ? method_ac.hashCode() : 0);
        result = 31 * result + (generator != null ? generator.hashCode() : 0);
        result = 31 * result + (dbVersion != null ? dbVersion.hashCode() : 0);
        result = 31 * result + (int) (seq_start ^ (seq_start >>> 32));
        return result;
    }

    /*public int hashCode() {
            return ((this.OrderId()==null
                            ?0:this.getOrderId().hashCode())
                     ^ ((int) this.getItemId()));
        }

        public boolean equals(Object otherOb) {
            if (this == otherOb) {
                return true;
            }
            if (!(otherOb instanceof LineItemKey)) {
                return false;
            }
            LineItemKey other = (LineItemKey) otherOb;
            return ((this.getOrderId()==null
                            ?other.orderId==null:this.getOrderId().equals
                    (other.orderId)) && (this.getItemId ==
                        other.itemId));
        }
             */
        public String toString() {
            return "" + seqIdentifier + "-" + method_ac + "-" + dbVersion + "-" + generator + "-" + seq_start;
        }

    
}
