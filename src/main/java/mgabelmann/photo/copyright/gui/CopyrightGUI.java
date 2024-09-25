package mgabelmann.photo.copyright.gui;

import mgabelmann.photo.copyright.Copyright;
import mgabelmann.photo.copyright.FileInfo;
import mgabelmann.photo.workflow.exception.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 *
 * @author mgabe
 */
public class CopyrightGUI extends JFrame {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CopyrightGUI.class);

    /** Underlying class that does all the work. */
    private final transient Copyright copyright;

    private final ResourceBundle resourceBundle;

    //TODO: add components
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu editMenu;
    private JMenuItem exitMenuItem;
    private JMenuItem addFileMenuItem;
    private JMenuItem addDirectoryMenuItem;
    private JMenuItem removeMenuItem;

    private JPanel panel1;
    private JScrollPane scrollPane1;
    private JTable table1;
    private JButton button2;
    private FileInfoTableModel tableModel1;
    private CopyrightOptionsPanel cop;


    /** Quick and dirty initialization, with defaults. */
    public static void main(final String[] args) {
        Path directory = Paths.get(System.getProperty("user.home"));
        String caseNumber = "xxx";

        Copyright c = new Copyright(directory, caseNumber, false);
        CopyrightGUI gui = new CopyrightGUI(c);
    }

    /**
     * Constructor.
     * @param copyright copyright
     */
    public CopyrightGUI(final Copyright copyright) {
        this.copyright = copyright;
        this.resourceBundle = copyright.getResourceBundle();

        this.init();
    }

    /**
     * Initialize GUI components, listeners, etc.
     */
    private void init() {
        this.exitMenuItem = new JMenuItem(getResourceByKey("menu.file.exit"));
        this.exitMenuItem.addActionListener(e -> System.exit(0));

        this.addFileMenuItem = new JMenuItem(getResourceByKey("menu.edit.addfile"));
        this.addFileMenuItem.addActionListener(e -> add(false));

        this.addDirectoryMenuItem = new JMenuItem(getResourceByKey("menu.edit.adddir"));
        this.addDirectoryMenuItem.addActionListener(e -> add(true));

        this.removeMenuItem = new JMenuItem(getResourceByKey("menu.edit.remove"));
        this.removeMenuItem.addActionListener(e -> remove());

        this.fileMenu = new JMenu(getResourceByKey("menu.file"));
        this.fileMenu.add(this.exitMenuItem);

        this.editMenu = new JMenu(getResourceByKey("menu.edit"));
        this.editMenu.add(this.addFileMenuItem);
        this.editMenu.add(this.addDirectoryMenuItem);
        this.editMenu.add(this.removeMenuItem);

        this.tableModel1 = new FileInfoTableModel(resourceBundle, copyright.getFileInfos(), DateTimeFormatter.ofPattern(FileInfoTableModel.DATE_FORMAT_YEARMONTHDAY));

        this.table1 = new JTable();
        this.table1.setFillsViewportHeight(true);
        this.table1.addColumn(new TableColumn());
        this.table1.addColumn(new TableColumn());
        this.table1.setModel(this.tableModel1);
        this.table1.setDefaultRenderer(Object.class, new RowCellRenderer());

        this.scrollPane1 = new JScrollPane();
        this.scrollPane1.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.scrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.scrollPane1.setViewportView(table1);

        this.menuBar = new JMenuBar();
        this.menuBar.add(this.fileMenu);
        this.menuBar.add(this.editMenu);
        this.setJMenuBar(this.menuBar);

        this.button2 = new JButton(getResourceByKey("button.process"));
        this.button2.addActionListener(e -> this.process());

        this.panel1 = new JPanel();
        this.panel1.setLayout(new BorderLayout());
        this.panel1.setBorder(new EmptyBorder(5,5,5,5));
        this.panel1.add(button2, BorderLayout.EAST);

        this.cop = new CopyrightOptionsPanel(copyright);

        this.getContentPane().add(cop, BorderLayout.NORTH);
        this.getContentPane().add(scrollPane1, BorderLayout.CENTER);
        this.getContentPane().add(this.panel1, BorderLayout.SOUTH);

        {
            //contextual menu that emulates Edit menu items
            ContextualMenu cm = new ContextualMenu();
            table1.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        cm.show(table1, e.getX(), e.getY());
                    }
                }
            });

            JMenuItem cmAddFile = new JMenuItem(getResourceByKey("menu.edit.addfile"));
            cmAddFile.addActionListener(e -> add(false));
            //cmAddFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
            cm.add(cmAddFile);

            JMenuItem cmAddDirectory = new JMenuItem(getResourceByKey("menu.edit.adddir"));
            cmAddDirectory.addActionListener(e -> add(true));
            cm.add(cmAddDirectory);

            JMenuItem cmRemove = new JMenuItem(getResourceByKey("menu.edit.remove"));
            cmRemove.addActionListener(e -> remove());
            cm.add(cmRemove);
        }

        this.cop.update();

        //finish initialization and display UI
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(900, 600);
        this.setVisible(true);
    }

    private String getResourceByKey(final String key) {
        return resourceBundle.getString(key);
    }

    /**
     * Add a new file or directory.
     * @param directoriesOnly directories or file
     */
    private void add(final boolean directoriesOnly) {
        //set to users default directory
        JFileChooser chooser = new JFileChooser();

        if (directoriesOnly) {
            chooser.setDialogTitle(getResourceByKey("dialog.filechooser.dir"));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        } else {
            chooser.setDialogTitle(getResourceByKey("dialog.filechooser.file"));
        }

        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(final File f) {
                if (f.isDirectory()) {
                    return true;

                } else {
                    return f.getName().toLowerCase().matches(".*\\.jpe?g");
                }
            }

            @Override
            public String getDescription() {
                return getResourceByKey("dialog.filechooser.filter");
            }
        });

        int returnVal = chooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();

            if (f != null && f.exists()) {
                LOGGER.debug("item chosen: {}", f.getAbsolutePath());

                try {
                    if (f.isDirectory()) {
                        List<FileInfo> records = copyright.collect(f.toPath());
                        copyright.add(records);

                    } else {
                        FileInfo fi = copyright.collectFile(f.toPath());
                        copyright.add(List.of(fi));
                    }

                } catch (WorkflowException we) {
                    LOGGER.error(we.getMessage());
                }

                //update and redraw table UI
                this.update();
            }
        }

        this.cop.update();
    }

    /**
     * Remove one or more entries.
     */
    private void remove() {
        int[] selected = table1.getSelectedRows();
        LOGGER.debug("remove indexes: {}", selected);

        if (selected.length > 0) {
            //remove in reverse order as indexes would change otherwise
            for (int i = selected.length - 1; i >= 0; i--) {
                FileInfo fi = copyright.getFileInfos().remove(selected[i]);
                LOGGER.debug("removed {}", fi);
            }

            //unselect rows so we don't reselect different rows
            this.table1.clearSelection();

            this.update();
            this.cop.updateImageCount();
        }

    }

    /**
     * Process records.
     */
    private void process() {
        LOGGER.debug("process");

        try {
            copyright.process();

        } catch (final WorkflowException we) {
            LOGGER.error(we.getMessage());
            JOptionPane.showMessageDialog(this, we.getMessage(), getResourceByKey("dialog.message.error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Update the UI.
     */
    private void update() {
        tableModel1.fireTableDataChanged();
        table1.repaint();
        scrollPane1.updateUI();
    }

}
