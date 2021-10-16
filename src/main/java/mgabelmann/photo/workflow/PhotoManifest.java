package mgabelmann.photo.workflow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import mgabelmann.photo.workflow.gui.PhotoManifestGUI;
import mgabelmann.photo.workflow.io.FileRecord;
import mgabelmann.photo.workflow.io.FileRecordCodec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * @author Mike Gabelmann
 * @version 0.9
 */
public final class PhotoManifest {
    /** Mode of application. */
    private enum Mode {
        GUI, //Graphical Mode
        CLI  //Command line Mode
    }
    
    /** Logger. */
    private final static Logger LOG = LogManager.getLogger(PhotoManifest.class);
    
    /** Mode of application. */
    private final transient Mode mode;
    
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
    public PhotoManifest(final Mode mode, final File rootdir) {
        this.mode = mode;
        this.rootdir = rootdir;
        this.records = new ArrayList<>();
        
        if (mode == Mode.GUI) {
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
            
            if (LOG.isDebugEnabled()) { LOG.debug(record); }
            
        } catch (FileNotFoundException fnfe) {
            LOG.error(fnfe);
            //System.err.println(fnfe);
            
        } catch (IOException ie) {
            LOG.error(ie);
            //System.err.println(ie);
        }
    }
    
    /**
     * 
     * @param d
     */
    public void processDirectory(final File d) {
        if (LOG.isDebugEnabled()) { 
            LOG.debug("processing directory: " + d.getAbsolutePath()); 
        }
        
        final File[] files = d.listFiles();
        
        for (File file : files) {
            if (file.isDirectory()) {
                processDirectory(file);
            } else {
                processFile(file);
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
    public Mode getMode() {
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
    public final static void main(final String[] args) {
        boolean error = false;
        Mode mode = Mode.GUI;
        File directory = null;
        
        if (args.length >= 1) {
            try {
                mode = Mode.valueOf(args[0]);

            } catch (IllegalArgumentException iae) {
                LOG.fatal("invalid Mode={}, choose from {}", args[0], Mode.values());
                error = true;
            }
        }
        
        if (args.length == 2) {
            directory = new File(args[1]);

            if (!directory.exists()) {
                LOG.fatal("{} does not exist", args[1]);
                error = true;

            } else if (!directory.isDirectory()) {
                LOG.fatal("{} is not a directory", args[1]);
                error = true;
            }
        }

        if (error) System.exit(1);

        //create application
        new PhotoManifest(mode, directory);
    }
    
}
