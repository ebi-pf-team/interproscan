package uk.ac.ebi.interpro.scan.io;
/*
       "PIRSR016496-1": {
            "hmmFrom": 3,
            "hmmTo": 250,
            "hmmAlign": "ivlKlGGSvitdKskpetairedalkriaeelaellekeeklvlvH.GgGSFGHpaakkyglsegiseeveGfaevheamseLnsivveaLleagv",
            "seqFrom": 6,
            "seqTo": 248,
            "seqAlign": "IVFKVGTSSLTNEDGSLS---RSKVKDITQQLAMLHEAGHELILVSsGAIAAGFGALG-FKKRPTKIADKQASAAVGQG-LLLEEYTTNLLLRQIV",
            "RuleSites": [
                {
                    "label": "BINDING",
                    "desc": "ATP; via amide nitrogen.",
                    "end": 46,
                    "hmmStart": 51,
                    "start": 46,
                    "condition": "[GAST]",
                    "group": "3",
                    "hmmEnd": 51
                }
            ],
            "Scope": [
                "Archaea",
                "Eukaryota",
                "Bacteria"
            ]
        }

 */

import java.util.ArrayList;
import java.util.List;

public class SimpleDomainMatch {

    private double domScore;

    private double domEvalue;

    private  int hmmFrom;

    private  int hmmTo;

    private String hmmAlign;

    private  int seqFrom;

    private  int seqTo;

    private String seqAlign;

    List<RuleSite> ruleSites = new ArrayList<>();

    List<String> scope = new ArrayList<>();

    public SimpleDomainMatch(double domScore, double domEvalue, int hmmFrom, int hmmTo, String hmmAlign, int seqFrom, int seqTo, String seqAlign, List<RuleSite> ruleSites, List<String> scope) {
        this.domScore = domScore;
        this.domEvalue = domEvalue;
        this.hmmFrom = hmmFrom;
        this.hmmTo = hmmTo;
        this.hmmAlign = hmmAlign;
        this.seqFrom = seqFrom;
        this.seqTo = seqTo;
        this.seqAlign = seqAlign;
        this.ruleSites = ruleSites;
        this.scope = scope;
    }


    public SimpleDomainMatch() {
    }

    public double getDomScore() {
        return domScore;
    }

    public void setDomScore(double domScore) {
        this.domScore = domScore;
    }

    public double getDomEvalue() {
        return domEvalue;
    }

    public void setDomEvalue(double domEvalue) {
        this.domEvalue = domEvalue;
    }

    public int getHmmFrom() {
        return hmmFrom;
    }

    public void setHmmFrom(int hmmFrom) {
        this.hmmFrom = hmmFrom;
    }

    public int getHmmTo() {
        return hmmTo;
    }

    public void setHmmTo(int hmmTo) {
        this.hmmTo = hmmTo;
    }

    public String getHmmAlign() {
        return hmmAlign;
    }

    public void setHmmAlign(String hmmAlign) {
        this.hmmAlign = hmmAlign;
    }

    public int getSeqFrom() {
        return seqFrom;
    }

    public void setSeqFrom(int seqFrom) {
        this.seqFrom = seqFrom;
    }

    public int getSeqTo() {
        return seqTo;
    }

    public void setSeqTo(int seqTo) {
        this.seqTo = seqTo;
    }

    public String getSeqAlign() {
        return seqAlign;
    }

    public void setSeqAlign(String seqAlign) {
        this.seqAlign = seqAlign;
    }

    public List<RuleSite> getRuleSites() {
        return ruleSites;
    }

    public void setRuleSites(List<RuleSite> ruleSites) {
        this.ruleSites = ruleSites;
    }

    public List<String> getScope() {
        return scope;
    }

    public void setScope(List<String> scope) {
        this.scope = scope;
    }

    @Override
    public String toString() {
        return "SimpleDomainMatch{" +
                "domScore=" + domScore +
                ", domEvalue=" + domEvalue +
                ", hmmFrom=" + hmmFrom +
                ", hmmTo=" + hmmTo +
                ", hmmAlign='" + hmmAlign + '\'' +
                ", seqFrom=" + seqFrom +
                ", seqTo=" + seqTo +
                ", seqAlign='" + seqAlign + '\'' +
                ", ruleSites=" + ruleSites +
                ", scope=" + scope +
                '}';
    }
}
