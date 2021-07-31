package mgabelmann.util;

import java.text.DecimalFormat;

/**
 * 
 * @author Mike Gabelmann
 */
public enum ByteConversion {
    BYTES("Bytes", "b", 1), 
    KILOBYTES("Kilobytes", "kB", 1024L), 
    MEGABYTES("Megabytes", "MB", 1024L * 1024), 
    GIGABYTES("Gigabytes", "GB", 1024L * 1024 * 1024), 
    TERABYTES("Terabytes", "TB", 1024L * 1024 * 1024 * 1024), 
    PETABYTES("Petabytes", "PB", 1024L * 1024 * 1024 * 1024 * 1024), 
    ;

    /** Number format. */
    private static final String FORMAT = "###,###,###,###,###.0";
    
    /** Long name. */
    public final String name;
    
    /** Short name. */
    public final String abbrev;
    
    /** Size in bytes. */
    public final long size;

    /**
     * Constructor.
     * @param name long name
     * @param abbrev short name
     * @param size (in bytes)
     */
    private ByteConversion(
        final String name, 
        final String abbrev,
        final long size) {
        
        this.name = name;
        this.abbrev = abbrev;
        this.size = size;
    }

    public String getName() {
        return name;
    }
    
    public String getAbbrev() {
        return abbrev;
    }
    
    public long getSize() {
        return size;
    }
    
    public double calculate(final long bytes) {
        return (double) bytes / (double) size;
    }
    
    public String formatShortName(final long bytes) {
        return (format(calculate(bytes)) + abbrev);
    }
    
    public String formatLongName(final long bytes) {
        return (format(calculate(bytes)) + " " + name);
    }
    
    private String format(final double bytes) {
        DecimalFormat formatter = new DecimalFormat(FORMAT);
        return formatter.format(bytes);
    }
    
    /**
     * Get formatted string using the units closest to the value
     * @param bytes size
     * @return formatted string
     */
    public static String format(final long bytes) {
        for (int i=ByteConversion.values().length - 1; i >= 0; i--) {
            double calculated = ByteConversion.values()[i].calculate(bytes);
            
            if (Double.compare(calculated, 1) >= 0) {
                return ByteConversion.values()[i].formatShortName(bytes);
            }
        }
        
        return ByteConversion.BYTES.formatShortName(bytes);
    }
}
