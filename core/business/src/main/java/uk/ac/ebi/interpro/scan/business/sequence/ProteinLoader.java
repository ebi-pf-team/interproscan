package uk.ac.ebi.interpro.scan.business.sequence;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.getorf.GetOrfDescriptionLineParser;
import uk.ac.ebi.interpro.scan.io.sequence.XrefParser;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.persistence.NucleotideSequenceDAO;
import uk.ac.ebi.interpro.scan.persistence.OpenReadingFrameDAO;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;


import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class knows how to store protein sequences and cross references
 * <p/>
 * This must be a system-wide Singleton - achieved by ONLY injecting into the
 * SerialWorker JVM, from Spring.
 *
 * @author Phil Jones
 *         Date: 14-Nov-2009
 *         Time: 14:04:59
 */
public class ProteinLoader implements SequenceLoader<Protein> {

    private static final Logger LOGGER = Logger.getLogger(ProteinLoader.class.getName());

    private PrecalculatedProteinLookup proteinLookup;

    private ProteinDAO proteinDAO;

    private NucleotideSequenceDAO nucleotideSequenceDAO;

    private OpenReadingFrameDAO openReadingFrameDAO;

    private int proteinInsertBatchSize;

    private int proteinInsertBatchSizeNoLookup;

    private int proteinPrecalcLookupBatchSize;

    private Set<Protein> proteinsAwaitingPrecalcLookup;

    private Set<Protein> proteinsAwaitingPersistence;

    private Set<Protein> precalculatedProteins = new HashSet<>();

    private Long bottomProteinId;

    private Long topProteinId;

    private boolean isGetOrfOutput;

    private GetOrfDescriptionLineParser descriptionLineParser;

    public void setProteinLookup(PrecalculatedProteinLookup proteinLookup) {
        this.proteinLookup = proteinLookup;
    }

    @Required
    public void setProteinInsertBatchSize(int proteinInsertBatchSize) {
        this.proteinInsertBatchSize = proteinInsertBatchSize;
        proteinsAwaitingPersistence = new HashSet<>(proteinInsertBatchSize);
    }

    @Required
    public void setProteinInsertBatchSizeNoLookup(int proteinInsertBatchSizeNoLookup) {
        this.proteinInsertBatchSizeNoLookup = proteinInsertBatchSizeNoLookup;
    }

    @Required
    public void setGetOrfOutput(boolean getOrfOutput) {
        isGetOrfOutput = getOrfOutput;
    }

    @Required
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    @Required
    public void setProteinPrecalcLookupBatchSize(int proteinPrecalcLookupBatchSize) {
        this.proteinPrecalcLookupBatchSize = proteinPrecalcLookupBatchSize;
        proteinsAwaitingPrecalcLookup = new HashSet<>(proteinPrecalcLookupBatchSize);
    }

    public void setDescriptionLineParser(GetOrfDescriptionLineParser descriptionLineParser) {
        this.descriptionLineParser = descriptionLineParser;
    }

    public void setNucleotideSequenceDAO(NucleotideSequenceDAO nucleotideSequenceDAO) {
        this.nucleotideSequenceDAO = nucleotideSequenceDAO;
    }

    public void setOpenReadingFrameDAO(OpenReadingFrameDAO openReadingFrameDAO) {
        this.openReadingFrameDAO = openReadingFrameDAO;
    }

    /**
     * This method stores sequences with (optionally) cross references.
     * The method attempts to store them in batches by calling the addProteinToBatch(Protein protein)
     * method.  This in turn calls persistBatch(), when the batch size has been reached.
     * <p/>
     *
     * @param sequence        being the protein sequence to store
     * @param crossReferences being a set of Cross references.
     */
    public void store(String sequence, Map<String, SignatureLibraryRelease> analysisJobMap, String... crossReferences) {
        if (sequence != null && sequence.length() > 0) {
            Protein protein = new Protein(sequence);
            if (crossReferences != null) {
                for (String crossReference : crossReferences) {
                    ProteinXref xref = XrefParser.getProteinXref(crossReference);
                    protein.addCrossReference(xref);
                }
            }
            proteinsAwaitingPrecalcLookup.add(protein);
            if (proteinsAwaitingPrecalcLookup.size() > proteinPrecalcLookupBatchSize) {
                lookupProteins(analysisJobMap);
            }
        }
    }

    /**
     *
     * @param analysisJobMap
     */
    private void lookupProteins(Map<String, SignatureLibraryRelease> analysisJobMap) {
        if (proteinsAwaitingPrecalcLookup.size() > 0) {
            final boolean usingLookupService = proteinLookup != null;
            if (! usingLookupService){
//                Utilities.verboseLog("proteinsAwaitingPrecalcLookup.size() " + proteinsAwaitingPersistence.size()
//                        + " proteinInsertBatchSize: " + proteinInsertBatchSize
//                        + " proteinInsertBatchSizeNoLookup: " + proteinInsertBatchSizeNoLookup);
                //update the insertbatch size
                proteinInsertBatchSize = proteinInsertBatchSizeNoLookup;
//                Utilities.verboseLog("After update - proteinsAwaitingPrecalcLookup.size() " + proteinsAwaitingPersistence.size()
//                        + " proteinInsertBatchSize: " + proteinInsertBatchSize
//                        + " proteinInsertBatchSizeNoLookup: " + proteinInsertBatchSizeNoLookup);
            }
            Set<Protein> localPrecalculatedProteins = (usingLookupService)
                    ? proteinLookup.getPrecalculated(proteinsAwaitingPrecalcLookup, analysisJobMap)
                    : null;

//            if(proteinLookup.isAnalysisVersionConsistent(analysisJobMap)){
//
//            }

            // Put precalculated proteins into a Map of MD5 to Protein;
            if (localPrecalculatedProteins != null) {
                final Map<String, Protein> md5ToPrecalcProtein = new HashMap<>(localPrecalculatedProteins.size());
                for (Protein precalc : localPrecalculatedProteins) {
                    md5ToPrecalcProtein.put(precalc.getMd5(), precalc);
                }

                for (Protein protein : proteinsAwaitingPrecalcLookup) {
                    if (md5ToPrecalcProtein.keySet().contains(protein.getMd5())) {
                        precalculatedProteins.add(md5ToPrecalcProtein.get(protein.getMd5()));
                    } else {
                        addProteinToBatch(protein);
                    }
                }
            } else {
                //there are no matches or we are not using the lookup match service
                //

                for (Protein protein : proteinsAwaitingPrecalcLookup) {
                    addProteinToBatch(protein);
                }
            }
            // All dealt with, so clear.
            proteinsAwaitingPrecalcLookup.clear();
        }
    }

    /**
     * Adds a protein to the batch of proteins to be persisted.  If the maximum
     * batch size is reached, store all these proteins (by calling persistBatch().)
     *
     * @param protein being the protein to be stored.
     */
    private void addProteinToBatch(Protein protein) {
        proteinsAwaitingPersistence.add(protein);

        if (proteinsAwaitingPersistence.size() == proteinInsertBatchSize) {
            Utilities.verboseLog("proteinInsertBatchSize " + proteinInsertBatchSize);
            persistBatch();
        }
    }

    /**
     * Persists all of the proteins in the list of proteinsAwaitingPersistence and empties
     * this Collection, ready to be used again.
     */
    private void persistBatch() {
        LOGGER.debug("ProteinLoader.persistBatch() method has been called.");
        if (proteinsAwaitingPersistence.size() > 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Persisting " + proteinsAwaitingPersistence.size() + " proteins");
            }
            Utilities.verboseLog("Persisting " + proteinsAwaitingPersistence.size() + " proteins");
            final ProteinDAO.PersistedProteins persistedProteins = proteinDAO.insertNewProteins(proteinsAwaitingPersistence);
            bottomProteinId = persistedProteins.updateBottomProteinId(bottomProteinId);
            topProteinId = persistedProteins.updateTopProteinId(topProteinId);
            if (isGetOrfOutput) {
                createAndPersistNewORFs(persistedProteins);
            }
            proteinsAwaitingPersistence.clear();
        }
    }

    /**
     * Following persistence of proteins, calls the SequenceLoadListener with the bounds of the proteins
     * added, so analyses (StepInstance) can be created appropriately.
     *
     * @param sequenceLoadListener which handles the creation of StepInstances for the new proteins added.
     */
    public void persist(SequenceLoadListener sequenceLoadListener, Map<String, SignatureLibraryRelease> analysisJobMap) {
        // Check any remaining proteins awaiting lookup
        lookupProteins(analysisJobMap);

        // Persist any remaining proteins (that last batch)
        persistBatch();

        // Grab hold of the lower and upper range of Protein IDs for ALL of the persisted proteins
        final Long bottomNewProteinId = bottomProteinId;
        final Long topNewProteinId = topProteinId;

        // Prepare the ProteinLoader for another set of proteins.
        resetBounds();


        // Now store the precalculated proteins (just updates - these should not be included in the
        // list of Proteins for the listener.)
        for (Protein precalculatedProtein : precalculatedProteins) {
            addProteinToBatch(precalculatedProtein);
        }

        persistBatch();

        final Long bottomPrecalcProteinId = bottomProteinId;
        final Long topPrecalcProteinId = topProteinId;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Protein ID ranges:");
            LOGGER.debug("Bottom new protein: " + bottomNewProteinId);
            LOGGER.debug("Top new protein:" + topNewProteinId);
            LOGGER.debug("Bottom precalc protein: " + bottomPrecalcProteinId);
            LOGGER.debug("Top precalc protein: " + topPrecalcProteinId);
        }

        sequenceLoadListener.sequencesLoaded(bottomNewProteinId, topNewProteinId, bottomPrecalcProteinId, topPrecalcProteinId);
        // Prepare the ProteinLoader for another set of proteins.
        resetBounds();
    }

    public void setUseMatchLookupService(boolean useMatchLookupService) {
        if (!useMatchLookupService || proteinLookup == null || !proteinLookup.isConfigured()) {
            this.proteinLookup = null;
            System.out.println("Pre-calculated match lookup service DISABLED.  Please wait for match calculations to complete...");
        } else {
            System.out.println("Available matches will be retrieved from the pre-calculated match lookup service.\n\n" +
                    "Matches for any sequences that are not represented in the lookup service will be calculated locally.");
        }
    }

    /**
     * Persists proteins that have been collapsed and annotated with ProteinXrefs
     * by a separate process, e.g. the fasta file loader.
     *
     * @param parsedProteins   being a Collection of non-redundant Proteins and Xrefs.
     * @param analysisJobMap for analysisJobNames to be included in analysis.
     */
    public void storeAll(Set<Protein> parsedProteins, Map<String, SignatureLibraryRelease> analysisJobMap) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Storing " + parsedProteins.size() + " proteins in batches of " + proteinPrecalcLookupBatchSize);
        }
        //TODO: do notify() run this step when lookupProteins() is disabled
        //complicated logic here
        int count = 0;
        SimpleDateFormat sdf =  new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");
        for (Protein protein : parsedProteins) {
            count++;
            proteinsAwaitingPrecalcLookup.add(protein);
            if (proteinsAwaitingPrecalcLookup.size() > proteinPrecalcLookupBatchSize) {
                lookupProteins(analysisJobMap);
            }
            if(count % 4000 == 0){
                if (count % 16000 == 0) {
                    //TODO use utilities.verboselog to log this
                    Utilities.verboseLog("Stored " + count + " sequences");
                    //System.out.println(sdf.format(Calendar.getInstance().getTime()) + " Stored " + count + " sequences");

                }else{
                    if(LOGGER.isInfoEnabled()){
                        LOGGER.info( "Stored " + count + " sequences");
                    }
                }
            }
        }
        if (count > 10000) {
            System.out.println(sdf.format(Calendar.getInstance().getTime()) + " Uploaded/Stored " + count + " sequences for analysis");
        }
        Utilities.verboseLog(10, " Uploaded/Stored " + count + " sequences for analysis");

        if(LOGGER.isInfoEnabled()) {
            LOGGER.info("Persisting protein sequences completed, stored " + count + "proteins");
        }
        Utilities.sequenceCount = count;
    }

    /**
     * Helper method that sets the upper and lower bounds to null.
     */
    private void resetBounds() {
        bottomProteinId = null;
        topProteinId = null;
    }

    private void createAndPersistNewORFs(final ProteinDAO.PersistedProteins persistedProteins) {
        //Holder for new ORFs which should be persisted
        Set<OpenReadingFrame> orfsAwaitingPersistence = new HashSet<>();

        Set<Protein> newProteins = persistedProteins.getNewProteins();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Persisted " + newProteins.size() + " new proteins and their cross references.");
            LOGGER.debug("Iterating over all new proteins and their xrefs...");
        }
        for (Protein newProtein : newProteins) {
            Set<ProteinXref> xrefs = newProtein.getCrossReferences();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Protein with ID " + newProtein.getId() + " has " + xrefs.size() + " cross references.");
            }
            for (ProteinXref xref : xrefs) {
                String nucleotideId = xref.getIdentifier();
                String description = xref.getDescription();
                OpenReadingFrame newOrf = descriptionLineParser.createORFFromParsingResult(description);
                //Get rid of the underscore
                nucleotideId = XrefParser.stripOfFinalUnderScore(nucleotideId);
                /*
                  Commented-out version number stripping to allow the short-term fix for nucleotide headers to work (IBU-2426)
                  TODO - consider if this is really necessary (may not be a good idea in all cases)
                */
                //Get rid of those pesky version numbers too
                //nucleotideId = XrefParser.stripOfVersionNumberIfExists(nucleotideId);
                NucleotideSequence nucleotide = nucleotideSequenceDAO.retrieveByXrefIdentifier(nucleotideId);
                //In cases the FASTA file contained sequences from ENA or any other database (e.g. ENA|AACH01000026|AACH01000026.1 Saccharomyces)
                //the nucleotide can be NULL and therefore we need to get the nucleotide sequence by name
                if (nucleotide == null) {
                    if (LOGGER.isDebugEnabled()) {
                        List<NucleotideSequence> seqs= nucleotideSequenceDAO.retrieveAll();
                        for (NucleotideSequence ns: seqs) {
                            Set<NucleotideSequenceXref> nsXrefs = ns.getCrossReferences() ;
                            for (NucleotideSequenceXref nsXref: nsXrefs) {
                                LOGGER.debug("Nucleotide xref identifier: " + nsXref.getIdentifier());
                            }
                        }
                    }
                    throw new IllegalStateException("Couldn't find nucleotide sequence by the following cross reference: " + nucleotideId);

                }
                newOrf.setNucleotideSequence(nucleotide);
                newOrf.setProtein(newProtein);
                newProtein.addOpenReadingFrame(newOrf);
                orfsAwaitingPersistence.add(newOrf);
            }
        }
        //Finally persist open reading frames
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Persisting " + orfsAwaitingPersistence.size() + " new open reading frames.");
        }
        openReadingFrameDAO.insert(orfsAwaitingPersistence);
        openReadingFrameDAO.flush();
    }


}
