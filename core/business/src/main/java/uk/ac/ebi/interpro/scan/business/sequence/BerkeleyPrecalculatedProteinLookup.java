package uk.ac.ebi.interpro.scan.business.sequence;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.BerkeleyToI5ModelDAO;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatchXML;
import uk.ac.ebi.interpro.scan.precalc.client.MatchHttpClient;

import java.io.IOException;

/**
 * Looks up precalculated matches from the Berkeley WebService.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class BerkeleyPrecalculatedProteinLookup implements PrecalculatedProteinLookup {

    Logger LOGGER = Logger.getLogger(BerkeleyPrecalculatedProteinLookup.class.getName());

    /**
     * This client is used to check for existing matches
     * from a web service.  This web service will be provided directly
     * from the EBI, but can also be easily installed locally.
     * (Runs from a BerkeleyDB on Jetty, so can be run from a single
     * command).
     * <p/>
     * Essentially, the client will be used if it is available.
     */
    private MatchHttpClient preCalcMatchClient;

    private BerkeleyToI5ModelDAO berkeleyToI5DAO;

    public BerkeleyPrecalculatedProteinLookup() {
    }

    @Required
    public void setBerkeleyToI5DAO(BerkeleyToI5ModelDAO berkeleyToI5DAO) {
        this.berkeleyToI5DAO = berkeleyToI5DAO;
    }

    /**
     * This client is used to check for existing matches
     * from a web service.  This web service will be provided directly
     * from the EBI, but can also be easily installed locally.
     * (Runs from a BerkeleyDB on Jetty, so can be run from a single
     * command).
     * <p/>
     * Essentially, the client will be used if it is available.
     */
    @Required
    public void setPreCalcMatchClient(MatchHttpClient preCalcMatchClient) {
        this.preCalcMatchClient = preCalcMatchClient;
    }

    /**
     * Note - this method returns null if there are no precalculated results.
     *
     * @param protein
     * @return
     */
    @Override
    public Protein getPrecalculated(Protein protein) {
        // First, check if the MD5 needs to be reanalyzed

        try {
            final String upperMD5 = protein.getMd5().toUpperCase();
            if (preCalcMatchClient.getMD5sOfProteinsToAnalyse(upperMD5).contains(upperMD5)) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Protein with MD5 " + upperMD5 + " has been returned from the web service, so needs to be analysed.");
                }
                return null;  // Needs to be analysed.
            }
            Protein precalcProtein = null;
            // Now retrieve the Matches and add to the protein.
            final BerkeleyMatchXML berkeleyMatchXML = preCalcMatchClient.getMatches(upperMD5);
            berkeleyToI5DAO.populateProteinMatches(precalcProtein, berkeleyMatchXML.getMatches());
            return precalcProtein;
        } catch (IOException e) {
            /* Don't overreact,  just log the error - I5 can continue and analyse the proteins
             even if the precalc lookup service is not working / configured correctly. */
            LOGGER.error("Non-fatal error - the pre-calculated match lookup service is not correctly configured and has thrown an IOException.  " +
                    "InterProScan 5 will continue to analyse the proteins.  You may wish to check your configuration for more efficient " +
                    "match calculation, or to disable the use of this service.", e);
        }
        return null;
    }
}
