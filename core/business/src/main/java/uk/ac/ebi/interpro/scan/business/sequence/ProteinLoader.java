package uk.ac.ebi.interpro.scan.business.sequence;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.getorf.GetOrfDescriptionLineParser;
import uk.ac.ebi.interpro.scan.io.sequence.XrefParser;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.persistence.NucleotideSequenceDAO;
import uk.ac.ebi.interpro.scan.persistence.OpenReadingFrameDAO;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
public class ProteinLoader implements SequenceLoader {

    private static final Logger LOGGER = Logger.getLogger(ProteinLoader.class.getName());

    private PrecalculatedProteinLookup proteinLookup;

    private ProteinDAO proteinDAO;

    private NucleotideSequenceDAO nucleotideSequenceDAO;

    private OpenReadingFrameDAO openReadingFrameDAO;

    private int proteinInsertBatchSize;

    private int proteinPrecalcLookupBatchSize;

    private Set<Protein> proteinsAwaitingPrecalcLookup;

    private Set<Protein> proteinsAwaitingPersistence;

    private Set<Protein> precalculatedProteins = new HashSet<Protein>();

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
        proteinsAwaitingPersistence = new HashSet<Protein>(proteinInsertBatchSize);
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
        proteinsAwaitingPrecalcLookup = new HashSet<Protein>(proteinPrecalcLookupBatchSize);
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
     * TODO - Needs to be refactored - currently does NOT store anything other than the Xref accession.
     * TODO - needs to be able to store the database name and the protein name too.
     *
     * @param sequence        being the protein sequence to store
     * @param crossReferences being a set of Cross references.
     */
    public void store(String sequence, String... crossReferences) {
        if (sequence != null && sequence.length() > 0) {
            Protein protein = new Protein(sequence);
            if (crossReferences != null) {
                for (String crossReference : crossReferences) {
                    ProteinXref xref = XrefParser.getProteinXref(crossReference);
                    protein.addCrossReference(xref);

                    // TODO - At this point, if this is the output of GetOrf being parsed, need to
                    // parse out the start and end coordinates relative to the nucleic acid,
                    // retrieve the correct nucleic acid and create an OpenReadingFrame object
                    // to relate the Protein to the NucleicAcid.
//                    if (isGetOrfOutput) {
//                    TODO: Moved this part to a different method called createAndPersistNewORFs()
//                    }
                }
            }
            proteinsAwaitingPrecalcLookup.add(protein);
            if (proteinsAwaitingPrecalcLookup.size() > proteinPrecalcLookupBatchSize) {
                lookupProteins();
            }
        }
    }

    private void lookupProteins() {
        if (proteinsAwaitingPrecalcLookup.size() > 0) {
            Set<Protein> localPrecalculatedProteins = (proteinLookup != null)
                    ? proteinLookup.getPrecalculated(proteinsAwaitingPrecalcLookup)
                    : null;
            // Put precalculated proteins into a Map of MD5 to Protein;
            if (localPrecalculatedProteins != null) {
                final Map<String, Protein> md5ToPrecalcProtein = new HashMap<String, Protein>(localPrecalculatedProteins.size());
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
            persistBatch();
        }
    }

    /**
     * Persists all of the proteins in the list of proteinsAwaitingPersistence and empties
     * this Collection, ready to be used again.
     */
    private void persistBatch() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("ProteinLoader.persistBatch() method has been called.");
        }
        if (proteinsAwaitingPersistence.size() > 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Persisting " + proteinsAwaitingPersistence.size() + " proteins");
            }
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
    public void persist(SequenceLoadListener sequenceLoadListener) {
        // Check any remaining proteins awaiting lookup
        lookupProteins();

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

    /**
     * Helper method that sets the upper and lower bounds to null.
     */
    private void resetBounds() {
        bottomProteinId = null;
        topProteinId = null;
    }

    private void createAndPersistNewORFs(final ProteinDAO.PersistedProteins persistedProteins) {
        //Holder for new ORFs which should be persisted
        Set<OpenReadingFrame> orfsAwaitingPersistence = new HashSet<OpenReadingFrame>();

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
                //
                String identifier = xref.getIdentifier();
                String[] chunks = descriptionLineParser.parseGetOrfDescriptionLine(identifier);
                OpenReadingFrame newOrf = descriptionLineParser.createORFFromParsingResult(chunks);
                if (newOrf != null) {
                    String nucleotideId = descriptionLineParser.getIdentifier(identifier);
                    NucleotideSequence nucleotide = nucleotideSequenceDAO.retrieveByXrefIdentifier(nucleotideId);
                    //In cases the FASTA file contained sequences from ENA or any other database (e.g. ENA|AACH01000026|AACH01000026.1 Saccharomyces)
                    //the nucleotide can be NULL and therefore we need to get the nucleotide sequence by name
                    if (nucleotide == null) {
                        nucleotide = nucleotideSequenceDAO.retrieveByXrefName(nucleotideId);
                    }
                    if (nucleotide != null) {
                        newOrf.setNucleotideSequence(nucleotide);
                        newOrf.setProtein(newProtein);
                        newProtein.addOpenReadingFrame(newOrf);
                        orfsAwaitingPersistence.add(newOrf);
                    } else {
                        LOGGER.warn("Couldn't find nucleotide sequence by the following identifier: " + nucleotideId);
                    }
                } else {
                    LOGGER.warn("Couldn't create any ORF object by the specified chunks: " + chunks + "!");
                }
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