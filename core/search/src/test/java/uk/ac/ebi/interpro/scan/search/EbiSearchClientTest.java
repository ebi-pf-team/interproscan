package uk.ac.ebi.interpro.scan.search;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Tests EBI Search web service client.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public class EbiSearchClientTest {

    @Test
    public void getServiceEndPoint() {

        // Default (oddly, this is null)
        EbiSearchClient client = new EbiSearchClient();
        assertEquals("Service end points should match", null, client.getServiceEndPoint());

        // Custom
        String endPoint = "http://www.ebi.ac.uk/ebisearch/service.ebi";
        client = new EbiSearchClient(endPoint);
        assertEquals("Service end points should match", endPoint, client.getServiceEndPoint());

    }

    @Test
    public void testIsSequence() {

        final String HEADER =
                ">sp|P38398|BRCA1_HUMAN Breast cancer type 1 susceptibility protein OS=Homo sapiens GN=BRCA1 PE=1" +
                "\nMDLSALRVEEVQNVINAMQKILECPICLELIKEPVSTKCDHIFCKFCMLKLLNQKKGPSQ";

        final String NO_HEADER =
                "MDLSALRVEEVQNVINAMQKILECPICLELIKEPVSTKCDHIFCKFCMLKLLNQKKGPSQ";

        final String CHARS_31        = "MDLSALRVEEVQNVINAMQKILECPICLELI";
        final String CHARS_31_SPACES = "MDLSALRVEEVQNV INAMQKILECPICLELI";
        final String CHARS_30        = "MDLSALRVEEVQNVINAMQKILECPICLEL";
        final String CHARS_29        = "MDLSALRVEEVQNVINAMQKILECPICLE";

        final String TEXT = "Protein phosphatase 2C, manganese/magnesium aspartate binding site";

        assertEquals("Header",    true,  EbiSearchClient.Query.isSequence(HEADER));
        assertEquals("No header", true,  EbiSearchClient.Query.isSequence(NO_HEADER));

        assertEquals("31 characters",             true,   EbiSearchClient.Query.isSequence(CHARS_31));
        assertEquals("31 characters with spaces", false,  EbiSearchClient.Query.isSequence(CHARS_31_SPACES));

        assertEquals("30 characters", true,   EbiSearchClient.Query.isSequence(CHARS_30));
        assertEquals("29 characters", false,  EbiSearchClient.Query.isSequence(CHARS_29));

        assertEquals("Text", false, EbiSearchClient.Query.isSequence(TEXT));

        // Example: http://www.uniprot.org/uniprot/P38398.fasta
        final String BRCA1 =
                ">sp|P38398|BRCA1_HUMAN Breast cancer type 1 susceptibility protein OS=Homo sapiens GN=BRCA1 PE=1 SV=2\n" +
                "MDLSALRVEEVQNVINAMQKILECPICLELIKEPVSTKCDHIFCKFCMLKLLNQKKGPSQ\n" +
                "CPLCKNDITKRSLQESTRFSQLVEELLKIICAFQLDTGLEYANSYNFAKKENNSPEHLKD\n" +
                "EVSIIQSMGYRNRAKRLLQSEPENPSLQETSLSVQLSNLGTVRTLRTKQRIQPQKTSVYI\n" +
                "ELGSDSSEDTVNKATYCSVGDQELLQITPQGTRDEISLDSAKKAACEFSETDVTNTEHHQ\n" +
                "PSNNDLNTTEKRAAERHPEKYQGSSVSNLHVEPCGTNTHASSLQHENSSLLLTKDRMNVE\n" +
                "KAEFCNKSKQPGLARSQHNRWAGSKETCNDRRTPSTEKKVDLNADPLCERKEWNKQKLPC\n" +
                "SENPRDTEDVPWITLNSSIQKVNEWFSRSDELLGSDDSHDGESESNAKVADVLDVLNEVD\n" +
                "EYSGSSEKIDLLASDPHEALICKSERVHSKSVESNIEDKIFGKTYRKKASLPNLSHVTEN\n" +
                "LIIGAFVTEPQIIQERPLTNKLKRKRRPTSGLHPEDFIKKADLAVQKTPEMINQGTNQTE\n" +
                "QNGQVMNITNSGHENKTKGDSIQNEKNPNPIESLEKESAFKTKAEPISSSISNMELELNI\n" +
                "HNSKAPKKNRLRRKSSTRHIHALELVVSRNLSPPNCTELQIDSCSSSEEIKKKKYNQMPV\n" +
                "RHSRNLQLMEGKEPATGAKKSNKPNEQTSKRHDSDTFPELKLTNAPGSFTKCSNTSELKE\n" +
                "FVNPSLPREEKEEKLETVKVSNNAEDPKDLMLSGERVLQTERSVESSSISLVPGTDYGTQ\n" +
                "ESISLLEVSTLGKAKTEPNKCVSQCAAFENPKGLIHGCSKDNRNDTEGFKYPLGHEVNHS\n" +
                "RETSIEMEESELDAQYLQNTFKVSKRQSFAPFSNPGNAEEECATFSAHSGSLKKQSPKVT\n" +
                "FECEQKEENQGKNESNIKPVQTVNITAGFPVVGQKDKPVDNAKCSIKGGSRFCLSSQFRG\n" +
                "NETGLITPNKHGLLQNPYRIPPLFPIKSFVKTKCKKNLLEENFEEHSMSPEREMGNENIP\n" +
                "STVSTISRNNIRENVFKEASSSNINEVGSSTNEVGSSINEIGSSDENIQAELGRNRGPKL\n" +
                "NAMLRLGVLQPEVYKQSLPGSNCKHPEIKKQEYEEVVQTVNTDFSPYLISDNLEQPMGSS\n" +
                "HASQVCSETPDDLLDDGEIKEDTSFAENDIKESSAVFSKSVQKGELSRSPSPFTHTHLAQ\n" +
                "GYRRGAKKLESSEENLSSEDEELPCFQHLLFGKVNNIPSQSTRHSTVATECLSKNTEENL\n" +
                "LSLKNSLNDCSNQVILAKASQEHHLSEETKCSASLFSSQCSELEDLTANTNTQDPFLIGS\n" +
                "SKQMRHQSESQGVGLSDKELVSDDEERGTGLEENNQEEQSMDSNLGEAASGCESETSVSE\n" +
                "DCSGLSSQSDILTTQQRDTMQHNLIKLQQEMAELEAVLEQHGSQPSNSYPSIISDSSALE\n" +
                "DLRNPEQSTSEKAVLTSQKSSEYPISQNPEGLSADKFEVSADSSTSKNKEPGVERSSPSK\n" +
                "CPSLDDRWYMHSCSGSLQNRNYPSQEELIKVVDVEEQQLEESGPHDLTETSYLPRQDLEG\n" +
                "TPYLESGISLFSDDPESDPSEDRAPESARVGNIPSSTSALKVPQLKVAESAQSPAAAHTT\n" +
                "DTAGYNAMEESVSREKPELTASTERVNKRMSMVVSGLTPEEFMLVYKFARKHHITLTNLI\n" +
                "TEETTHVVMKTDAEFVCERTLKYFLGIAGGKWVVSYFWVTQSIKERKMLNEHDFEVRGDV\n" +
                "VNGRNHQGPKRARESQDRKIFRGLEICCYGPFTNMPTDQLEWMVQLCGASVVKELSSFTL\n" +
                "GTGVHPIVVVQPDAWTEDNGFHAIGQMCEAPVVTREWVLDSVALYQCQELDTYLIPQIPH\n" +
                "SHY";

        assertEquals("BRCA1", true, EbiSearchClient.Query.isSequence(BRCA1));

    }

}
