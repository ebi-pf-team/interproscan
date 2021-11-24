package uk.ac.ebi.interpro.scan.management.model.implementations.funfam;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.gene3d.CathResolveHitsOutputParser;
import uk.ac.ebi.interpro.scan.io.gene3d.CathResolverRecord;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.Hmmer3DomTblParser;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.DomTblDomainMatch;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.DomainMatch;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.HmmSearchRecord;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.SequenceMatch;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.HmmBounds;
import uk.ac.ebi.interpro.scan.model.Hmmer3Match;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.*;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseAndPersisteStep extends Step  {
    private static final Logger LOGGER = LogManager.getLogger(ParseAndPersisteStep.class.getName());

    public String getCathResolveHitsOutputFileNameTemplate() {
        return cathResolveHitsOutputFileNameTemplate;
    }

    public void setCathResolveHitsOutputFileNameTemplate(String cathResolveHitsOutputFileNameTemplate) {
        this.cathResolveHitsOutputFileNameTemplate = cathResolveHitsOutputFileNameTemplate;
    }

    public String getHmmsearchDomTblOutputFileNameTemplate() {
        return hmmsearchDomTblOutputFileNameTemplate;
    }

    public void setHmmsearchDomTblOutputFileNameTemplate(String hmmsearchDomTblOutputFileNameTemplate) {
        this.hmmsearchDomTblOutputFileNameTemplate = hmmsearchDomTblOutputFileNameTemplate;
    }

    public Hmmer3DomTblParser getHmmer3DomTblParser() {
        return hmmer3DomTblParser;
    }

    public void setHmmer3DomTblParser(Hmmer3DomTblParser hmmer3DomTblParser) {
        this.hmmer3DomTblParser = hmmer3DomTblParser;
    }

    public CathResolveHitsOutputParser getCathResolveHitsOutputParser() {
        return cathResolveHitsOutputParser;
    }

    public void setCathResolveHitsOutputParser(CathResolveHitsOutputParser cathResolveHitsOutputParser) {
        this.cathResolveHitsOutputParser = cathResolveHitsOutputParser;
    }

    private String cathResolveHitsOutputFileNameTemplate;
    private String hmmsearchDomTblOutputFileNameTemplate;
    private Hmmer3DomTblParser hmmer3DomTblParser;
    private CathResolveHitsOutputParser cathResolveHitsOutputParser;

    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        if (checkIfDoSkipRun(stepInstance.getBottomProtein(), stepInstance.getTopProtein())) {
            String key = getKey(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
            Utilities.verboseLog(110, "doSkipRun - step: "  + this.getId() + " - " +  key);
            return;
        }

        InputStream domTblInputStream = null;
        InputStream cathResolverRecordInputStream = null;
        String cathResolveHitsOutputFileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getCathResolveHitsOutputFileNameTemplate());
        String domTblOutputFileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getHmmsearchDomTblOutputFileNameTemplate());
    }
}
