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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.xml.bind.annotation.*;
import javax.persistence.ManyToOne;
import javax.persistence.Entity;
import java.util.Set;

/**
 * Represents a raw protein match.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */

@Entity
@XmlTransient
public abstract class RawMatch<T extends Location> extends Match<T> {

    @ManyToOne
    private Model model;

    protected RawMatch() {}

    protected RawMatch(Model model)  {
        setModel(model);
    }

    // TODO: Make Location(s) a required argument?
    protected RawMatch(Model model, Set<T> locations)  {
        super(locations);
        setModel(model);
    }

    public Model getModel() {
        return model;
    }

    // Private for Hibernate (see http://www.javalobby.org/java/forums/t49288.html)
    // Uh-oh - changed to public for JPA.
    public void setModel(Model model) {
        this.model = model;
    }

    public String getKey() {
        return model.getKey();
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RawMatch))
            return false;
        final RawMatch m = (RawMatch) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(model, m.model)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(19, 71)
                .appendSuper(super.hashCode())
                .append(model)
                .toHashCode();
    }

}
