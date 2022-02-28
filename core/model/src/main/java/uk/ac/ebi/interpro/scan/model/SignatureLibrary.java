/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.interpro.scan.model;

import javax.xml.bind.annotation.XmlType;

/**
 * Signature library, for example Pfam or PRINTS.
 *
 * @author  Antony Quinn
 * @author  Phil Jones
 * @version $Id$
 */
@XmlType(name = "SignatureLibraryType")
public enum SignatureLibrary {

    //TODO - complete descriptions of member database libraries.
    ANTIFAM("AntiFam", "Prediction of spurious protein.", false),
    CDD("CDD", "Prediction of CDD domains", true),
    HAMAP("Hamap", "Description to be added", true),
    MOBIDB_LITE("MobiDBLite", "Predicts disordered protein regions", false),
    PHOBIUS("Phobius", "Prediction of signal peptides and trans-membrane regions", false),
    GENE3D("Gene3D", "Description to be added", true),
    PANTHER("PANTHER", "The PANTHER (Protein ANalysis THrough Evolutionary Relationships) Classification System is a unique resource that classifies genes by their functions, using published scientific experimental evidence and evolutionary relationships to predict function even in the absence of direct experimental evidence.", true),
    PFAM("Pfam", "Description to be added", true),
    SMART("SMART", "Description to be added", true),
    SUPERFAMILY("SUPERFAMILY", "Description to be added", true),
    PIRSF("PIRSF", "Family classification system at the Protein Information Resource", true),
    PIRSR("PIRSR", "Family classification system - Residue level at the Protein Information Resource", false),
    PRINTS("PRINTS", "Description to be added", true),
    PRODOM("ProDom", "Description to be added", true),
    PROSITE_PATTERNS("ProSitePatterns", "Description to be added", true),
    PROSITE_PROFILES("ProSiteProfiles", "Description to be added", true),
    COILS("Coils", "Description to be added", false),
    TIGRFAM("TIGRFAM", "Description to be added", true),
    SFLD("SFLD", "Description to be added", true),
    SIGNALP_EUK("SignalP_EUK", "SignalP (organism type eukaryotes) predicts the presence and location of signal peptide cleavage sites in amino acid sequences for eukaryotes.", false),
    SIGNALP_GRAM_POSITIVE("SignalP_GRAM_POSITIVE", "SignalP (organism type gram-positive prokaryotes) predicts the presence and location of signal peptide cleavage sites in amino acid sequences for gram-positive prokaryotes.", false),
    SIGNALP_GRAM_NEGATIVE("SignalP_GRAM_NEGATIVE", "SignalP (organism type gram-negative prokaryotes) predicts the presence and location of signal peptide cleavage sites in amino acid sequences for gram-negative prokaryotes.", false),
    TMHMM("TMHMM", "Prediction of transmembrane helices in proteins.", false);

    private String name;

    private String description;

    // Is this an InterPro member database (whose signatures can be integrated into InterPro entries)?
    private boolean interproMDB;

    SignatureLibrary(String name, String description, boolean interproMDB) {
        setName(name);
        setDescription(description);
        setInterproMDB(interproMDB);
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    public boolean isInterproMDB() {
        return interproMDB;
    }

    private void setInterproMDB(boolean interproMDB) {
        this.interproMDB = interproMDB;
    }
}
