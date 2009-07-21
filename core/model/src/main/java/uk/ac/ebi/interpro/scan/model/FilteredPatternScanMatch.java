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

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Set;

/**
 * PatternScan filtered match.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@Entity
@XmlType(name="FilteredPatternScanMatchType", propOrder={"signature", "locations"})
public class FilteredPatternScanMatch
        extends AbstractFilteredMatch<PatternScanLocation>
        implements FilteredMatch<PatternScanLocation>, Serializable {

    protected FilteredPatternScanMatch() {}

    public FilteredPatternScanMatch(Signature signature) {
        super(signature);
    }

    public FilteredPatternScanMatch(Signature signature, Set<PatternScanLocation> locations) {
        super(signature, locations);
    }

    @Override public PatternScanLocation addLocation(PatternScanLocation location) {
        return super.addLocation(location);
    }

    @Override public void removeLocation(PatternScanLocation location) {
        super.removeLocation(location);
    }

    @Override public String toString()  {
        return ToStringBuilder.reflectionToString(this);
    }

}