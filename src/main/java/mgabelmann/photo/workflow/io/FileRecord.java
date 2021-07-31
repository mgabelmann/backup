package mgabelmann.photo.workflow.io;

import java.util.Date;

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
    private final transient Date date;
    
    /** Checksum type. */
    private final transient HashType type;

    /**
     * 
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
        final Date date, 
        final HashType type) {
        
        super();
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
    public Date getDate() {
        return date;
    }

    /**
     * @return the type
     */
    public HashType getType() {
        return type;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("FileRecord [path=");
        builder.append(path);
        builder.append(", sum=");
        builder.append(sum);
        builder.append(", size=");
        builder.append(size);
        builder.append(", date=");
        builder.append(date);
        builder.append(", type=");
        builder.append(type);
        builder.append("]");
        return builder.toString();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + (int) (size ^ (size >>> 32));
        result = prime * result + ((sum == null) ? 0 : sum.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FileRecord other = (FileRecord) obj;
        if (date == null) {
            if (other.date != null)
                return false;
        } else if (!date.equals(other.date))
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (size != other.size)
            return false;
        if (sum == null) {
            if (other.sum != null)
                return false;
        } else if (!sum.equals(other.sum))
            return false;
        if (type != other.type)
            return false;
        return true;
    }
    
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    
    public int compareTo(final FileRecord arg0) {
        return this.path.compareTo(arg0.getPath());
    }
}
