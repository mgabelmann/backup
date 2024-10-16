package mgabelmann.photo.workflow;

import java.io.Serializable;
import java.util.Comparator;

import mgabelmann.photo.workflow.io.FileRecord;

/**
 * 
 * @author Mike Gabelmann
 */
public final class FileRecordComparator implements Comparator<FileRecord>, Serializable {
    /** Fields available for ordering. */
    public enum Field {
        PATH,
        SUM,
        SIZE,
        DATE,
        TYPE
    }
    
    /** Field being sorted. */
    private final transient Field field;
    
    /**
     * Constructor, uses Field.PATH as default.
     */
    public FileRecordComparator() {
        this(Field.PATH);
    }
    
    /**
     * Constructor.
     * @param field field to sort on
     */
    public FileRecordComparator(final Field field) {
        this.field = field;
    }

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(final FileRecord arg0, final FileRecord arg1) {
        int compare;
        
        switch(field) {
        case SUM:
            compare = arg0.getSum().compareTo(arg1.getSum());
            break;
        
        case SIZE:
            if (arg0.getSize() == arg1.getSize()) {
                compare = 0;
            } else if (arg0.getSize() <= arg1.getSize()) {
                compare = -1;
            } else {
                compare = 1;
            }
            
            break;
            
        case DATE:
            compare = arg0.getDate().compareTo(arg1.getDate());
            break;
        
        case TYPE:
            compare = arg0.getType().compareTo(arg1.getType());
            break;
            
        case PATH:
        default:
            compare = arg0.compareTo(arg1);
        }
        
        return compare;
    }

}
