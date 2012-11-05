package uk.ac.ebi.interpro.scan.condensed.server.web.service;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Phil Jones
 */
public interface CondensedService {

    /**
     * Retrieves an HTML snippet for the condensed view, either
     * by sequence MD5 or (current) UniProt AC.
     *
     * @param id sequence MD5 or (current) UniProt AC.
     * @return an HTML snippet for the condensed view
     */
    String getCondensedHtml(String id);
}
