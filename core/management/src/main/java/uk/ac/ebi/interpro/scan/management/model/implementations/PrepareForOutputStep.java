package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.persistence.MatchDAO;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.SignatureLibraryLookup;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.*;

public class PrepareForOutputStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(PrepareForOutputStep.class.getName());

    //DAOs
    private ProteinDAO proteinDAO;
    private MatchDAO matchDAO;

    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    public void setMatchDAO(MatchDAO matchDAO) {
        this.matchDAO = matchDAO;
    }

    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        String proteinRange = "[" + stepInstance.getBottomProtein() + "-" + stepInstance.getTopProtein() + "]";
        Utilities.verboseLog("starting PrepareForOutputStep :" + proteinRange);
        Long bottomProteinId = stepInstance.getBottomProtein();
        Long topProteinId = stepInstance.getTopProtein();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Starting step with Id " + this.getId());
        }

        int proteinCount = 0;
        int matchCount = 0;

        Set<String> signatureLibraryNames = new HashSet<>();

        for (SignatureLibrary sig: SignatureLibrary.values() ){
            signatureLibraryNames.add(sig.getName());
        }

       // Map<String, Protein> keyToProteinMap = proteinDAO.getKeyToProteinMap();
        //Iterator it = keyToProteinMap.keySet().iterator();

//        List<Protein> proteinsinRange = proteinDAO.getProteinsBetweenIds(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
//        List<Protein> proteinsinRange = getProteinsBetweenIds(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
        /*
        List<String> proteinsinRange = getProteinsBetweenIds(stepInstance.getBottomProtein(), stepInstance.getTopProtein());

        for (String proteinKey: proteinsinRange) {
            proteinCount ++;

            for(String signatureLibraryName: signatureLibraryNames){

                final String dbKey = proteinKey + signatureLibraryName;
                Set<Match> matches = matchDAO.getMatchSet(dbKey);
                if (matches != null){
                    //Utilities.verboseLog("Get matches for protein  id: " + protein.getId() +  " dbKey (matchKey): " + dbKey);
                    for(Match match: matches){
                        //protein.addMatch(match);
                        matchCount ++;
                    }
                }
            }
            //keyToProteinMap.put(key, protein);
        }
        */
        int proteinRawCount = 0;
        Protein exampleProtein = null;
        for (Long proteinIndex= bottomProteinId;proteinIndex <= topProteinId; proteinIndex ++){
            proteinRawCount ++;
            String proteinKey = Long.toString(proteinIndex);
            Protein protein = proteinDAO.getProtein(proteinKey);
            if(protein != null){
                proteinCount ++;
            }
            for(String signatureLibraryName: signatureLibraryNames){
                final String dbKey = proteinKey + signatureLibraryName;
                Set<Match> matches = matchDAO.getMatchSet(dbKey);
                if (matches != null){
                    //Utilities.verboseLog("Get matches for protein  id: " + protein.getId() +  " dbKey (matchKey): " + dbKey);
                    for(Match match: matches){
                        match.getSignature().getCrossReferences();
                        match.getSignature().getEntry();
                        protein.addMatch(match);
                        matchCount ++;
                    }
                }
            }
            if (exampleProtein == null && protein.getMatches().size() > 1) {
                exampleProtein = protein;
            }
            if (proteinIndex % 4000 == 0){
                Utilities.verboseLog(proteinRange + " - Of possible  " + proteinRawCount + " proteins, processed  " + proteinCount + " with  total matches : " + matchCount);
            }
            //store protein back in kv store
            proteinDAO.persist(proteinKey, protein);
            //keyToProteinMap.put(key, protein);
        }



       // Map<String, Set<Match>> matchesForEachProtein = matchDAO.getMatchesForEachProtein();

        //Utilities.verboseLog("matchesForEachProtein: " + matchesForEachProtein.size());

        //for (String matchKey : matchesForEachProtein.keySet()){
        //    Set<Match> matches = matchesForEachProtein.get(matchKey);
            //Utilities.verboseLog("matchKey: " + matchKey + " match count: " + matches.size());
        //}


        //Set<Match> allMatches = matchDAO.getMatches();
        //matchCount = allMatches.size();

        //        Match testMatch = (Match) matchDAO.get("test1");


        Utilities.verboseLog("Total proteins in range " + proteinRange + " with matches :  " + proteinCount + " Total matches : " + matchCount);
        //Protein exampleProtein = proteinsinRange.get(1);
        if(exampleProtein != null) {
            //dont prin the example protein for now
//            Utilities.verboseLog("exampleProtein: " + exampleProtein.toString());
            int matchsize = 0;
            if (! exampleProtein.getMatches().isEmpty()){
                matchsize =  exampleProtein.getMatches().size();
            }
            Utilities.verboseLog("exampleProtein: " + exampleProtein.getId() + " matches: " + matchsize);
        }
    }

    List<String>   getProteinsBetweenIds(Long bottom, Long top){
        Long timestart = System.currentTimeMillis();
        List<String> proteinsinRange = new ArrayList<>();
        for (Long index= bottom;index <= top; index ++){
            String proteinKey = Long.toString(index);
            Protein protein = proteinDAO.getProtein(proteinKey);
            if(protein != null){
                proteinsinRange.add(proteinKey);
            }
        }
        Long timeTaken = System.currentTimeMillis() - timestart;
        int timeTakenSeconds = timeTaken.intValue() / 1000;
        Utilities.verboseLog("timeTakenSeconds to get range: [" + bottom + "-" + top + "] = " + timeTakenSeconds + " seconds ");

        return proteinsinRange;
    }

}