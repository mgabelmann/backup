package mgabelmann.photo.copyright.gui;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;


/**
 * Custom row colors for odd/even and selected.
 */
public class RowCellRenderer extends DefaultTableCellRenderer {
    public static final Color COLOR_EVEN_ROW = new Color(245, 245, 245);
    public static final Color COLOR_ODD_ROW = Color.white;
    public static final Color COLOR_SELECTED_CELL = Color.yellow;
    public static final Color COLOR_EDITING_CELL = Color.cyan;
    public static final Color COLOR_FOREGROUND_TEXT = Color.black;


    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        //don't use bold font by default
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        c.setFont(c.getFont().deriveFont(Font.PLAIN));

        if (hasFocus && (column == 1 || column == 2)) {
            //for editing a cell
            this.setBackground(COLOR_EDITING_CELL);

        } else if (isSelected) {
            //row is selected
            this.setBackground(COLOR_SELECTED_CELL);

        } else if (row % 2 == 0) {
            //even row
            this.setBackground(COLOR_EVEN_ROW);

        } else {
            //odd row
            this.setBackground(COLOR_ODD_ROW);
        }

        this.setForeground(COLOR_FOREGROUND_TEXT);
        this.setText(value.toString());

        return this;
    }

}
