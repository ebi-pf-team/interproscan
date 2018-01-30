/*
 * Copyright 2009-2010 the original author or authors.
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

import javax.xml.bind.annotation.XmlType;
import java.util.HashMap;
import java.util.Map;

/**
 * HMMER output notation for model match
 *
 * @author  Antony Quinn
 * @author  Phil Jones
 * @version $Id$
 */
@XmlType(name="HmmBoundsType")
public enum HmmBounds {

    COMPLETE("[]", "Complete"),
    N_TERMINAL_COMPLETE("[.", "N-terminal complete"),
    C_TERMINAL_COMPLETE(".]", "C-terminal complete"),
    INCOMPLETE("..", "Incomplete");

    private static final Map<String, HmmBounds> SYMBOL_TO_ENUM = new HashMap<String, HmmBounds>(HmmBounds.values().length);

    static{
        for (HmmBounds bounds : HmmBounds.values()){
            SYMBOL_TO_ENUM.put(bounds.getSymbol(), bounds);
        }
    }


    private final String symbol;
    private final String description;

    HmmBounds(String symbol, String description) {
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
        HmmBounds bound = SYMBOL_TO_ENUM.get(symbol);
        if (bound == null){
            throw new IllegalArgumentException("Unrecognised symbol: " + symbol);
        }
        return bound;
    }

    /**
     * Calculate HMM bounds
     *
     * E.g. for env 1 - 330:
     * ali 1 - 330, hmmBounds = []
     * ali 3 - 330, hmmBounds = .]
     * ali 1 - 209, hmmBounds =  [.
     * ali 3 - 209, hmmBounds = ..
     *
     * @param envStart
     * @param envEnd
     * @param aliStart
     * @param aliEnd
     * @return
     */
    public static String calculateHmmBounds(int envStart, int envEnd, int aliStart, int aliEnd) {
        String hmmBounds;
        if (envStart == aliStart) {
            hmmBounds = "[";
        }
        else {
            hmmBounds = ".";
        }
        if (envEnd == aliEnd) {
            hmmBounds += "]";
        }
        else {
            hmmBounds += ".";
        }
        assert HmmBounds.parseSymbol(hmmBounds) != null;
        return hmmBounds;
    }

}
