package uk.ac.ebi.interpro.scan.io;
/*
       "label": "BINDING",
                    "desc": "ATP; via amide nitrogen.",
                    "end": 46,
                    "hmmStart": 51,
                    "start": 46,
                    "condition": "[GAST]",
                    "group": "3",
                    "hmmEnd": 51
 */
public class RuleSite {
    private String label;
    private String desc;
    private int start;
    private int end;
    private int hmmStart;
    private int hmmEnd;
    private String group;

    private String condition;

    public RuleSite(String label, String desc, int start, int end, int hmmStart, int hmmEnd, String group, String condition) {
        this.label = label;
        this.desc = desc;
        this.start = start;
        this.end = end;
        this.hmmStart = hmmStart;
        this.hmmEnd = hmmEnd;
        this.group = group;
        this.condition = condition;
    }

    public RuleSite() {
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getHmmStart() {
        return hmmStart;
    }

    public void setHmmStart(int hmmStart) {
        this.hmmStart = hmmStart;
    }

    public int getHmmEnd() {
        return hmmEnd;
    }

    public void setHmmEnd(int hmmEnd) {
        this.hmmEnd = hmmEnd;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return "RuleSite{" +
                "label='" + label + '\'' +
                ", desc='" + desc + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", hmmStart=" + hmmStart +
                ", hmmEnd=" + hmmEnd +
                ", group='" + group + '\'' +
                ", condition='" + condition + '\'' +
                '}';
    }
}
