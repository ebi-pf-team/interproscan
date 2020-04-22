package uk.ac.ebi.interpro.scan.business.sequence.fasta;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.business.sequence.SequenceLoadListener;
import uk.ac.ebi.interpro.scan.business.sequence.SequenceLoader;
import uk.ac.ebi.interpro.scan.io.getorf.GetOrfDescriptionLineParser;
import uk.ac.ebi.interpro.scan.io.ntranslate.ORFDescriptionLineParser;
import uk.ac.ebi.interpro.scan.io.sequence.XrefParser;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.persistence.NucleotideSequenceDAO;
import uk.ac.ebi.interpro.scan.persistence.OpenReadingFrameDAO;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import org.iq80.leveldb.DB;
import static org.iq80.leveldb.impl.Iq80DBFactory.factory;
import org.iq80.leveldb.Options;

import org.apache.commons.lang3.SerializationUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Phil Jones
 *         Date: 14-Nov-2009
 *         Time: 09:27:14
 *         <p/>
 *         Parses Fasta file (Protein or nucleic acid) and uses a SequenceLoader to load the sequences
 *         into the database.
 *         <p/>
 *         T is "Protein" or "NucleotideSequence"
 */
public class LoadFastaFileIntoDBImpl<T> implements LoadFastaFile {

    private static final Logger LOGGER = Logger.getLogger(LoadFastaFileIntoDBImpl.class.getName());

    private SequenceLoader<Protein> sequenceLoader;

    protected static final Pattern WHITE_SPACE_PATTERN = Pattern.compile("\\s+");

    private ProteinDAO proteinDAO;

    String levelDBStoreRoot;

    String levelDBStoreName;

    private NucleotideSequenceDAO nucleotideSequenceDAO;

    private OpenReadingFrameDAO openReadingFrameDAO;

    private boolean isGetOrfOutput;

    private ORFDescriptionLineParser orfDescriptionLineParser;
    private GetOrfDescriptionLineParser descriptionLineParser;

    @Required
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    //    @Override
    public void setLevelDBStoreRoot(String levelDBStoreRoot) {
        this.levelDBStoreRoot = levelDBStoreRoot;
    }

    @Override
    @Required
    public void setSequenceLoader(SequenceLoader sequenceLoader) {
        this.sequenceLoader = sequenceLoader;
    }

    @Required
    public void setGetOrfOutput(boolean getOrfOutput) {
        isGetOrfOutput = getOrfOutput;
    }

    public void setOrfDescriptionLineParser(ORFDescriptionLineParser orfDescriptionLineParser) {
        this.orfDescriptionLineParser = orfDescriptionLineParser;
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

    @Override
    @Transactional
    public void loadSequences(InputStream fastaFileInputStream, SequenceLoadListener sequenceLoaderListener,Map<String, SignatureLibraryRelease> analysisJobMap, boolean useMatchLookupService) {
        sequenceLoader.setDisplayLookupMessage(true);
        sequenceLoader.setUseMatchLookupService(useMatchLookupService); //set lookup and display message
        LOGGER.debug("Entered LoadFastaFileImpl.loadSequences() method");
        Utilities.verboseLog("Entered LoadFastaFiletoDBImpl.loadSequences() method");
        int sequencesParsed = 0;

        levelDBStoreName = levelDBStoreRoot + "/leveldb";
        LOGGER.debug("levelDBStoreName: " + levelDBStoreName);
//        levelDBStore = getLevelDBStore(levelDBStoreName);
        try (BufferedReader  reader = new BufferedReader(new InputStreamReader(fastaFileInputStream))) {
            String currentId = null;
            final StringBuffer currentSequence = new StringBuffer();
            int lineNumber = 0;
            String line;
            boolean foundIdLine = false;

            final Set<Protein> parsedMolecules = new HashSet<>();

            Utilities.verboseLog("start Parsing  input file stream");
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.length() > 0) {
                    if ('>' == line.charAt(0)) {
                        // Found ID line.
                        foundIdLine = true;
                        // Store previous record, if it exists.
                        if (currentId != null) {
                            if (LOGGER.isDebugEnabled()) {
//                                if (sequencesParsed % 500 == 0) {
//                                    LOGGER.debug("Stored " + sequencesParsed + " sequences.");
//                                    if (LOGGER.isTraceEnabled()) {
//                                        LOGGER.trace("Current id: " + currentId);
//                                        LOGGER.trace("Current sequence: '" + currentSequence + "'");
//                                    }
//                                }
                                if (LOGGER.isTraceEnabled()) {
                                    Matcher seqCheckMatcher = Protein.AMINO_ACID_PATTERN.matcher(currentSequence);
                                    if (!seqCheckMatcher.matches()) {
                                        LOGGER.warn("Strange sequence parsed from FASTA file, does not match the Protein AMINO_ACID_PATTERN regex:\n" + currentSequence);
                                    }
                                }
                            }
                            final String seq = currentSequence.toString();
                            if (seq.trim().length() > 0) {
                                addToMoleculeCollectionInDB(seq, currentId, parsedMolecules);
                                sequencesParsed++;
                            }
//                            if (sequencesParsed == 1000 ) {
//                                //check if we are running nucleotides
//                               if (parsedMolecules.iterator().next() instanceof NucleotideSequence){
//                                    LOGGER.info("You are analysing more than 1000 nucleotide sequences. " +
//                                            " Either use an external tool to translate the sequences or Chunk the input and then send the chunks to InterProScan. Refer to " +
//                                            " https://github.com/ebi-pf-team/interproscan/wiki/ScanNucleicAcidSeqs#improving-performance");
//                                }
//                                throw new IllegalStateException("Input error - nucleotide sequence  count : " + sequencesParsed);
//                            }

                            currentSequence.delete(0, currentSequence.length());
                            if (sequencesParsed % 4000 == 0) {
                                if (sequencesParsed % 16000 == 0) {
                                    //TODO use utilities.verboselog
                                    Utilities.verboseLog("Parsed " + sequencesParsed + " sequences");
                                    //System.out.println(sdf.format(Calendar.getInstance().getTime()) + " Parsed " + sequencesParsed + " sequences");
                                }else{
                                    if(LOGGER.isInfoEnabled()){
                                        LOGGER.info( "Parsed " + sequencesParsed + " sequences");
                                    }
                                }

                            }
                        }
                        currentId = parseId(line, lineNumber);
                    } else {
                        // must be a sequence line.
                        if (foundIdLine) {
                            currentSequence.append(line.trim());
                        }
                        else {
                            // The sequence had no FASTA header, fatal user input error!
                            LOGGER.fatal("A FASTA input sequence had no header. Stopping now.");
                            System.out.println("Error: All input sequences should include their FASTA header lines.");
                            System.out.println("In the provided input, no FASTA header could be found before line: " + line);
                            System.out.println("No seqeuences have been processed.");
                            System.exit(999);
                            // Note: This doesn't stop this sort of issue, but we can't account for everything!
                            // > Seq 1
                            // Seq1Sequence
                            //
                            // Seq2Sequence
                        }
                    }
                }
            }
            // Store the final record (if there were any at all!)
            if (currentId != null) {
                addToMoleculeCollectionInDB(currentSequence.toString(), currentId, parsedMolecules);
                LOGGER.debug("About to call SequenceLoader.persist().");
            }

            int totalProteinsParsed = parsedMolecules.size();
            Utilities.verboseLog("Parsed Molecules (sequences) : " + totalProteinsParsed);

            // Now iterate over Proteins and store using Sequence Loader.
            LOGGER.info( "Store and persist the sequences");


            //Load in the h2DB  first
            final ProteinDAO.PersistedProteins persistedProteins = proteinDAO.insertNewProteins(parsedMolecules);



            Long bottomProteinId = persistedProteins.updateBottomProteinId(null);
            Long topProteinId = persistedProteins.updateTopProteinId(null);

            Utilities.verboseLog("Persisted " + topProteinId + " Molecules (sequences)");

            if(isGetOrfOutput){
                Utilities.verboseLog("Persisting  getOrfOutput topProteinId: " + topProteinId + " bottomProteinId: " + bottomProteinId);
                createAndPersistNewORFs(persistedProteins);
                Utilities.verboseLog("Completed Persisting  getOrfOutput ");
            }

            //then load into KV store using the sequenceIds
            //TODO get rid of lists that would baloon the memory usage
            //List<Protein> storedProteins = proteinDAO.getProteins(bottomProteinId, topProteinId);
            int count = 0;
            Long proteinIdIndex = bottomProteinId;
            //for (long proteinIndex = bottomProteinId; proteinIndex <= topProteinId; proteinIndex ++) {
            //    count++;
            //    String proteinKey = Long.toString(proteinIndex);
            //    //Protein protein = proteinDAO.getProteinAndCrossReferencesByProteinId(proteinIndex);
            //    Protein protein = proteinDAO.getProtein(proteinKey);

            int sliceSize = 10000;  //should this be higher?
            for (long bottomProteinOnSlice = bottomProteinId; bottomProteinOnSlice <= topProteinId; bottomProteinOnSlice += sliceSize) {
                final long topProteinOnSlice = Math.min(topProteinId, bottomProteinOnSlice + sliceSize - 1);
                List<Protein> storedProteins = proteinDAO.getProteinsBetweenIds(bottomProteinOnSlice, topProteinOnSlice);

                for (Protein protein : storedProteins) {
                    String sequenceId = Long.toString(protein.getId());

                    protein.getCrossReferences();
                    protein.getOpenReadingFrames();

                    for (OpenReadingFrame orf : protein.getOpenReadingFrames()) {
                        Utilities.verboseLog(30, "OpenReadingFrame: [" + protein.getId() + "]" + orf.getId() + " --  " + orf.getStart() + "-" + orf.getEnd());
                        NucleotideSequence seq = orf.getNucleotideSequence();
                        //Utilities.verboseLog("NucleotideSequence: \n" +  seq.toString());
                        if (seq != null && Utilities.verboseLogLevel >= 30) {
                            Utilities.verboseLog(30, "getCrossReferences().size" + seq.getCrossReferences().size());
                            Utilities.verboseLog(30, "getOpenReadingFrames().size" + seq.getOpenReadingFrames().size());
                        }
                    }
                    proteinDAO.insert(sequenceId, protein);
                    count++;
                }
            }
            Utilities.verboseLog("Stored " + count + " parsed sequences into KVDB: " + levelDBStoreName);

            Utilities.verboseLog("Completed Persisting new proteins in H2: topProteinId: " + topProteinId + " bottomProteinId: " + bottomProteinId);
            Utilities.verboseLog("Stored parsed sequences into H2DB: ");
            //sequenceLoader.storeAll(parsedMolecules, analysisJobMap);
            //Utilities.verboseLog("Store parsed sequences (processed lookup): " + parsedMolecules.size());

            sequenceLoaderListener.sequencesLoaded(bottomProteinId, topProteinId, null, null, useMatchLookupService, null);
            //sequenceLoader.persist(sequenceLoaderListener, analysisJobMap);
            LOGGER.info( "Store and persist the sequences ...  completed");
            Utilities.verboseLog("Store and persist the sequences into KV and H2 dbs...  completed");

            if (count > 12000) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");
                System.out.println(sdf.format(Calendar.getInstance().getTime()) + " Uploaded " + totalProteinsParsed + " unique sequences for analysis");
            }

        } catch (IOException e) {
            throw new IllegalStateException("Could not read the fastaFileInputStream. ", e);
        }
    }

    /**
     * Parses out an ID line, replaces white space with underscores in IDs
     *
     * @param line
     * @param lineNumber
     * @return
     */
    private String parseId(String line, int lineNumber) {
        String currentId = null;
        if (line.length() > 1) {
            currentId = line.substring(1).trim();
        }

        if (currentId == null || currentId.isEmpty()) {
            LOGGER.error("Found an empty ID line in the FASTA file on line " + lineNumber);
            throw new IllegalStateException("Found an empty ID line in the FASTA file on line " + lineNumber);
        } else if (currentId.length() > 255) {
            // ID line is too long to fit in the database column, so trim it!
            // TODO Really this line should be parsed properly!
            currentId = currentId.substring(0, 255);
        }
        return currentId;
    }

//    protected abstract void addToMoleculeCollection(String sequence, final String currentId, final Set<T> parsedMolecules);

    protected void addToMoleculeCollectionInDB(String sequence, final String currentId, final Set<Protein> parsedMolecules) {
        sequence = WHITE_SPACE_PATTERN.matcher(sequence).replaceAll("");
        Protein thisProtein = new Protein(sequence);

        // Check if this sequence is already in the Set.  If it is, retrieve it.
        boolean isMoleculeAdded = parsedMolecules.add(thisProtein);
        if (!isMoleculeAdded) {
            for (Protein existing : parsedMolecules) {
                if (existing.getMd5().equals(thisProtein.getMd5())) {
                    thisProtein = existing;
                    break;
                }
            }
        }

        // Add the Xref to the Protein object. (Being added to a Set, so no risk of duplicates)
        thisProtein.addCrossReference(XrefParser.getProteinXref(currentId));

    }


    private void createAndPersistNewORFs(final ProteinDAO.PersistedProteins persistedProteins) {
        //Holder for new ORFs which should be persisted

        Utilities.verboseLog("Start createAndPersistNewORFs for new proteins and their cross references.");

        Set<OpenReadingFrame> orfsAwaitingPersistence = new HashSet<>();

        //--- new implementation


        Long startCreateAndPersistNewORFs = System.currentTimeMillis();
        Map<String, Set<ProteinXref>> proteinXrefMap = getProteinXrefs(persistedProteins);
        int proteinXrefMapKeySize = proteinXrefMap.size();
        Long startucleotideSequenceCount = System.currentTimeMillis();
        Utilities.verboseLog("NucleotideSequences represented -  " + proteinXrefMap.size() + ": processed in "
                + (startucleotideSequenceCount - startCreateAndPersistNewORFs) + " millis ");
        Long totalNucleotideSequences = nucleotideSequenceDAO.count();
        Utilities.verboseLog("Actual NucleotideSequences  -  " + totalNucleotideSequences + ": processed in "
                + (System.currentTimeMillis() - startucleotideSequenceCount) + " millis ");

        Long startRetrieveAllNucleotideSequence = System.currentTimeMillis();
        List<NucleotideSequence> nucleotideSequenceList =  nucleotideSequenceDAO.retrieveAll();
        Long endRetrieveAllNucleotideSequence = System.currentTimeMillis();
        Utilities.verboseLog("Retrieved all nuclotieds  -  " + nucleotideSequenceList.size()
                + " nucleotides  in "
                + (endRetrieveAllNucleotideSequence - startRetrieveAllNucleotideSequence) + " millis ");

        int totalProteinXrefs = 0;
        int totalProteins = 0;
//        for (Long nucleotideSeqIndex = 1l; nucleotideSeqIndex <= totalNucleotideSequences; nucleotideSeqIndex++) {
        for ( NucleotideSequence nucleotideSequence : nucleotideSequenceList) {
            //Long startRetrieveNucleotideSequence = System.currentTimeMillis();
//            toDebugPrint(newProteins.size(), proteinCount,
//                    "newOrf: " + (startRetrieveByXrefIdentifier - startNewOrf) + " millis ");
            //NucleotideSequence nucleotideSequence = nucleotideSequenceDAO.getNucleotideSequence(nucleotideSeqIndex);
            Long startRetrieveByXrefIdentifier = System.currentTimeMillis();
//            Utilities.verboseLog("get  nucleotideSequence  in  -  "
//                    + (startRetrieveByXrefIdentifier - startRetrieveNucleotideSequence) + " millis ");
            //nucleotideSequenceDAO.retrieveByXrefIdentifier("id");
            Set<NucleotideSequenceXref> nucleotideSequenceCrossReferences = nucleotideSequence.getCrossReferences();
            Utilities.verboseLog(50, "get  NucleotideSequenceXref set  -  " + nucleotideSequenceCrossReferences.size() + "  in "
                    + (System.currentTimeMillis() - startRetrieveByXrefIdentifier) + " millis ");
            for (NucleotideSequenceXref nucleotideXref : nucleotideSequenceCrossReferences) {
                String nucleotideXrefIdentifier = nucleotideXref.getIdentifier();
                Set<ProteinXref> proteinXrefSet = (HashSet) proteinXrefMap.get(nucleotideXrefIdentifier);
                if (proteinXrefSet != null){
                  Utilities.verboseLog(50, "got proteinXrefSet set  -  " + proteinXrefSet.size() + "  in ");
		} else {
                  LOGGER.warn("CHECK nucleotideXrefIdentifier: " + nucleotideXrefIdentifier
                            + " nucleotideSequence: " + nucleotideSequence.getId());
                  continue;
		}
                for (ProteinXref proteinXref : proteinXrefSet) {
                    totalProteinXrefs ++;
                    String description = proteinXref.getDescription();
                    OpenReadingFrame newOrf = orfDescriptionLineParser.createORFFromParsingResult(description);
                    Long startSetNucleotideSequence = System.currentTimeMillis();

                    toDebugPrint(proteinXrefMapKeySize, totalProteinXrefs,
                            "RetrieveByXrefIdentifier: " + (startSetNucleotideSequence - startRetrieveByXrefIdentifier) + " millis ");
                    newOrf.setNucleotideSequence(nucleotideSequence);

                    Long startSetProtein = System.currentTimeMillis();
                    toDebugPrint(proteinXrefMapKeySize, totalProteinXrefs,
                            "SetNucleotideSequence: " + (startSetProtein - startSetNucleotideSequence) + " millis ");
                    Protein newProtein = proteinXref.getProtein();
                    newOrf.setProtein(newProtein);

                    Long startAddOpenReadingFrame = System.currentTimeMillis();
                    toDebugPrint(proteinXrefMapKeySize, totalProteinXrefs,
                            "SetProtein in ORF: " + (startAddOpenReadingFrame - startSetProtein) + " millis ");
                    newProtein.addOpenReadingFrame(newOrf);

                    Long startOrfAwaitingPersistence = System.currentTimeMillis();
                    toDebugPrint(proteinXrefMapKeySize, totalProteinXrefs,
                            "Add Orf to protein: " + (startOrfAwaitingPersistence - startAddOpenReadingFrame) + " millis ");
                    orfsAwaitingPersistence.add(newOrf);

                    Long endOrfAwaitingPersistence = System.currentTimeMillis();
                    toDebugPrint(proteinXrefMapKeySize, totalProteinXrefs,
                            "Add newOrf to ORFs AwaitingPersistence: " + (endOrfAwaitingPersistence - startOrfAwaitingPersistence) + " millis ");
                }
            }
        }

        Utilities.verboseLog("Completed processing " + totalNucleotideSequences + " NucleotideSequences " +
                "  with totalXrefs " + totalProteinXrefs
                + " in " +
                (System.currentTimeMillis() - startCreateAndPersistNewORFs) + " millis ");


        Utilities.verboseLog("createAndPersistNewORFs done in " +
                (System.currentTimeMillis() - startCreateAndPersistNewORFs) + " millis");
    }


    /**
     * 
     * @param persistedProteins
     * @return
     */
    public Map<String, Set<ProteinXref>> getProteinXrefs(final ProteinDAO.PersistedProteins persistedProteins) {

        Map<String, Set<ProteinXref>> proteinXrefs = new HashMap<>();

        Set<Protein> newProteins = persistedProteins.getNewProteins();
        int proteinCount = 0;
        int totalXrefs = 0;
        for (Protein newProtein : newProteins) {
            proteinCount++;
            Long startPersistProtein = System.currentTimeMillis();
            Set<ProteinXref> xrefs = newProtein.getCrossReferences();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Protein with ID " + newProtein.getId() + " has " + xrefs.size() + " cross references.");
            }
            int xrefCount = xrefs.size();
            totalXrefs = totalXrefs + xrefCount;

            toDebugPrint(newProteins.size(), proteinCount,
                    "getCrossReferences: " + (System.currentTimeMillis() - startPersistProtein) + " millis ");
            for (ProteinXref proteinXref : xrefs) {
                String originalHeader = proteinXref.getName();

                //Get rid of the underscore
                //String nucleotideId = XrefParser.stripOfFinalUnderScore(nucleotideId);
                String nucleotideId = XrefParser.getNucleotideIdFromORFId(originalHeader);
                Set<ProteinXref> proteinXrefSet = new HashSet<>();
                if(proteinXrefs.containsKey(nucleotideId)){
                    proteinXrefSet = proteinXrefs.get(nucleotideId);
                    proteinXrefSet.add(proteinXref);
                } else {
                    proteinXrefSet.add(proteinXref);
                }
                proteinXrefs.put(nucleotideId, proteinXrefSet);
            }
        }
        int avgXrefPerProtein = totalXrefs / proteinCount;

        Utilities.verboseLog("Total proteins: " + proteinCount + " total xrefs = " +  totalXrefs + " avrg xref per protein: " + avgXrefPerProtein);
        return proteinXrefs;
    }



    void toDebugPrint(int size, int count, String debugString){
        if(count < 0){
            return;
        }
        int halfSize = size / 2;
        boolean debugPrint = false;
        if(count == 1){
            debugPrint =  true;
        }else if (count == halfSize){
            debugPrint =  true;
        }else if (count == size - 1){
            debugPrint =  true;
        }

        if (debugPrint) {
            Utilities.verboseLog(25,"count:" + count + " - " + debugString);
        }
    }



}
//todo edit
