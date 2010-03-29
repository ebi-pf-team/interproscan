package uk.ac.ebi.interpro.scan.jms.installer;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.io.gene3d.Model2SfReader;
import uk.ac.ebi.interpro.scan.io.model.Hmmer3ModelLoader;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import javax.activation.DataHandler;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


/**
 * Install InterProScan
 */
public class Installer implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Installer.class.getName());


    private GenericDAO<SignatureLibraryRelease, Long> signatureLibraryReleaseDAO;

    private String pfamHMMfilePath;

    private Resource gene3dModel2SfFile;


    @Required
    public void setSignatureLibraryReleaseDAO(GenericDAO<SignatureLibraryRelease, Long> signatureLibraryReleaseDAO) {
        this.signatureLibraryReleaseDAO = signatureLibraryReleaseDAO;
    }
    @Required
    public void setPfamHMMfilePath(String pfamHMMfilePath) {
        this.pfamHMMfilePath = pfamHMMfilePath;
    }

    @Required
    public void setGene3dModel2SfFile(Resource gene3dModel2SfFile) {
        this.gene3dModel2SfFile = gene3dModel2SfFile;
    }



//    private LocalSessionFactoryBean sessionFactoryBean;


    @Override
    public void run() {
        LOGGER.info("Schema creation");

        LOGGER.info("Loading signatures");
        loadGene3dModels();
        loadPfamModels();
        LOGGER.info("Loaded signatures");
    }


//    public void setSessionFactoryBean(LocalSessionFactoryBean sessionFactoryBean) {
//        this.sessionFactoryBean = sessionFactoryBean;
//    }

    public void setEntityManagerFactory(javax.persistence.EntityManagerFactory entityManagerFactory) {

    }

    private void loadGene3dModels() {
        // Read models
        Model2SfReader reader = new Model2SfReader();
        Map<String, String> modelMap;
        try {
             modelMap = reader.read(gene3dModel2SfFile);
        } catch (IOException e) {
            LOGGER.fatal("IOException thrown when parsing HMM file.",e);
            throw new IllegalStateException("Unable to load Gene3d models",e);
        }
        // Create signatures
        final Map<String, Signature> signatureMap = new HashMap<String, Signature>();
        for (String modelAc : modelMap.keySet())  {
            String signatureAc = modelMap.get(modelAc);
            Signature signature;
            if (signatureMap.containsKey(signatureAc))  {
                signature = signatureMap.get(signatureAc);
            }
            else    {
                signature = new Signature(signatureAc);
                signatureMap.put(signatureAc, signature);
            }
            signature.addModel(new Model(modelAc));
        }
        // Create and persist release
        SignatureLibraryRelease release =
                new SignatureLibraryRelease(SignatureLibrary.GENE3D, "3.0.0",
                        new HashSet<Signature>(signatureMap.values()));
        signatureLibraryReleaseDAO.insert(release);
    }

    // TODO load pfam models in chunks, not all at one go... java.lang.OutOfMemory
    private void loadPfamModels() {
        // Parse and retrieve the signatures.
        Hmmer3ModelLoader modelLoader = new Hmmer3ModelLoader(SignatureLibrary.PFAM, "24.0");
        SignatureLibraryRelease release = null;
        try{
            release = modelLoader.parse(pfamHMMfilePath);
        } catch (IOException e) {
            LOGGER.fatal("IOException thrown when parsing HMM file.",e);
            throw new IllegalStateException("Unable to load Pfam models",e);
        }

        // And store the Models / Signatures to the database.
        LOGGER.debug("Storing SignatureLibraryRelease...");
        signatureLibraryReleaseDAO.insert(release);
        LOGGER.debug("Storing SignatureLibraryRelease...DONE");

    }

}
