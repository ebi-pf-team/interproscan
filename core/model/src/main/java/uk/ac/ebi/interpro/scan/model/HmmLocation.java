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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * Location(s) of match on protein sequence
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@XmlType(name="HmmLocationType", propOrder={"start", "end"})
@Entity
public class HmmLocation
        extends AbstractLocation
        implements Location {

    @Column (nullable = false)
    private int hmmStart;

    @Column (nullable = false)
    private int hmmEnd;

    @Column (nullable = false)
    private HmmBounds hmmBounds;

    @Column (nullable = false)
    private double evalue;

    @Column (nullable = false)
    private double score;

    // TODO: Do we need private setters for Hibernate? (see http://www.javalobby.org/java/forums/t49288.html)

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected HmmLocation() {}

    // TODO: Use Builder pattern so constructor-args are obvious?
    public HmmLocation(int start, int end,
                       int hmmStart, int hmmEnd, HmmBounds hmmBounds,
                       double evalue, double score) {
        super(start, end);
        this.hmmStart  = hmmStart;
        this.hmmEnd    = hmmEnd;
        this.hmmBounds = hmmBounds;
        this.evalue    = evalue;
        this.score     = score;
    }

    @XmlAttribute(name="hmm-start", required=true)
    public int getHmmStart() {
        return hmmStart;
    }

    @XmlAttribute(name="hmm-end", required=true)
    public int getHmmEnd() {
        return hmmEnd;
    }

    // Not using adapter because XML schema contains eg. "COMPLETE" instead of "[]"
    // @XmlJavaTypeAdapter(HmmBounds.HmmBoundsAdapter.class)
    @XmlAttribute(name="hmm-bounds", required=true)
    public HmmBounds getHmmBounds() {
        return hmmBounds;
    }

    @XmlAttribute(name="evalue", required=true)
    public double getEValue() {
        return evalue;
    }

    @XmlAttribute(name="score", required=true)
    public double getScore() {
        return score;
    }

    // HMMER output notation for model match
    public static enum HmmBounds {

        COMPLETE("[]", "Complete"),
        N_TERMINAL_COMPLETE("[.", "N-terminal complete"),
        C_TERMINAL_COMPLETE(".]", "C-terminal complete"),
        INCOMPLETE("..", "Incomplete");

        private final String symbol;
        private final String description;

        private HmmBounds(String symbol, String description) {
            this.symbol = symbol;
            this.description = description;
        }

        public String getSymbol() {
            return symbol;
        }

        public String getDescription() {
            return description;
        }

        @Override public String toString() {
            return symbol;
        }

        /**
         * Returns enum corresponding to symbol, for example "[."
         *
         * @param   symbol  HmmBounds symbol, for example "[." or ".."
         * @return  Enum corresponding to symbol, for example "[."
         */
        public static HmmBounds parseSymbol(String symbol)  {
            for (HmmBounds hb : HmmBounds.values()) {
                if (symbol.equals(hb.getSymbol()))   {
                    return hb;
                }
            }
            throw new IllegalArgumentException("Unrecognised symbol: " + symbol);
        }

        /**
         * Map HmmBounds to and from XML representation
         */
        /*
        @XmlTransient
        static final class HmmBoundsAdapter extends XmlAdapter<String, HmmBounds> {
            // Map Java to XML type
            @Override public String marshal(HmmBounds hmmBounds) {
                return hmmBounds.getSymbol();
            }
            // Map XML type to Java
            @Override public HmmBounds unmarshal(String symbol) {
                return HmmBounds.parseSymbol(symbol);
            }
        }
        */

    }

    // TODO: Figure out which class to use (HmmMatch replaced by RawHmmMatch and FilteredHmmMatch)
    //@ManyToOne(targetEntity = HmmMatch.class)
    @XmlTransient
    @Override public Match getMatch() {
        return super.getMatch();
    }
}
