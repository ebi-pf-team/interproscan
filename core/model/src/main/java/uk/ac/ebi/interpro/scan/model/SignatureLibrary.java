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
    CDD("CDD", "Prediction of CDD domains"),
    PHOBIUS("Phobius", "Prediction of signal peptides and trans-membrane regions"),
    GENE3D("Gene3D", "Description to be added"),
    PANTHER("PANTHER", "The PANTHER (Protein ANalysis THrough Evolutionary Relationships) Classification System is a unique resource that classifies genes by their functions, using published scientific experimental evidence and evolutionary relationships to predict function even in the absence of direct experimental evidence."),
    PFAM("Pfam", "Description to be added"),
    SMART("SMART", "Description to be added"),
    SUPERFAMILY("SUPERFAMILY", "Description to be added"),
    PIRSF("PIRSF", "Family classification system at the Protein Information Resource"),
    PRINTS("PRINTS", "Description to be added"),
    PRODOM("ProDom", "Description to be added"),
    PROSITE_PATTERNS("ProSitePatterns", "Description to be added"),
    PROSITE_PROFILES("ProSiteProfiles", "Description to be added"),
    COILS("Coils", "Description to be added"),
    HAMAP("Hamap", "Description to be added"),
    TIGRFAM("TIGRFAM", "Description to be added"),
    SFLD("SFLD", "Description to be added"),
    SIGNALP_EUK("SignalP_EUK", "SignalP (organism type eukaryotes) predicts the presence and location of signal peptide cleavage sites in amino acid sequences for eukaryotes."),
    SIGNALP_GRAM_POSITIVE("SignalP_GRAM_POSITIVE", "SignalP (organism type gram-positive prokaryotes) predicts the presence and location of signal peptide cleavage sites in amino acid sequences for gram-positive prokaryotes."),
    SIGNALP_GRAM_NEGATIVE("SignalP_GRAM_NEGATIVE", "SignalP (organism type gram-negative prokaryotes) predicts the presence and location of signal peptide cleavage sites in amino acid sequences for gram-negative prokaryotes."),
    TMHMM("TMHMM", "Prediction of transmembrane helices in proteins.");

    private String name;

    private String description;

    SignatureLibrary(String name, String description) {
        setName(name);
        setDescription(description);
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
}
