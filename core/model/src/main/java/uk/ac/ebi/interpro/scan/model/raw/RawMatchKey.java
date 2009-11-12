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

        private String sequenceIdentifier;
        private String model;



    private String generator;
        private String dbversion;
        private long start;


    public String getSequenceIdentifier() {
        return sequenceIdentifier;
    }

    public void setSequenceIdentifier(String sequenceIdentifier) {
        this.sequenceIdentifier = sequenceIdentifier;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

     public String getGenerator() {
        return generator;
    }

    public void setGenerator(String generator) {
        this.generator = generator;
    }

    public String getDbversion() {
        return dbversion;
    }

    public void setDbversion(String dbversion) {
        this.dbversion = dbversion;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }




    //default equals and override method based on field types
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RawMatchKey)) return false;

        RawMatchKey that = (RawMatchKey) o;

        if (start != that.start) return false;
        if (dbversion != null ? !dbversion.equals(that.dbversion) : that.dbversion != null) return false;
        if (generator != null ? !generator.equals(that.generator) : that.generator != null) return false;
        if (model != null ? !model.equals(that.model) : that.model != null) return false;
        if (sequenceIdentifier != null ? !sequenceIdentifier.equals(that.sequenceIdentifier) : that.sequenceIdentifier != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = sequenceIdentifier != null ? sequenceIdentifier.hashCode() : 0;
        result = 31 * result + (model != null ? model.hashCode() : 0);
        result = 31 * result + (generator != null ? generator.hashCode() : 0);
        result = 31 * result + (dbversion != null ? dbversion.hashCode() : 0);
        result = 31 * result + (int) (start ^ (start >>> 32));
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
            return "" + sequenceIdentifier + "-" + model + "-" + dbversion + "-" + generator + "-" + start;
        }

    
}
