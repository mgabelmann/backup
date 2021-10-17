package mgabelmann.photo.workflow.io;

import java.io.File;

import mgabelmann.photo.workflow.DirectoryInfo;

/**
 * Process a directory and all its files. This information can be used to 
 * determine how long it will take to process using a more complex task.
 * 
 * If you know how many files/folders exist you can display a progressbar
 * when an item is processed.
 * 
 * @author Mike Gabelmann
 */
public final class DirectoryInfoCodec {

    /** Do not instantiate this class. */
    private DirectoryInfoCodec() {}
    
    /**
     * Given a directory process all its files and directories.
     * @param dir directory to process
     * @return information
     */
    public static DirectoryInfo calculateInfo(final File dir) {
        final DirectoryInfo info = new DirectoryInfo(dir);
        
        processDirectory(dir, info);
        
        return info;
    }
    
    /**
     * Process a directory.
     * @param dir current directory
     * @param info information object
     */
    private static void processDirectory(final File dir, final DirectoryInfo info) {
        File[] files = dir.listFiles();

        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    //add directory information and process it
                    info.addDirectory();
                    processDirectory(f, info);

                } else {
                    //add File information
                    info.addFile();
                    info.addSize(f.length());
                }
            }
        }
    }
    
}
