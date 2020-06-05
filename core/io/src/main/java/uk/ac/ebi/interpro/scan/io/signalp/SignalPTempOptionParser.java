package uk.ac.ebi.interpro.scan.io.signalp;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Parses the output of the SignalP Perl script help to check if the -T option is available:
 * <p/>
 * The manual instruction looks like that:
 * <p/>
 * Description: Predict signal peptide and cleavage site.
 * <p/>
 * Usage: signalp -f <format>  -p <graphics-type> -k -s <networks> -t <organism-type> -m <fasta-file> -n gff-file -v -l <logfile> -u <value>  -U <value> -w -h -c <value> <fasta-file(s)>
 * Options:
 * -f   Setting the output format ('short', 'long', 'summary' or 'all'). Default: 'short'
 * -g   Graphics 'gif' or 'gif+eps'. Default: 'Off'
 * -k   Keep temporary directory. Default: 'Off'
 * -s   Signal peptide networks to use ('best' or 'notm'). Default: 'best'
 * -t   Organism type> (euk, gram+, gram-). Default: 'euk'
 * -m   Make fasta file with mature sequence. Default: 'Off'
 * -n   Make gff file of processed sequences. Default: 'Off'
 * -T   Specify temporary file directory. Default: /tmp
 * -u   user defined D-cutoff for noTM networks
 * -U   user defined D-cutoff for TM networks
 * -c   truncate to sequence length - 0 means no truncation. Default '70'
 * -l   Logfile if -v is defined. Default: 'STDERR'
 * -v   Verbose. Default: 'Off'
 * -h   Print this help information
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class SignalPTempOptionParser {

    private static final Logger LOGGER = LogManager.getLogger(SignalPTempOptionParser.class.getName());

    private String tempOptionLine;

    @Required
    public void setTempOptionLine(String tempOptionLine) {
        this.tempOptionLine = tempOptionLine;
    }

    /**
     * Parses all lines of the input file. Return TRUE if -T option is available, otherwise FALSE.
     *
     * @param is File input stream.
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public boolean parse(InputStream is) throws IOException, ParseException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                //Checks if -T option is available or not
                if (line.startsWith(tempOptionLine)) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("-T option is available in this SignalP Perl script version.");
                    }
                    return true;
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return false;
    }
}