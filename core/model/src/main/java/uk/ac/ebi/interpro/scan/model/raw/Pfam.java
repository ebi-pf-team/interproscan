package uk.ac.ebi.interpro.scan.model.raw;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

/**
 * Created by IntelliJ IDEA.
 * User: thimma
 * Date: 21-Oct-2009
 * Time: 15:48:13
 * To change this template use File | Settings | File Templates.
 */
@Entity
//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "PFAM")
public class Pfam extends HmmRawMatch {
    public Pfam() {

    }
    
}
