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
    public static final Color colorEvenRow = new Color(245, 245, 245);
    public static final Color colorOddRow = Color.white;
    public static final Color colorSelectedCell = Color.yellow;
    public static final Color colorEditingCell = Color.cyan;
    public static final Color colorForegroundText = Color.black;


    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        //don't use bold font by default
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        c.setFont(c.getFont().deriveFont(Font.PLAIN));

        if (hasFocus && (column == 1 || column == 2)) {
            //for editing a cell
            this.setBackground(colorEditingCell);

        } else if (isSelected) {
            //row is selected
            this.setBackground(colorSelectedCell);

        } else if (row % 2 == 0) {
            //even row
            this.setBackground(colorEvenRow);

        } else {
            //odd row
            this.setBackground(colorOddRow);
        }

        this.setForeground(colorForegroundText);
        this.setText(value.toString());

        return this;
    }

}
