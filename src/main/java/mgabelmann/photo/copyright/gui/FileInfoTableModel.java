package mgabelmann.photo.copyright.gui;

import mgabelmann.photo.copyright.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;


public class FileInfoTableModel extends AbstractTableModel {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileInfoTableModel.class);

    public static final String DATE_FORMAT_YEARMONTHDAY = "yyyy-MM-dd";

    private final List<FileInfo> data;

    private final EventListenerList listenerList = new EventListenerList();

    private final DateTimeFormatter formatter;

    private final ResourceBundle resourceBundle;


    public FileInfoTableModel(final ResourceBundle resourceBundle, List<FileInfo> data, final DateTimeFormatter formatter) {
        this.resourceBundle = resourceBundle;
        this.data = data;
        this.formatter = formatter;
    }

    private String getResourceByKey(final String key) {
        return resourceBundle.getString(key);
    }

    public void add(final FileInfo fileInfo) {
        if (!data.contains(fileInfo)) {
            data.add(fileInfo);
        }
    }

    public void remove(final FileInfo fileInfo) {
        this.data.remove(fileInfo);
    }

    public List<FileInfo> getData() {
        return data;
    }

    @Override
    public int getRowCount() {
        return this.data.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> getResourceByKey("table.header.file");
            case 1 -> getResourceByKey("table.header.title");
            case 2 -> getResourceByKey("table.header.date");
            default -> getResourceByKey("table.header.other");
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0, 1, 2 -> String.class;
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        FileInfo fileInfo = data.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> fileInfo.getPath().getFileName();
            case 1 -> fileInfo.getTitle();
            case 2 -> fileInfo.getDate().format(formatter);
            default -> null;
        };
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        FileInfo fileInfo = data.get(rowIndex);

        if (columnIndex == 1) {
            fileInfo.setTitle((String) aValue);
        }
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        this.listenerList.add(TableModelListener.class, l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        this.listenerList.remove(TableModelListener.class, l);
    }

}
