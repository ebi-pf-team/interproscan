package uk.ac.ebi.interpro.scan.genericjpadao;

import javax.persistence.*;

@Entity
public class ModelObject {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "test_field_one")
    private String testFieldOne;

    private ModelObject() {
    }

    public ModelObject(String testFieldOne) {
        this.testFieldOne = testFieldOne;
    }

    public Long getId() {
        return id;
    }

    public String getTestFieldOne() {
        return testFieldOne;
    }

    @Override
    public boolean equals(Object o) {
        if (null == o) return true;
        if (!(o instanceof ModelObject)) return false;

        ModelObject that = (ModelObject) o;

        if (testFieldOne != null ? !testFieldOne.equals(that.testFieldOne) : that.testFieldOne != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return testFieldOne != null ? testFieldOne.hashCode() : 0;
    }
}
