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

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * Represents a raw protein match.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */

@XmlTransient
abstract class AbstractRawMatch<T extends Location>
        extends AbstractMatch<T>
        implements RawMatch<T>, Serializable {

    private Model model;

    protected AbstractRawMatch() {}

    public AbstractRawMatch(Model model)  {
        this.model = model;
    }

    @XmlElement(required=true)
    public Model getModel() {
        return model;
    }

    // Private for Hibernate (see http://www.javalobby.org/java/forums/t49288.html)
    // Uh-oh - changed to public for JPA.
    public void setModel(Model model) {
        this.model = model;
    }


    @XmlTransient
    public String getKey() {
        return model.getKey();
    } 

}