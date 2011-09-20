package uk.ac.ebi.interpro.scan.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.Protein;

import java.util.HashSet;
import java.util.Set;

/**
 * Controller for InterPro protein view.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@Controller
public class ProteinViewController {

    /**
     * Returns protein features for inclusion in DBML
     *
     * @param  ac   Protein accession, for example "P38398"
     * @return Protein features for inclusion in DBML
     */
    @RequestMapping(value = "/protein-features/{ac}", method = RequestMethod.GET)
    public ModelAndView proteinFeatures(@PathVariable String ac) {
        return new ModelAndView("protein-features", "protein", retrieve(ac));
    }    

    /**
     * Returns main body of protein page for inclusion in DBML
     *
     * @param  ac   Protein accession, for example "P38398"
     * @return Main body of protein page for inclusion in DBML
     */
    @RequestMapping(value = "/protein-body/{ac}", method = RequestMethod.GET)
    public ModelAndView proteinBody(@PathVariable String ac) {
        return new ModelAndView("protein-body", "protein", retrieve(ac));
    }

    /**
     * Returns protein for given accession number
     *
     * @param  ac   Protein accession, for example "P38398"
     * @return Protein for given accession
     */
    private Protein retrieve(String ac) {

        // TODO: Get real data from Berkeley DB

        // Create protein
        Protein p = new Protein.Builder("GYGDEYGDEEDEOIHOHDEUHWQJINIKNCD")
                .crossReference(new ProteinXref("UniProt", "A0A314", "RR12_COFAR"))
                .build();

        // Add matches
        Signature signature = new Signature("G3DSA:2.40.50.140", "Nucleic acid-binding proteins");
        Set<Hmmer3Match.Hmmer3Location> locations = new HashSet<Hmmer3Match.Hmmer3Location>();
        locations.add(new Hmmer3Match.Hmmer3Location(74, 93, -8.9, 0.28, 63, 82, 114, 73, 94));
        locations.add(new Hmmer3Match.Hmmer3Location(105, 189, -8.9, 0.28, 63, 82, 114, 73, 94));
        p.addMatch(new Hmmer3Match(signature, -8.9, 0.28, locations));

        return p;

    }

}
