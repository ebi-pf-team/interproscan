/*
 * Copyright 2010 the original author or authors.
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

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Processes every java.util.Date class in this package
 *
 * @author  Antony Quinn
 * @version $Id$
 */

@XmlTransient
final class DateAdapter extends XmlAdapter<String, Date> {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    public String marshal(Date date) {
        if (date == null) {
            return null;
        }
        return SDF.format(date);
    }

    public Date unmarshal(String date) throws ParseException {
        return toDate(date);
    }

    public static Date toDate(String date) throws ParseException {
        if (date == null) {
            return null;
        }
        return SDF.parse(date);
    }

}
