package uk.ac.ebi.interpro.scan.io.cdd;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.io.AbstractModelFileParser;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a cddid.tbl file and creates Signature / Method objects appropriately.
 *
 * @author Siew-Yit Yong, EMBL-EBI, InterPro
 * @author Gift Nuka
 * @version $Id$
 * @since 5.16
 */
public class CDDModelParser extends AbstractModelFileParser {

    /*
     * Example cddid.tbl input file:
     *
     *237975	cd00001	PTS_IIB_man	PTS_IIB, PTS system, Mannose/sorbose specific IIB subunit. The bacterial phosphoenolpyruvate: sugar phosphotransferase system (PTS) is a multi-protein system involved in the regulation of a variety of metabolic and transcriptional processes. This family is one of four structurally and functionally distinct group IIB PTS system cytoplasmic enzymes, necessary for the uptake of carbohydrates across the cytoplasmic membrane and their phosphorylation. The active site histidine receives a phosphate group from the IIA subunit and transfers it to the substrate.	151
     *237976	cd00002	YbaK_deacylase	This CD includes cysteinyl-tRNA(Pro) deacylases from Haemophilus influenzae and Escherichia coli and other related bacterial proteins. These trans-acting, single-domain proteins are homologs of ProX and also the cis-acting prolyl-tRNA synthetase (ProRS) inserted (INS) editing domain.  The bacterial amino acid trans-editing enzyme YbaK is a deacylase that hydrolyzes cysteinyl-tRNA(Pro)'s mischarged by prolyl-tRNA synthetase.   YbaK also hydrolyzes glycyl-tRNA's, alanyl-tRNA's, seryl-tRNA's, and prolyl-tRNA's.  YbaK is homologous to the INS domain of prolyl-tRNA synthetase (ProRS) as well as the trans-editing enzyme ProX of Aeropyrum pernix which hydrolyzes alanyl-tRNA's and glycyl-tRNA's.	152
     *237977	cd00003	PNPsynthase	Pyridoxine 5'-phosphate (PNP) synthase domain; pyridoxal 5'-phosphate is the active form of vitamin B6 that acts as an essential, ubiquitous coenzyme in amino acid metabolism. In bacteria, formation of pyridoxine 5'-phosphate is a step in the biosynthesis of vitamin B6. PNP synthase, a homooctameric enzyme, catalyzes the final step in PNP biosynthesis, the condensation of 1-amino-acetone 3-phosphate and 1-deoxy-D-xylulose 5-phosphate. PNP synthase adopts a TIM barrel topology, intersubunit contacts are mediated by three ''extra'' helices, generating a tetramer of symmetric dimers with shared active sites; the open state has been proposed to accept substrates and to release products, while most of the catalytic events are likely to occur in the closed state; a hydrophilic channel running through the center of the barrel was identified as the essential structural feature that enables PNP synthase to release water molecules produced during the reaction from the closed, solvent-shielded active site.	234
     *99708	cd00004	Sortase	Sortases are cysteine transpeptidases, found in gram-positive bacteria, that anchor surface proteins to peptidoglycans of the bacterial cell wall envelope. They do so by catalyzing a transpeptidation reaction in which the surface protein substrate is cleaved at a conserved cell wall sorting signal and covalently linked to peptidoglycan for display on the bacterial surface. Sortases are grouped into different classes and subfamilies based on sequence, membrane topology, genomic positioning, and cleavage site preference. The different classes are called Sortase A or SrtA (subfamily 1), B or SrtB (subfamily 2), C or SrtC (subfamily3), D or SrtD (subfamilies 4 and 5), and E or SrtE. In two different sortase subfamilies, the N-terminus either functions as both a signal peptide for secretion and a stop-transfer signal for membrane anchoring, or it contains a signal peptide only and the C-terminus serves as a membrane anchor. Most gram-positive bacteria contain more than one sortase and it is thought that the different sortases anchor different surface protein classes. The sortase domain is a modified beta-barrel flanked by two (SrtA) or three (SrtB) short alpha-helices.	128
     *187674	cd00005	CBM9_like_1	DOMON-like type 9 carbohydrate binding module of xylanases. Family 9 carbohydrate-binding modules (CBM9) play a role in the microbial degradation of cellulose and hemicellulose (materials found in plants). The domain has previously been called cellulose-binding domain. The polysaccharide binding sites of CBMs with available 3D structure have been found to be either flat surfaces with interactions formed by predominantly aromatic residues (tryptophan and tyrosine), or extended shallow grooves. The CBM9 domain frequently occurs in tandem repeats; members found in this subfamily typically co-occur with glycosyl hydrolase family 10 domains and are annotated as endo-1,4-beta-xylanases. CBM9 from Thermotoga maritima xylanase 10A is reported to have specificity for polysaccharide reducing ends.	185
     *
     */

    private static final Logger LOGGER = Logger.getLogger(CDDModelParser.class.getName());

    private static final Pattern CD_ACCESSION_PATTERN = Pattern.compile("^cd\\d+");
    // CDD models can have accessions start with sd too. Therefore, we need this pattern:
    private static final Pattern SD_ACCESSION_PATTERN = Pattern.compile("^sd\\d+");

//    private static final Pattern NAME_PATTERN = Pattern.compile("^\\s+");
//    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("^\\s+");


    @Transactional
    public SignatureLibraryRelease parse() throws IOException {
        LOGGER.debug("Starting to parse cddid.tbl file.");
        SignatureLibraryRelease release = new SignatureLibraryRelease(library, releaseVersion);

        for (Resource modelFile : modelFiles) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(modelFile.getInputStream()))) {
                int lineNumber = 0;
                String line;
                while ((line = reader.readLine()) != null) {
                    if (LOGGER.isDebugEnabled() && lineNumber++ % 10000 == 0) {
                        LOGGER.debug("Parsed " + lineNumber + " lines of the cddid.tbl file.");
                        LOGGER.debug("Parsed " + release.getSignatures().size() + " signatures.");
                    }

                    lineNumber ++;
                    line = line.trim();

                    // Now parse the model line
                    String[] values = line.split("\\t");
                    if (values.length != 5) {
                        LOGGER.warn("CDD model parser text not in expected 5 column TSV format "
                                + "on line number " + lineNumber + " - so this can't be added to the database");
                        continue;
                    }
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Signature line: " + Arrays.toString(values));
                    }

                    // Parse model accession
                    final String accession = (values[1] == null ? "" : values[1].trim());
                    Matcher cdAcMatcher = CD_ACCESSION_PATTERN.matcher(accession);
                    Matcher sdAcMather = SD_ACCESSION_PATTERN.matcher(accession);
                    if (! (cdAcMatcher.find() || sdAcMather.find())) {
                        // E.g. accession = "CHL00001"
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("CDD model parser skipping CDD signature accession " + accession
                                    + " on line number " + lineNumber + " - so this can't be added to the database");
                        }
                        continue;
                    }

                    // Parse model name and description
                    String name = null;
                    String description = null;
                    if (values[2] == null) {
                        LOGGER.warn("CDD model parser could not extract the name from NULL text "
                                + "on line number " + lineNumber + " - so this can't be added to the database");
                    }
                    else {
                        name = values[2].trim(); // Example: BAR_Arfaptin_like
                        // Name will also be used for description
                        description = name;
                        if (description.length() > 50) {
                            description = description.substring(0, 46) + "...";
                        }
                    }

                    // CDD abstract text
                    // Example: The Bin/Amphiphysin/Rvs (BAR) domain of Arfaptin-
                    //String abstractText = (values[3] == null ? "" : values[3].trim());

                    // Now create the signature
                    release.addSignature(createSignature(accession, name, description, release));
                }
            }
        }
        return release;
    }

    protected Signature createSignature(String accession, String name, String description, SignatureLibraryRelease release) {
        Model model = new Model(accession, name, description, null);
        return new Signature(accession, name, null, description, null, release, Collections.singleton(model));
    }


}
