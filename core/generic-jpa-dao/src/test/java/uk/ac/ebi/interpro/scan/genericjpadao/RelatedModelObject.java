package uk.ac.ebi.interpro.scan.genericjpadao;

/**
 * Test Model class, holds foreign key to ModelObject class in JPA context.
 * User: pjones
 * Date: 21-Jul-2009
 * Time: 11:51:58
 *
 * @author Phil Jones, EMBL-EBI
 */
import javax.persistence.*;

@Entity
public class RelatedModelObject {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "test_field_two")
    private String testFieldTwo;

    @ManyToOne (optional = false)
    private ModelObject modelObject;

    protected RelatedModelObject(){

    }

    public RelatedModelObject(ModelObject related, String testFieldValue){
        this.testFieldTwo = testFieldValue;
        this.modelObject = related;
        related.addRelatedModelObject(this);
    }

    public Long getId() {
        return id;
    }

    public String getTestFieldTwo() {
        return testFieldTwo;
    }

    public ModelObject getModelObject() {
        return modelObject;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RelatedModelObject)) return false;

        RelatedModelObject that = (RelatedModelObject) o;

        if (!modelObject.equals(that.modelObject)) return false;
        if (testFieldTwo != null ? !testFieldTwo.equals(that.testFieldTwo) : that.testFieldTwo != null) return false;

        return true;
    }

    public int hashCode() {
        int result = testFieldTwo != null ? testFieldTwo.hashCode() : 0;
        result = 31 * result + modelObject.hashCode();
        return result;
    }
}
