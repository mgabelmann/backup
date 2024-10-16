package mgabelmann.photo.workflow.io;

import java.time.LocalDateTime;
import java.util.Objects;

import mgabelmann.photo.workflow.HashType;


/**
 * A record that contains information about a file. This information is written to
 * a manifest that is used when backing up or archiving files. This same information can
 * be used to verify the backup/archive and potentially restore from it.
 * 
 * @author Mike Gabelmann
 */
public final class FileRecord implements Comparable<FileRecord> {
    /** Absoulte file path. */
    private final transient String path;
    
    /** Checksum. */
    private final transient String sum;
    
    /** File size in bytes. */
    private final transient long size;
    
    /** Date encoded. */
    private final transient LocalDateTime date;
    
    /** Checksum type. */
    private final transient HashType type;


    /**
     * Constructor.
     * @param path
     * @param sum
     * @param size
     * @param date
     * @param type
     */
    public FileRecord(
        final String path, 
        final String sum, 
        final long size, 
        final LocalDateTime date,
        final HashType type) {

        if (path == null || sum == null || date == null || type == null) {
            throw new IllegalArgumentException("values required");
        }

        this.path = path;
        this.sum = sum;
        this.size = size;
        this.date = date;
        this.type = type;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @return the sum
     */
    public String getSum() {
        return sum;
    }

    /**
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * @return the date
     */
    public LocalDateTime getDate() {
        return date;
    }

    /**
     * @return the type
     */
    public HashType getType() {
        return type;
    }

    @Override
    public int compareTo(final FileRecord arg0) {
        return this.path.compareTo(arg0.getPath());
    }

    @Override
    public String toString() {
        return "FileRecord{" + "path='" + path + '\'' +
                ", sum='" + sum + '\'' +
                ", size=" + size +
                ", date=" + date +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileRecord that = (FileRecord) o;
        return size == that.size && path.equals(that.path) && sum.equals(that.sum) && date.equals(that.date) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, sum, size, date, type);
    }

}
