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

/**
 * Stores object and it's expected XML representation.
 *
 * @author  Antony Quinn
 * @version $Id$ 
 * @since   1.0
 */
class ObjectXmlPair<T> {

    private final T      object;
    private final String xml;

    private ObjectXmlPair()  {
        this.object = null;
        this.xml    = null;
    }
    
    public ObjectXmlPair(T object, String xml)  {
        this.object = object;
        this.xml = xml;
    }

    public T getObject() {
        return object;
    }

    public String getXml() {
        return xml;
    }
    
}
