package uk.ac.ebi.interpro.scan.model.helper;

import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.Signature;

/**
 * Object holding a signature and a model.
 *
 * @author Matthew Fraser
 * @version $Id$
 */
public class SignatureModelHolder {

    private Signature signature;

    private Model model;

    public SignatureModelHolder(Signature signature, Model model) {
        this.signature = signature;
        this.model = model;
    }

    public Signature getSignature() {
        return signature;
    }

    public void setSignature(Signature signature) {
        this.signature = signature;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SignatureModelHolder)) return false;

        SignatureModelHolder that = (SignatureModelHolder) o;

        if (signature != null ? !signature.equals(that.signature) : that.signature != null) return false;
        return model != null ? model.equals(that.model) : that.model == null;

    }

    @Override
    public int hashCode() {
        int result = signature != null ? signature.hashCode() : 0;
        result = 31 * result + (model != null ? model.hashCode() : 0);
        return result;
    }
}
