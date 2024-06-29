package mgabelmann.photo.workflow.io;

import java.io.File;
import java.io.IOException;

import mgabelmann.photo.workflow.HashType;
import mgabelmann.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Mike Gabelmann
 */
public final class ChecksumCompare implements Runnable {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ChecksumCompare.class);
    
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
            LOGGER.info("FILE: {} checksum - equal", source.getAbsolutePath() );
            
        } catch (IOException e) {
            LOGGER.warn("FILE: {} checksum - failed", source.getAbsolutePath());
        }
    }

}
