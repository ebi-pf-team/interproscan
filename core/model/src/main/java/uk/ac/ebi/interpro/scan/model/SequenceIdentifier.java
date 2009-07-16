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
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * Represents a sequence identifier, for example a UniProtKB accession or UniParc ID.
 *
 * @author  Antony Quinn
 * @author  Phil Jones
 * @version $Id$
 * @since   1.0
 */
@Entity
public interface SequenceIdentifier extends Serializable, MatchableEntity {

    @Id
    public Long getId();

    public void setId (Long id);

    /**
     * Returns sequence identifier
     *
     * @return Sequence identifier
     */
    public String getIdentifier();

    /**
     * TODO Sets the sequence identifier.  Required only for JPA - will try to remove.
     * @param identifier DO NOT USE - added only for JPA.
     */
    public void setIdentifier (String identifier);

    /**
     * Use this factory to create SequenceIdentifier objects based upon a String that may
     * be an accession, or an MD5, or any other way of identifying a protein in a FASTA file.
     */
    @XmlTransient
    public class Factory{

        private Factory(){}

        /**
         * Returns an appropriate implementation of SequenceIdentifier based upon the contents
         * of the identifier parameter.
         * 
         * @param  identifier the identifier retrieved from a flat file or FASTA file.
         * @return SequenceIdentifier of an appropriate class for the identifier type.
         */
        public static SequenceIdentifier createSequenceIdentifier (String identifier) {
            if (identifier == null){
                throw new IllegalArgumentException("'identifier' must not be null.");
            }
            return (MD5SequenceIdentifier.isMD5(identifier))
                ? new MD5SequenceIdentifier(identifier)
                : new XrefSequenceIdentifier(identifier);
        }
    }

}
