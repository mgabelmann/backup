package mgabelmann.photo.workflow;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import mgabelmann.photo.ApplicationMode;
import mgabelmann.photo.workflow.gui.PhotoManifestGUI;
import mgabelmann.photo.workflow.io.FileRecord;
import mgabelmann.photo.workflow.io.FileRecordCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Mike Gabelmann
 * @version 0.9
 */
public final class PhotoManifest {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(PhotoManifest.class);
    
    /** Mode of application. */
    private final transient ApplicationMode mode;
    
    /** Base directory to parse from. */
    private transient File rootdir;
    
    /**  */
    private final transient Collection<FileRecord> records;
    
    /**  */
    private final transient HashType type = HashType.SHA256;
    
    /**  */
    private transient PhotoManifestGUI pmg = null;


    /**
     * Constructor.
     * @param mode
     * @param rootdir
     */
    public PhotoManifest(final ApplicationMode mode, final File rootdir) {
        this.mode = mode;
        this.rootdir = rootdir;
        this.records = new ArrayList<>();
        
        if (this.mode == ApplicationMode.GUI) {
            this.pmg = new PhotoManifestGUI(this);
            
        } else {
            processDirectory(rootdir);
        }
    }
    
    /**
     * 
     * @param f
     */
    public void processFile(final File f) {
        try {
            final String checksum = FileRecordCodec.calculateChecksum(f, HashType.SHA256);
            
            final FileRecord record = new FileRecord(f.getAbsolutePath(), checksum, f.length(), LocalDateTime.now(), HashType.SHA256);
            records.add(record);
            
            LOGGER.debug(record.toString());
            
        } catch (IOException fnfe) {
            LOGGER.error(fnfe.getMessage());
        }
    }
    
    /**
     * 
     * @param d
     */
    public void processDirectory(final File d) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("processing directory: {}", d.getAbsolutePath());
        }
        
        final File[] files = d.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    processDirectory(file);

                } else {
                    processFile(file);
                }
            }
        }
    }
    
    /**
     * Parse a file for records.
     * @throws IOException
     */
    public void readFile() throws IOException {
        FileRecordCodec.readFile(new File(rootdir, FileRecordCodec.FILENAME), records);
    }
    
    /**
     * Write records to file.
     * @throws IOException
     */
    public void writeFile() throws IOException {
        FileRecordCodec.writeFile(records, rootdir);
    }
    
    /**
     * @return the rootdir
     */
    public File getRootdir() {
        return rootdir;
    }

    /**
     * @param rootdir the rootdir to set
     */
    public void setRootdir(final File rootdir) {
        this.rootdir = rootdir;
    }
    
    /**
     * @return the mode
     */
    public ApplicationMode getMode() {
        return mode;
    }
    
    /**
     * @return the records
     */
    public Collection<FileRecord> getRecords() {
        return records;
    }
    
    /**
     * Get the number of records.
     * @return
     */
    public int getRecordCount() {
        return records.size();
    }
    
    /**
     * Entry point for application.
     * @param args arguments
     */
    public static void main(final String[] args) {
        boolean error = false;
        ApplicationMode mode = ApplicationMode.GUI;
        File directory = null;
        
        if (args.length >= 1) {
            try {
                mode = ApplicationMode.valueOf(args[0]);

            } catch (IllegalArgumentException iae) {
                LOGGER.error("invalid ApplicationMode={}, choose from {}", args[0], ApplicationMode.values());
                error = true;
            }
        }
        
        if (args.length == 2) {
            directory = new File(args[1]);

            if (!directory.exists()) {
                LOGGER.error("{} does not exist", args[1]);
                error = true;

            } else if (!directory.isDirectory()) {
                LOGGER.error("{} is not a directory", args[1]);
                error = true;
            }
        }

        if (error) System.exit(1);

        //create application
        new PhotoManifest(mode, directory);
    }
    
}
