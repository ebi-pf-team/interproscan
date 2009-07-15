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

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * Sequence cross-reference
 *
 * @author  Phil Jones
 * @author  Antony Quinn
 * @version $Id: XrefSequenceIdentifier.java,v 1.8 2009/07/10 13:24:41 aquinn Exp $
 * @since   1.0
 * @see     Match
 */
@XmlTransient
@Entity
public class XrefSequenceIdentifier
        extends AbstractSequenceIdentifier
        implements SequenceIdentifier, MatchableEntity, Serializable {

    /**
     * Reference to the protein that this Xref is an annotation of.
     */
    @ManyToOne (optional = false)
    private Protein protein;

    /**
     * Returns the Protein that this accession / ID cross reference annotates.
     * @return the Protein that this accession / ID cross reference annotates.
     */
    public Protein getProtein() {
        return protein;
    }

    /**
     * Package private setter used by the Protein class to create a reference to the annotated protein.
     * @param protein the Protein that this accession / ID cross reference annotates.
     */
    void setProtein(Protein protein) {
        this.protein = protein;
    }

    /**
     * Logger for Junit logging. Log messages will be associated with the XrefSequenceIdentifier class.
     */
//    private static Logger LOGGER = Logger.getLogger(XrefSequenceIdentifier.class);

    /**
     * Zero arguments constructor just for Hibernate.
     */
    protected XrefSequenceIdentifier (){
        super(null);
    }

    /**
     * Constructor for an Xref that takes the xref String as an argument.
     * @param identifier the Xref String.
     */
    public XrefSequenceIdentifier(String identifier){
        super(identifier);
    }



}
