package uk.ac.ebi.interpro.scan.model;

import javax.xml.bind.annotation.XmlTransient;
import java.util.Collection;

/**
 * Simple generalization for a matches holder implementation.
 *
 * @author Maxim Scheremetjew
 * @version $Id$
 */
@XmlTransient
public interface IMatchesHolder {

    void addProtein(Protein protein);

    void addProteins(Collection<Protein> proteins);
}