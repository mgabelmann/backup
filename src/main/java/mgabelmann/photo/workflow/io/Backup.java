package mgabelmann.photo.workflow.io;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import mgabelmann.photo.workflow.HashType;
import mgabelmann.photo.workflow.exception.WorkflowException;
import mgabelmann.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simply copies the local directory structure to a remote directory structure without modifying it. A manifest
 * file is created with information about each file. A checksum is also provided for verifying the backup in
 * the future.
 * 
 * Recovering a backup is easy to do as long as the local folder structure still exists.
 * 
 * @author Mike Gabelmann
 */
public final class Backup extends AbstractWorkflow {
    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(Backup.class);
    
    /** Service that ensures that the file checksums are threaded for optimum performance. */
    private final ExecutorService service;


    /**
     * Main method.
     * @param args arguments
     */
    public static void main(final String[] args) {
        Backup backup = new Backup(
        	new File("P:/Mike/catalog1/03_raw/01_working/2024"),
        	new File("Z:/catalog1/03_raw/01_working/2024")
        );
        
        try {
            backup.process();
            
        } catch (WorkflowException we) {
            LOG.error(we.getMessage());
        }
    }

    /**
     * Constructor.
     * @param workDir dirLocal local directory (original files)
     * @param dirRemote dirRemote remote directory (backup files)
     */
    public Backup(final File workDir, final File dirRemote) {
        this(workDir, dirRemote, DEFAULT_HASHTYPE, DEFAULT_VERIFY);
    }

    /**
     * Constructor.
     * @param dirLocal local directory (original files)
     * @param dirRemote remote directory (backup files)
     * @param type checksum type
     * @param verify verify copy
     */
    public Backup(final File dirLocal, final File dirRemote, final HashType type, final boolean verify) {
        super(dirLocal, dirRemote, type, verify);
        
        this.service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 5);
    }

    @Override
    public void process() throws WorkflowException {
        if (LOG.isInfoEnabled()) {
            LOG.info("backup - starting");
        }

        try {
            this.backupDirectory(dirLocal, dirRemote);

            service.shutdown();

            boolean timeout = service.awaitTermination(15, TimeUnit.SECONDS);

            if (timeout) {
                LOG.warn("service timed out");
            }

        } catch (IOException | InterruptedException e) {
            throw new WorkflowException(e);
        }
        
        if (LOG.isInfoEnabled()) { 
            LOG.info("backup - finished\n"); 
        }
    }

    @Override
    public void restore() throws WorkflowException {
        throw new WorkflowException("not implemented yet");
    }

    @Override
    public void validate() throws WorkflowException {
        throw new WorkflowException("not implemented yet");
    }
    
    /**
     * Iterate over the directory contents and backup files/directories as needed.
     *
     * @param localDir local directory
     * @param remoteDir remote directory
     * @throws IOException error backing up
     */
    private void backupDirectory(
        final File localDir,
        final File remoteDir) 
        throws IOException {
        
        final File[] files = localDir.listFiles();
        
        if (files != null && files.length > 0) {
            File dirR;
            File fileR;
            
            for (File f : files) {
                if (f.isDirectory()) {
                    dirR = new File(remoteDir, f.getName());
                    
                    if (! dirR.exists()) {
                        if(! dirR.mkdir()) {
                            throw new IOException("unable to create directory " + dirR.getAbsolutePath());

                        } else {
                            LOG.info("DIR: {} does not exist - created", dirR.getAbsolutePath());
                        }
                    }
                    
                    this.backupDirectory(f, dirR);
                    
                } else {
                    fileR = new File(remoteDir, f.getName());

                    this.backupFile(f, fileR);
                }
            }
            
        } else {
            LOG.debug("DIR: {} skipping - empty", localDir.getAbsolutePath() );
        }
    }
    
    /**
     * Backup a file if it is new or has changed.
     * @param localFile local file
     * @param remoteFile remote file
     * @throws IOException error backing up file
     */
    private void backupFile(final File localFile, final File remoteFile) throws IOException {
        boolean copied = true;

        //FIXME: switch to do this instead
        //BasicFileAttributes attributesLocal = Files.readAttributes(localFile.toPath(), BasicFileAttributes.class);
        //BasicFileAttributes attributesRemote = Files.readAttributes(remoteFile.toPath(), BasicFileAttributes.class);


        if (remoteFile.exists()) {
            /*if (attributesLocal.size() != attributesRemote.size()) {


            } else if (attributesLocal.lastModifiedTime() != attributesRemote.lastModifiedTime()) {


            } else {

            }*/

            if (localFile.length() != remoteFile.length()) {
                //different file lengths
                LOG.info("FILE: {} replacing - different length", localFile.getAbsolutePath());
                
                FileUtil.copyFile(localFile, remoteFile, true);
                
            } else if (localFile.lastModified() != remoteFile.lastModified()) {
                //same file lengths and different timestamps
                LOG.info("FILE: {} replacing - different timestamp", localFile.getAbsolutePath());
                
                FileUtil.copyFile(localFile, remoteFile, true);
                
            } else {
                //same file length and timestamps
                LOG.debug("FILE: {} skipping - identical", localFile.getAbsolutePath());
                copied = false;
            }
            
        } else {
            //copy file
            LOG.info("FILE: {} copying - new", localFile.getAbsolutePath());
            FileUtil.copyFile(localFile, remoteFile, true);
        } 
        
        //if copied, verify it
        if (copied && verify) {
            service.execute(new ChecksumCompare(localFile, remoteFile, type));
        }
    }
     
}
