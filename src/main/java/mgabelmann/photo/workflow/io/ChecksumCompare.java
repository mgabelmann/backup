package mgabelmann.photo.workflow.io;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import mgabelmann.photo.workflow.HashType;
import mgabelmann.util.FileUtil;

/**
 * 
 * @author Mike Gabelmann
 */
public final class ChecksumCompare implements Runnable {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(ChecksumCompare.class);
    
    private final transient File source;
    private final transient File dest;
    private final transient HashType type;
    
    /** Are checksums equal? */
    private transient boolean equal = false;
    
    /**
     * Constructor.
     * @param source
     * @param dest
     * @param type
     */
    public ChecksumCompare(
        final File source, 
        final File dest, 
        final HashType type) {
        
        this.source = source;
        this.dest = dest;
        this.type = type;
    }
    
    
    
    public void run() {
        try {
            equal = FileUtil.verifyCopy(source, dest, type);
            LOG.info("FILE: " + source.getAbsolutePath() + " checksum - equal");
            
        } catch (IOException e) {
            LOG.warn("FILE: " + source.getAbsolutePath() + " checksum - failed");
        }
    }

}
