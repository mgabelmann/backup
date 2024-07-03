package mgabelmann.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import mgabelmann.photo.workflow.HashType;
import mgabelmann.photo.workflow.io.FileRecordCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author Mike Gabelmann
 */
public final class FileUtil {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);
    
    /** Do not instantiate this class. */
    private FileUtil() {}
    
    /**
     * Copy a single file. Ensures last modified date remains the same.
     * @param source file to copy
     * @param dest new file
     * @param preserveLastModified preserve last modified date
     * @throws IOException error copying file
     */
    public static void copyFile(
        final File source, 
        final File dest,
        final boolean preserveLastModified)
        throws IOException {
        
        //create a new file if it doesnt exist
        if (! dest.exists() && ! dest.createNewFile()) {
            throw new IOException("could not create new file - " + dest.getAbsolutePath());
        }
       
        try (FileInputStream fis = new FileInputStream(source);
        	 FileOutputStream fos = new FileOutputStream(dest)) {
        	
        	FileChannel sourceChannel = fis.getChannel();
            FileChannel destChannel = fos.getChannel();
            
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
          
            sourceChannel.close();
            destChannel.close();
        	
        } finally {
        	LOGGER.trace("closed channels and streams");
        }
       
        //preserve last modified date (we check this)
        if (preserveLastModified && ! dest.setLastModified(source.lastModified())) {
            LOGGER.warn("FILE: {} unable to set last modified date", dest.getAbsolutePath());
        }
    }
    
    /**
     * Verify the copy succeeded by performing a checksum on both files and comparing them.
     * @param source source file
     * @param dest destination file
     * @param type type
     * @return true if equal, false otherwise
     * @throws IOException error verifying copy
     */
    public static boolean verifyCopy(
        final File source, 
        final File dest, 
        final HashType type) 
        throws IOException {
        
        final String localCheck = FileRecordCodec.calculateChecksum(source, type);
        final String newCheck = FileRecordCodec.calculateChecksum(dest, type);
        
        return localCheck.equals(newCheck);
    }
    
    /**
     * Given a file determine its file extension.
     * @param file file to process
     * @return empty string or extension without the '.'
     */
    public static String getFileExtension(final File file) {
        return file == null ? "" : getFileExtension(file.getName());
    }
    
    /**
     * Given an optional path and filename determine the files extension.
     * @param filename filename
     * @return empty string or extension without the '.'
     */
    public static String getFileExtension(final String filename) {
        final int pos = filename == null ? -1 : filename.lastIndexOf('.');
        return pos < 0 ? "" : filename.substring(pos + 1);
    }
}
