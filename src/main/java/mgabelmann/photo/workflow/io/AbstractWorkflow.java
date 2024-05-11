package mgabelmann.photo.workflow.io;

import java.io.File;
import java.io.IOException;

import mgabelmann.photo.workflow.HashType;
import mgabelmann.photo.workflow.exception.WorkflowException;

/**
 * 
 * @author Mike Gabelmann
 */
public abstract class AbstractWorkflow {
    public static final HashType DEFAULT_HASHTYPE = HashType.SHA256;

    public static final boolean DEFAULT_VERIFY = false;

    /** local directory. copy from. */
    protected transient final File dirLocal;
    
    /** remote directory. copy to. */
    protected transient final File dirRemote;
    
    /** checksum type. */
    protected transient final HashType type;
    
    /** Verify files. */
    protected transient final boolean verify;


    /**
     * Constructor, all args.
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

        if (dirLocal == null) {
            throw new IllegalArgumentException("dirLocal cannot be null");

        } else if (!dirLocal.exists()) {
            throw new IllegalArgumentException("dirLocal does not exist");

        } else if (!dirLocal.canRead()) {
            throw new IllegalArgumentException("dirLocal is not readable");
        }

        if (dirRemote == null) {
            throw new IllegalArgumentException("dirRemote cannot be null");

        } else if (!dirRemote.exists()) {
            throw new IllegalArgumentException("dirRemote does not exist");

        } else if (!dirRemote.canWrite()) {
            throw new IllegalArgumentException("dirRemote is not writable");
        }

        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }

        this.dirLocal = dirLocal;
        this.dirRemote = dirRemote;
        this.type = type;
        this.verify = verify;
    }

    public final File getDirLocal() {
        return dirLocal;
    }

    public final File getDirRemote() {
        return dirRemote;
    }

    public final HashType getType() {
        return type;
    }

    public final boolean isVerify() {
        return verify;
    }

    /**
     * Process workflow.
     * @throws Exception error processing
     */
    public abstract void process() throws WorkflowException;
    
    /**
     * Restore workflow.
     * @throws Exception error restoring
     */
    public abstract void restore() throws WorkflowException;
    
    /**
     * Verify workflow.
     * @throws Exception error verifying
     */
    public abstract void validate() throws WorkflowException;

}
