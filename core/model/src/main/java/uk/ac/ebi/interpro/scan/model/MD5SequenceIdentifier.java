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
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * MD5 sequence identifier.
 *
 * @author  Phil Jones
 * @author  Antony Quinn
 * @version $Id: MD5SequenceIdentifier.java,v 1.7 2009/07/10 13:24:41 aquinn Exp $
 * @since   1.0
 * @see     Match
 */
@XmlTransient
@Entity
class MD5SequenceIdentifier extends AbstractSequenceIdentifier implements SequenceIdentifier, Serializable {

    /**
     * Logger for Junit logging. Log messages will be associated with the MD5 class.
     */
//    private static Logger LOGGER = Logger.getLogger(MD5SequenceIdentifier.class);

    /**
     * Pattern to test a string is a hex MD5 digest.
     */
    private static final Pattern MD5_PATTERN = Pattern.compile("^[A-Fa-f0-9]{32}$");


    /**
     * Constructor that sets the MD5 String, having first converted it to upper case for consistency.
     * 
     * @param md5 being a hexdecimal representation of an MD5 digest.  Can be mixed case hex, will be converted to upper.
     */
    MD5SequenceIdentifier(String md5){
        super(
                (md5 == null) ? null : md5.toLowerCase(Locale.ENGLISH)
        );
    }

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected MD5SequenceIdentifier() {
    }

    /**
     * Determines if a String matches the regex pattern for an MD5 digest in Hex format.
     *
     * @param candidate to test against the MD5 digest
     * @return true if matches the regex pattern for an MD5 digest in Hex format.
     */
    static boolean isMD5(String candidate){
        return candidate != null && MD5_PATTERN.matcher(candidate).matches();
    }

}
