package mgabelmann.photo.workflow.io;

import java.io.File;
import java.io.IOException;

import mgabelmann.photo.workflow.HashType;

/**
 * 
 * @author Mike Gabelmann
 */
public abstract class AbstractWorkflow {
    
    /** local directory. copy from. */
    protected transient final File dirLocal;
    
    /** remote directory. copy to. */
    protected transient final File dirRemote;
    
    /** checksum type. */
    protected transient final HashType type;
    
    /** Verify files. */
    protected transient final boolean verify;
    
    /**
     * Constructor.
     * @param dirLocal local directory
     * @param dirRemote remote directory
     * @param type checksum type
     * @param verify verify checksums
     */
    public AbstractWorkflow(
        final File dirLocal, 
        final File dirRemote, 
        final HashType type, 
        final boolean verify) {
        
        this.dirLocal = dirLocal;
        this.dirRemote = dirRemote;
        this.type = type;
        this.verify = verify;
    }
    
    /**
     * Process workflow.
     * @throws Exception error processing
     */
    public abstract void process() throws Exception;
    
    /**
     * Restore workflow.
     * @throws Exception error restoring
     */
    public abstract void restore() throws Exception;
    
    /**
     * Verify workflow.
     * @throws Exception error verifying
     */
    public abstract void validate() throws Exception;
    
    /**
     * Ensure we can process the work to do
     * @throws IOException error
     */
    protected void sanityCheck() throws IOException {
       if (! dirLocal.exists()) {
           throw new IOException(dirLocal.getAbsolutePath() + " does not exist");
       }
       
       if (! dirRemote.exists()) {
           throw new IOException(dirRemote.getAbsolutePath() + " does not exist");
       }
    }

}
