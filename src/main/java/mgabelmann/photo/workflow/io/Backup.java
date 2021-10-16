package mgabelmann.photo.workflow.io;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import mgabelmann.photo.workflow.HashType;
import mgabelmann.photo.workflow.exception.WorkflowRuntimeException;
import mgabelmann.util.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


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
    private static final Logger LOG = LogManager.getLogger(Backup.class);
    
    /** 
     * Service that ensures that the file checksums are threaded for optimum performance.
     */
    private final ExecutorService service;
    
    /**
     * Main method.
     * @param args arguments
     */
    public static void main(final String[] args) {
        
        final Backup backup = new Backup(
        	new File("F:/Mike/catalog1/03_raw/01_working/2021"),
        	new File("Z:/catalog1/03_raw/01_working/2021"),
            HashType.SHA256,
            false);
        
        try {
            backup.process();
            
        } catch (IOException ioe) {
            LOG.fatal(ioe);
            
        } catch (InterruptedException ie) {
        	LOG.error(ie);
        	Thread.currentThread().interrupt();
        }
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
        
        service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 5);
    }

    @Override
    public void process() throws IOException, InterruptedException {
        //simple sanity check
        sanityCheck();
        
        if (LOG.isInfoEnabled()) {
            LOG.info("backup - starting");
        }
        
        //determine amount of work
//        LOG.info("Determining workload");
//        DirectoryInfo info = DirectoryInfoCodec.calculateInfo(dirLocal);
//        LOG.info(info.toString());
        
        backupDirectory(dirLocal, dirRemote);
        
        service.shutdown();
        
        try {
            service.awaitTermination(15, TimeUnit.SECONDS);
            
        } catch (InterruptedException ie) {
            LOG.warn(ie.getMessage());
            throw ie;
        }
        
        if (LOG.isInfoEnabled()) { 
            LOG.info("backup - finished\n"); 
        }
    }

    @Override
    public void restore() throws IOException {
        throw new WorkflowRuntimeException("not implemented yet");
    }

    @Override
    public void validate() throws IOException {
        throw new WorkflowRuntimeException("not implemented yet");
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
        
        if (files.length > 0) {
            File dirR;
            File fileR;
            
            for (File f : files) {
                if (f.isDirectory()) {
                    dirR = new File(remoteDir, f.getName());
                    
                    if (! dirR.exists()) {
                        if(! dirR.mkdir()) {
                            throw new IOException("unable to create directory " + dirR.getAbsolutePath());
                        } else {
                            LOG.info("DIR: " + dirR.getAbsolutePath() + " does not exist - created");
                        }
                    }
                    
                    backupDirectory(f, dirR);
                    
                } else {
                    fileR = new File(remoteDir, f.getName());
                    backupFile(f, fileR);
                }
            }
            
        } else {
            LOG.debug("DIR: " + localDir.getAbsolutePath() + " skipping - empty");
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
        
        if (remoteFile.exists()) {
            if (localFile.length() != remoteFile.length()) {
                //different file lengths
                LOG.info("FILE: " + localFile.getAbsolutePath() + " replacing - different length");
                
                FileUtil.copyFile(localFile, remoteFile, true);
                
            } else if (localFile.lastModified() != remoteFile.lastModified()) {
                //same file lengths and different timestamps
                LOG.info("FILE: " + localFile.getAbsolutePath() + " replacing - different timestamp");
                
                FileUtil.copyFile(localFile, remoteFile, true);
                
            } else {
                //same file length and timestamps
                LOG.debug("FILE: " + localFile.getAbsolutePath() + " skipping - identical");
                copied = false;
            }
            
        } else {
            //copy file
            LOG.info("FILE: " + localFile.getAbsolutePath() + " copying - new");
            FileUtil.copyFile(localFile, remoteFile, true);
        } 
        
        //if copied, verify it
        if (copied && verify) {
            service.execute(new ChecksumCompare(localFile, remoteFile, type));
        }
    }
     
}
