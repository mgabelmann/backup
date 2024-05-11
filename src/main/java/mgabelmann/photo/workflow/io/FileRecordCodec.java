package mgabelmann.photo.workflow.io;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Collection;

import mgabelmann.photo.workflow.HashType;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles the reading and writing of FileRecord objects. 
 * @author Mike Gabelmann
 */
public final class FileRecordCodec {
    /** Logger. */
    private static final Logger LOG = LogManager.getLogger(FileRecordCodec.class);
    
    /** Date mask format. */
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    
    /** Filename. */
    public static final String FILENAME = "manifest.txt";
    
    /** Parameter separator. */
    public static final String SEPARATOR = "\t";
    

    /** Do not instantiate this class. */
    private FileRecordCodec() {}
    
    /**
     * Read a manifest file that contains FileRecord information.
     * @param manifest location of the manifest file
     * @param records collection to store contents of manifest file
     * @throws IOException error reading file
     */
    public static void readFile(
        final File manifest,
        final Collection<FileRecord> records) 
        throws IOException {
        
        final BufferedReader br = new BufferedReader(new FileReader(manifest));
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("opening file={}", manifest.getAbsolutePath());
        }
        
        String data;
        while((data = br.readLine()) != null) {
            try {
                records.add(readFileRecord(data));
            } catch (ParseException pe) {
                LOG.warn(pe);
            }
        }
        
        br.close();
    }
    
    /**
     * Write a collection of FileRecords to disk.
     * @param records collection to write
     * @param directory location to write records to
     * @throws IOException error writing file
     */
    public static void writeFile(
        final Collection<FileRecord> records, 
        final File directory) 
        throws IOException {
        
        final File f = new File(directory, FILENAME);
        
        if (! f.exists() && ! f.createNewFile()) {
            throw new IOException("could not create file " + f.getAbsolutePath());
        }
        
        final DataOutputStream dos = new DataOutputStream(new FileOutputStream(f));
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating file={}", f.getAbsolutePath());
        }
        
        for (FileRecord record : records) {
            FileRecordCodec.writeFileRecord(dos, record);
        }
        
        dos.close();
    }
    
    /**
     * Verify that the given record has a valid checksum.
     * @param record record to verify
     * @return true if checksums match, false otherwise
     * @throws IOException error processing file
     */
    public static boolean verifyFileRecord(final FileRecord record) throws IOException {
        return FileRecordCodec.verifyFileChecksum(new File(record.getPath()), record.getType(), record.getSum());
    }
    
    /**
     * Verify that the given files checksum matches the actual files checksum.
     * @param file file to verify checksum
     * @param type checksum algorithm
     * @param checksum old checksum
     * @return true if equal, false otherwise
     * @throws IOException error calculating checksum
     */
    public static boolean verifyFileChecksum(final File file, final HashType type, final String checksum) throws IOException {
        final String newChecksum = FileRecordCodec.calculateChecksum(file, type);
        return newChecksum.equals(checksum);
    }
    
    /**
     * Calculate a hex checksum for a given file and algorithm.
     * @param f file to calculate checksum
     * @param type checksum type
     * @return checksum in hex format
     * @throws IOException error calculating checksum
     * @throws IllegalArgumentException invalid type
     */
    public static String calculateChecksum(final File f, final HashType type) throws IOException {
        try (FileInputStream fis = new FileInputStream(f)) {
            return switch (type) {
                case MD5 -> DigestUtils.md5Hex(fis);
                case SHA256 -> DigestUtils.sha256Hex(fis);
                case SHA384 -> DigestUtils.sha384Hex(fis);
                case SHA512 -> DigestUtils.sha512Hex(fis);
            };
        }
    }
    
    /**
     * Calculate a hex checksum for the given string and algorithm.
     * @param s value to calculate
     * @param type algorithm
     * @return hex checksum
     */
    public static String calculateChecksum(final String s, final HashType type) {
        return switch (type) {
            case MD5 -> DigestUtils.md5Hex(s);
            case SHA256 -> DigestUtils.sha256Hex(s);
            case SHA384 -> DigestUtils.sha384Hex(s);
            case SHA512 -> DigestUtils.sha512Hex(s);
        };
    }
    
    /**
     * Parse a string record into a FileRecord.
     * @param data string record to parse
     * @return parsed record
     * @throws ParseException error parsing record
     */
    private static FileRecord readFileRecord(final String data) throws ParseException {
        final String[] fields = data.split(SEPARATOR);
        
        if (fields.length != 5) {
            LOG.warn("invalid record ({}). skipping", data);
            throw new ParseException(data, 0);
        }

        return new FileRecord(
            fields[0],
            fields[4], 
            Long.parseLong(fields[2]),
            LocalDateTime.parse(fields[1]),
            HashType.valueOf(fields[3]));
    }
    
    /**
     * Output a FileRecord as a string record.
     * @param dos stream
     * @param record record to write
     * @throws IOException error writing record to stream
     */
    private static void writeFileRecord(final DataOutputStream dos, final FileRecord record) throws IOException {
        final StringBuilder sb = new StringBuilder();

        sb.append(record.getPath());
        sb.append(SEPARATOR);
        sb.append(record.getDate());
        sb.append(SEPARATOR);
        sb.append(record.getSize());
        sb.append(SEPARATOR);
        sb.append(record.getType().toString());
        sb.append(SEPARATOR);
        sb.append(record.getSum());
        sb.append('\n');
        
        dos.writeBytes(sb.toString());
        
        if (LOG.isDebugEnabled()) { 
            LOG.debug(sb.toString()); 
        }
    }
      
}
