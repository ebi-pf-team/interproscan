package uk.ac.ebi.interpro.scan.condensed.berkeley;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Phil Jones
 *         <p/>
 *         This class makes use of java.util.zip.Deflator
 *         and java.util.zip.Inflator to compress Strings down
 *         to a compact byte[] for storage / retrieval to and from
 *         the BerkeleyDB.
 */
@Service
public class StringSqueezer {


    public byte[] compress(final String toCompress) throws IOException {
        ByteArrayOutputStream baos = null;
        try {
            final byte[] input = toCompress.getBytes("UTF-8");
            final Deflater df = new Deflater();
            df.setInput(input);
            baos = new ByteArrayOutputStream(input.length);
            df.finish();
            final byte[] buff = new byte[1024];
            while (!df.finished()) {
                int count = df.deflate(buff);
                baos.write(buff, 0, count);
            }
            baos.flush();
            return baos.toByteArray();
        } finally {
            if (baos != null) {
                baos.close();
            }
        }
    }

    public String inflate(final byte[] toExpand) throws IOException, DataFormatException {
        final Inflater ifl = new Inflater();
        ifl.setInput(toExpand);

        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream(toExpand.length);
            final byte[] buff = new byte[1024];
            while (!ifl.finished()) {
                int count = ifl.inflate(buff);
                baos.write(buff, 0, count);
            }
            baos.flush();
            return new String(baos.toByteArray());
        } finally {
            if (baos != null) {
                baos.close();
            }
        }
    }
}
