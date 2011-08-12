package uk.ac.ebi.interpro.scan.business.sequence;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.BerkeleyToI5ModelDAO;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatchXML;
import uk.ac.ebi.interpro.scan.precalc.client.MatchHttpClient;

import java.io.IOException;
import java.util.*;

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
        // Check if the precalc service is configure and available.
        if (!preCalcMatchClient.isConfigured()) {
            return null;
        }

        // First, check if the MD5 needs to be reanalyzed

        try {
            final String upperMD5 = protein.getMd5().toUpperCase();

            if (!preCalcMatchClient.getMD5sOfProteinsAlreadyAnalysed(upperMD5).contains(upperMD5)) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Protein with MD5 " + upperMD5 + " has been analysed previously, so needs to be analysed.");
                }
                return null;  // Needs to be analysed.
            }
            // Now retrieve the Matches and add to the protein.
            Long startTime = null;
            if (LOGGER.isDebugEnabled()) {
                startTime = System.nanoTime();
            }
            final BerkeleyMatchXML berkeleyMatchXML = preCalcMatchClient.getMatches(upperMD5);
            if (LOGGER.isDebugEnabled()) {
                long time = System.nanoTime() - startTime;
                LOGGER.debug("Time to lookup " + berkeleyMatchXML.getMatches().size() + " matches for one protein: " + time + "ns");
            }
            if (berkeleyMatchXML != null) {
                berkeleyToI5DAO.populateProteinMatches(protein, berkeleyMatchXML.getMatches());
            }
            return protein;
        } catch (IOException e) {
            /* Don't overreact,  just log the error - I5 can continue and analyse the proteins
             even if the precalc lookup service is not working / configured correctly. */
            LOGGER.error("Non-fatal error - the pre-calculated match lookup service is not correctly configured and has thrown an IOException.  " +
                    "InterProScan 5 will continue to analyse the proteins.  You may wish to check your configuration for more efficient " +
                    "match calculation, or to disable the use of this service.", e);
        } catch (IllegalStateException ise) {
            LOGGER.error("The precalculated match lookup service has not been configured correctly and has thrown an " +
                    "IllegalStateException.  This is NOT fatal however - InterProScan 5 will continue to " +
                    "analyse the proteins.  You may wish to check your configuration for more efficient match " +
                    "calculation, or to disable the use of this service." + ise);
        }
        return null;
    }

    @Override
    public Set<Protein> getPrecalculated(Set<Protein> proteins) {
        // Check if the precalc service is configure and available.
        if (!preCalcMatchClient.isConfigured()) {
            return null;
        }

        try {
            // First, check if the MD5s have been precalculated
            String[] md5s = new String[proteins.size()];
            // Map for looking up proteins by MD5 efficiently.
            final Map<String, Protein> md5ToProteinMap = new HashMap<String, Protein>(proteins.size());
            int i = 0;
            // Both populate the lookup map and also create the array of MD5s to query the service.
            for (Protein protein : proteins) {
                md5ToProteinMap.put(protein.getMd5().toUpperCase(), protein);
                md5s[i++] = protein.getMd5().toUpperCase();
            }
            final List<String> analysedMd5s = preCalcMatchClient.getMD5sOfProteinsAlreadyAnalysed(md5s);

            // Check if NONE have been pre-calculated - if so, return empty set.
            if (analysedMd5s == null || analysedMd5s.size() == 0) {
                return Collections.emptySet();
            }

            // Create a Set of proteins that have been precalculated - this is what will end up being returned.
            final Set<Protein> precalculatedProteins = new HashSet<Protein>(analysedMd5s.size());

            // For the MD5s of proteins that have been pre-calculated, retrieve match data and populate the proteins.
            md5s = new String[analysedMd5s.size()];
            i = 0;
            for (String md5 : analysedMd5s) {
                final String md5Upper = md5.toUpperCase();
                md5s[i++] = md5Upper;
                precalculatedProteins.add(md5ToProteinMap.get(md5Upper));
            }
            Long startTime = null;
            if (LOGGER.isDebugEnabled()) {
                startTime = System.nanoTime();
            }
            final BerkeleyMatchXML berkeleyMatchXML = preCalcMatchClient.getMatches(md5s);
            if (LOGGER.isDebugEnabled()) {
                long time = System.nanoTime() - startTime;
                LOGGER.debug("Time to lookup " + berkeleyMatchXML.getMatches().size() + " matches for " + md5s.length + " proteins: " + time + "ns");
            }
            if (berkeleyMatchXML != null) {
                berkeleyToI5DAO.populateProteinMatches(precalculatedProteins, berkeleyMatchXML.getMatches());
            }
            return precalculatedProteins;
        } catch (IOException e) {
            /* Don't overreact,  just log the error - I5 can continue and analyse the proteins
             even if the precalc lookup service is not working / configured correctly. */
            LOGGER.error("Non-fatal error - the pre-calculated match lookup service is not correctly configured and has thrown an IOException.  " +
                    "InterProScan 5 will continue to analyse the proteins.  You may wish to check your configuration for more efficient " +
                    "match calculation, or to disable the use of this service.", e);
            return Collections.emptySet();
        }
    }
}
