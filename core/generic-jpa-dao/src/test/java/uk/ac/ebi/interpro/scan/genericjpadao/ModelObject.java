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

package uk.ac.ebi.interpro.scan.genericjpadao;

import javax.persistence.*;

@Entity
public class ModelObject {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "test_field_one")
    private String testFieldOne;

    private ModelObject() {
    }

    public ModelObject(String testFieldOne) {
        this.testFieldOne = testFieldOne;
    }

    public Long getId() {
        return id;
    }

    public String getTestFieldOne() {
        return testFieldOne;
    }

    public void setTestFieldOne(String testFieldOne) {
        this.testFieldOne = testFieldOne;
    }

    @Override
    public boolean equals(Object o) {
        if (null == o) return true;
        if (!(o instanceof ModelObject)) return false;

        ModelObject that = (ModelObject) o;

        if (testFieldOne != null ? !testFieldOne.equals(that.testFieldOne) : that.testFieldOne != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return testFieldOne != null ? testFieldOne.hashCode() : 0;
    }
}
