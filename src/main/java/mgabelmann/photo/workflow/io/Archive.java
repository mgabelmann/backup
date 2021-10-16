package mgabelmann.photo.workflow.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import mgabelmann.photo.workflow.HashType;
import mgabelmann.photo.workflow.exception.WorkflowRuntimeException;
import mgabelmann.util.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copies a number of files and/or directories to a new directory/file structure
 * that can be used for archiving files. A manifest file is also created that can
 * be used to verify the files. Files are stored by their checksum value. A single file
 * can exist multiple times in an archive if it has a different checksum. This is similar
 * to a versioning control system. The version with the latest date is the newest copy.
 * 
 * Losing the manifest file will make it next to impossible to recover the files back to
 * their original name and/or location. 
 * 
 * The directory structure in the archive is flattened to make it easier to navigate. The orignal
 * directory path is hashed and the last few digits are used to create a directory. This means
 * all files with the same path will end up in the same folder, but is not guaranteed to only be 
 * files from that folder.
 * 
 * The intent of an archive is that it will be moved to an archival format (ie: CD, DVD, BluRay).
 * 
 * @author Mike Gabelmann
 */
public final class Archive extends AbstractWorkflow {
    /** Logger. */
    private static final Logger LOG = LogManager.getLogger(Archive.class);
    
    /** 
     * Represents the number of characters in the archive directory name.
     * 16^x, so if x is 3 the number of directories will be 4096. Considering
     * Files stored in that folder are stored by their checksum the chances of
     * a collision are at best 2^64 and at worst 2^128 given SHA256.
     */
    private static final int DIR_CHECKSUM_LENGTH = 3;

    /** Collected records. */
    private transient final Collection<FileRecord> records;
    
    /**
     * Main method.
     * @param args
     */
    public static void main(final String[] args) {
        final Archive archive = new Archive(
            new File("M:/Photos/Mike/03_raw/01_working/2010"),
            new File("C:/Users/Mike/Desktop/tmp"),
            HashType.SHA256,
            false);
        
        try {
            archive.process();
        } catch (IOException ie) {
            LOG.fatal(ie);
        }
    }
    
    /**
     * Constructor.
     * @param dirLocal local directory
     * @param dirRemote remote directory
     * @param type checksum type
     * @param verify verify copied files
     */
    public Archive(final File dirLocal, final File dirRemote, final HashType type, final boolean verify) {
        super(dirLocal, dirRemote, type, verify);
        
        records = new ArrayList<>();
    }
   
    /** {@inheritDoc} */
    public void process() throws IOException {
        archiveDirectory(dirLocal);
        
        FileRecordCodec.writeFile(records, dirRemote);
        
        if (LOG.isDebugEnabled()) { LOG.debug("finished archiving files"); }
    }
    
    /** {@inheritDoc} */
    public void validate() throws IOException {
        FileRecordCodec.readFile(new File(dirRemote, FileRecordCodec.FILENAME), records);
        
        File path;
        File remoteFile;
        
        for (FileRecord record : records) {
            path = new File(record.getPath());
            
            final String dirChecksumName = calculateDirectoryName(path.getParentFile(), true, type);
            final String extension = FileUtil.getFileExtension(path);
            
            remoteFile = new File(dirRemote, dirChecksumName + File.separator + record.getSum() + "." + extension);
            
            try {
                if (FileRecordCodec.verifyFileChecksum(remoteFile, record.getType(), record.getSum())) {
                    LOG.info(record.getPath() + " checksum - passed");
                    
                } else {
                    LOG.warn(record.getPath() + " checksum - failed");
                }
                
            } catch (FileNotFoundException fnfe) {
                LOG.error(fnfe);
            }
        }
        
        if (LOG.isDebugEnabled()) { LOG.debug("finished verifying files"); }
    }
    
    /** {@inheritDoc} */
    public void restore() throws Exception {
        throw new WorkflowRuntimeException("not implemented yet");
    }
    
    /**
     * Process a directories files.
     * @param dirProcess directory to process
     * @throws IOException error processing
     */
    private void archiveDirectory(
        final File dirProcess) 
        throws IOException {
        
        final File[] files = dirProcess.listFiles();
        
        //calculate a checksum based on the source directories path and take the last few characters
        final String dirNameChecksum = calculateDirectoryName(dirProcess, true, type);
        final File dirR = new File(dirRemote, dirNameChecksum);
        
        if (files.length > 0) {
            for (File f : files) {
                if (f.isDirectory()) {
                    archiveDirectory(f);
                } else {
                    archiveFile(f, dirR);
                }
            }
            
        } else {
            LOG.debug("DIR: " + dirProcess.getAbsolutePath() + " is empty - skipping");
        }
    }
    
    /**
     * Archive a file if it is new or has changed.
     * @param file file to archive
     * @param dirR archive directory where file copied to
     * @throws IOException error archiving file
     */
    private void archiveFile(final File file, final File dirR) throws IOException {
        //create remote directory if it doesn't exist
        if (! dirR.exists()) {
            if (! dirR.mkdir()) {
                throw new IOException("unable to create directory " + dirR.getAbsolutePath());
            }
            
            LOG.info("DIR: " + dirR.getAbsolutePath() + " doesn't exist - created");
        }
        
        final String fileName = file.getName();
        final int pos = fileName.lastIndexOf('.');
        final String extension = fileName.substring(pos);
        final String fileChecksum = FileRecordCodec.calculateChecksum(file, type);
        
        final File newFile = new File(dirR, fileChecksum + extension);
       
        //create a file record and store it
        final FileRecord record = new FileRecord(file.getAbsolutePath(), fileChecksum, file.length(), new Date(file.lastModified()), type);
        records.add(record);
        
        if (newFile.exists()) {
            //chances of a collision are next to impossible (2^128 at worst, 2^64 at best) so this MUST be the same file
            LOG.debug("FILE: " + file.getAbsolutePath() + " is identical - skipping");
            
        } else {
            //copy file
            //NOTE: if a file has changed since it was last archived it compute a new hash and be archived again
            LOG.info("FILE: " + file.getAbsolutePath() + " is new - copying");
            FileUtil.copyFile(file, newFile, true);
        } 
    }
    
    /**
     * Calculate the name of a directory by calculating a checksum of it.
     * @param directory directory to calculate name for
     * @param useFullPath use directory path or directory name
     * @return name for directory calculated from its checksum
     */
    public static String calculateDirectoryName(final File directory, final boolean useFullPath, final HashType type) {
        String dirNameChecksum;
        
        if (useFullPath) {
            dirNameChecksum = FileRecordCodec.calculateChecksum(directory.getAbsolutePath(), type);
        } else {
            dirNameChecksum = FileRecordCodec.calculateChecksum(directory.getName(), type);
        }
        
        return dirNameChecksum.substring(dirNameChecksum.length() - DIR_CHECKSUM_LENGTH);
    }
    
}
