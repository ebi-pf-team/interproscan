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
     * Returns view name and data to display a simple protein page
     *
     * @param  ac   Protein accession, for example "P38398"
     * @return View name and model to display a simple protein page
     */
    @RequestMapping(value = "/protein/{ac}", method = RequestMethod.GET)
    public ModelAndView protein(@PathVariable String ac) {
        
//        Protein p = new Protein(id, "GYGDEYGDEEDEOIHOHDEUHWQJINIKNCD", "d85ce40adbc0af811e7feae7fd3cd827");
//        p.addMatch(new Protein.Match("IPR000008", "C2 calcium-dependent membrane targeting", 841, 932));
//        p.addMatch(new Protein.Match("IPR000909", "Phospholipase C, phosphatidylinositol-specific , X domain", 402, 546));
//        //return new ModelAndView("protein", "protein", p);
//        return new ModelAndView("protein-body", "protein", p);
        
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

        return new ModelAndView("protein-body", "protein", p);

    }

}
