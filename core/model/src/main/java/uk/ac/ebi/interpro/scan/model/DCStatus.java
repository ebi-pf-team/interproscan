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
 * DCStatus  notation for locationFragment
 *
 * @author  Gift Nuka
 * @version $Id$
 */
@XmlType(name="DCStatusType")
public enum DCStatus {

    CONTINUOUS("S", "continuous single chain domain"),
    N_TERMINAL_DISC("N", "N-terminal discontinuous "),
    C_TERMINAL_DISC("C", "C-terminal discontinuous"),
    NC_TERMINAL_DISC("(NC)", "N and C -terminal discontinuous");

    private static final Map<String, DCStatus> SYMBOL_TO_ENUM = new HashMap<String, DCStatus>(DCStatus.values().length);

    static{
        for (DCStatus dcStatus : DCStatus.values()){
            SYMBOL_TO_ENUM.put(dcStatus.getSymbol(), dcStatus);
        }
    }


    private final String symbol;
    private final String description;

    DCStatus(String symbol, String description) {
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
     * Returns enum corresponding to symbol, for example "N"
     *
     * @param   symbol  DCStatus symbol, for example "S" or "NC"
     * @return  Enum corresponding to symbol, for example "C"
     */
    public static DCStatus parseSymbol(String symbol)  {
        DCStatus dcStatus = SYMBOL_TO_ENUM.get(symbol);
        if (dcStatus == null){
            throw new IllegalArgumentException("Unrecognised symbol: " + symbol);
        }
        return dcStatus;
    }


    /**
     *  getNewDCStatus
     * @param statusOne
     * @param statusTwo
     * @return
     */
    public static DCStatus getNewDCStatus(DCStatus statusOne, DCStatus statusTwo) {
        DCStatus status = null;

        if (statusOne == statusTwo) {
            status = statusOne;
        } else if (statusOne ==  null || statusOne == DCStatus.CONTINUOUS) {
            status = statusTwo;
        } else if (statusTwo ==  null || statusTwo == DCStatus.CONTINUOUS) {
            status = statusOne;
        } else {
            status = DCStatus.NC_TERMINAL_DISC;
        }
        return status;
    }


}
