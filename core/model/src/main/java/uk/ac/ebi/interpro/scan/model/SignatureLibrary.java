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
    PHOBIUS("Phobius", "Prediction of signal peptides and trans-membrane regions"),
    GENE3D("Gene3D", "Description to be added"),
    PANTHER("PANTHER", "The PANTHER (Protein ANalysis THrough Evolutionary Relationships) Classification System is a unique resource that classifies genes by their functions, using published scientific experimental evidence and evolutionary relationships to predict function even in the absence of direct experimental evidence."),
    PFAM("Pfam", "Description to be added"),
    PFAM_B("Pfam B", "Description to be added"),
    SMART("SMART", "Description to be added"),
    SUPERFAMILY("SUPERFAMILY", "Description to be added"),
    PIRSF("PIRSF", "Family classification system at the Protein Information Resource"),
    PRINTS("PRINTS", "Description to be added"),
    PRODOM("ProDom", "Description to be added"),
    PROSITE_PATTERNS("ProSitePatterns", "Description to be added"),
    PROSITE_PROFILES("ProSiteProfiles", "Description to be added"),
    COILS("Coils", "Description to be added"),
    COMPARA("Compara", "Description to be added"),
    HAMAP("Hamap", "Description to be added"),
    TIGRFAM("TIGRFAM", "Description to be added"),
    SIGNALP3_EUK("SignalP Eukaryotic","Description to be added"),
    SIGNALP3_GRAM_POS("SignalP Gram+", "Description to be added"),
    SIGNALP3_GRAM_NEG("SignalP Gram-", "Description to be added");

    private String name;

    private String description;

    private SignatureLibrary(String name, String description) {
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
