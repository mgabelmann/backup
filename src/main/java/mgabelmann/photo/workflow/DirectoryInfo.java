package mgabelmann.photo.workflow;

import java.io.File;

import mgabelmann.util.ByteConversion;

/**
 * 
 * @author Mike Gabelmann
 */
public final class DirectoryInfo {
    /** Directory to calculate information for. */
    private File directory;
    
    /** Number of files found. */
    private long numFiles;
    
    /** Number of directories found. */
    private long numDirs;
    
    /** Total size in bytes. */
    private long totalSize;

    
    /**
     * Constructor.
     * @param directory
     */
    public DirectoryInfo(final File directory) {
        super();
        this.directory = directory;
    }

    /**
     * @return the directory
     */
    public File getDirectory() {
        return directory;
    }

    /**
     * @param directory the directory to set
     */
    public void setDirectory(File directory) {
        this.directory = directory;
    }

    /**
     * @return the numFiles
     */
    public long getNumFiles() {
        return numFiles;
    }

    /**
     * @return the numDirs
     */
    public long getNumDirs() {
        return numDirs;
    }

    /**
     * @return the totalSize
     */
    public long getTotalSize() {
        return totalSize;
    }
    
    public synchronized void addFile() {
        numFiles++;
    }
    
    public synchronized void addDirectory() {
        numDirs++;
    }
    
    public synchronized void addSize(long size) {
        totalSize += size;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DirectoryInfo [directory=");
        builder.append(directory);
        builder.append(", numFiles=");
        builder.append(numFiles);
        builder.append(", numDirs=");
        builder.append(numDirs);
        builder.append(", totalSize=");
        builder.append(totalSize);
        
        String s = ByteConversion.format(totalSize);
        builder.append(" (");
        builder.append(s);
        builder.append(")");
        
        builder.append("]");
        return builder.toString();
    }
}
