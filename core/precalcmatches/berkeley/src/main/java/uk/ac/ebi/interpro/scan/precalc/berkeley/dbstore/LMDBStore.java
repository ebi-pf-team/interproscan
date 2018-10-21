package uk.ac.ebi.interpro.scan.precalc.berkeley.dbstore;

import java.nio.ByteBuffer;

import static org.lmdbjava.DbiFlags.MDB_CREATE;
import static org.lmdbjava.DbiFlags.MDB_DUPSORT;
import static org.lmdbjava.DirectBufferProxy.PROXY_DB;

import org.lmdbjava.*;
import static org.lmdbjava.Env.create;
import static org.lmdbjava.Env.open;

import java.io.File;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * level DB store
 */
public class LMDBStore {

    String dbName = "IPRSCAN";

    public LMDBStore() {

    }

    public Dbi<ByteBuffer>  getLMDBStore(String dbStorePath) {
        //
        final File lookupMatchDBDirectory = new File(dbStorePath);
        
	final Env<ByteBuffer> env = create()
        .setMapSize(10 * 1024 * 1024)
        .setMaxDbs(1)
        .open(lookupMatchDBDirectory);

        final Dbi<ByteBuffer> db = env.openDbi(dbName, MDB_CREATE);

	return db;
	//
	
	//return null;

    }

}

