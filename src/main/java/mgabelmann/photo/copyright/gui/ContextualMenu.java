package mgabelmann.photo.copyright.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;


public class ContextualMenu extends JPopupMenu {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContextualMenu.class);


    public ContextualMenu() {

    }

    public void addJMenuItem(final JMenuItem addFileMenuItem) {
        this.add(addFileMenuItem);
    }

    public void addSeparator() {
        this.add(new JSeparator());
    }

}
