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

import javax.persistence.*;

/**
 * Represents raw protein match.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public interface RawMatch<T extends Location> extends Match<T> {

    @Id
    Long getId();

    @ManyToOne
    public Model getModel();
    // TODO: Why is setter required by Hibernate (and JPA?)
    public void setModel(Model model);

}
