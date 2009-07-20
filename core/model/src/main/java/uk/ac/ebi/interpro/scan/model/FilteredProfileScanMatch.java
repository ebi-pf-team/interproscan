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
 * ProfileScan filtered match.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@Entity
@XmlType(name="FilteredProfileScanMatchType", propOrder={"signature", "locations"})
public class FilteredProfileScanMatch
        extends AbstractFilteredMatch<ProfileScanLocation>
        implements FilteredMatch<ProfileScanLocation>, Serializable {

    protected FilteredProfileScanMatch() {}

    public FilteredProfileScanMatch(Signature signature) {
        super(signature);
    }

    public FilteredProfileScanMatch(Signature signature, Set<ProfileScanLocation> locations) {
        super(signature, locations);
    }

    @Override public ProfileScanLocation addLocation(ProfileScanLocation location) {
        return super.addLocation(location);
    }

    @Override public void removeLocation(ProfileScanLocation location) {
        super.removeLocation(location);
    }

    @Override public String toString()  {
        return ToStringBuilder.reflectionToString(this);
    }

}