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
    private final Color evenRow = new Color(245, 245, 245);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        //don't use bold font by default
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        c.setFont(c.getFont().deriveFont(Font.PLAIN));

        if (hasFocus && column == 1) {
            //for editing a cell
            this.setBackground(Color.cyan);

        } else if (isSelected) {
            //row is selected
            this.setBackground(Color.yellow);

        } else if (row % 2 == 0) {
            //even row
            this.setBackground(evenRow);

        } else {
            //odd row
            this.setBackground(Color.white);
        }

        this.setForeground(Color.black);
        this.setText(value.toString());

        return this;
    }

}
