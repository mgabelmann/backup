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
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 *
 * @author mgabe
 */
public class CopyrightGUI extends JFrame {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CopyrightGUI.class);

    /** Underlying class that does all the work. */
    private final transient Copyright copyright;

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


    /** Quick and dirty initialization, with defaults. */
    public static void main(final String[] args) {
        Copyright c = new Copyright(Paths.get(System.getProperty("user.home")), "xxx", false);
        CopyrightGUI gui = new CopyrightGUI(c);
    }

    /**
     * Constructor.
     * @param copyright copyright
     */
    public CopyrightGUI(final Copyright copyright) {
        this.copyright = copyright;

        this.init();
    }

    /**
     * Initialize GUI components, listeners, etc.
     */
    private void init() {
        this.exitMenuItem = new JMenuItem("Exit");
        this.exitMenuItem.addActionListener(e -> System.exit(0));

        this.addFileMenuItem = new JMenuItem("Add File");
        this.addFileMenuItem.addActionListener(e -> add(false));

        this.addDirectoryMenuItem = new JMenuItem("Add Directory");
        this.addDirectoryMenuItem.addActionListener(e -> add(true));

        this.removeMenuItem = new JMenuItem("Remove File(s)");
        this.removeMenuItem.addActionListener(e -> remove());

        this.fileMenu = new JMenu("File");
        this.fileMenu.add(this.exitMenuItem);

        this.editMenu = new JMenu("Edit");
        this.editMenu.add(this.addFileMenuItem);
        this.editMenu.add(this.addDirectoryMenuItem);
        this.editMenu.add(this.removeMenuItem);

        this.tableModel1 = new FileInfoTableModel(copyright.getFileInfos(), DateTimeFormatter.ofPattern(FileInfoTableModel.DATE_FORMAT));

        //NOTE: add some test data, temporary
        this.tableModel1.add(new FileInfo(Path.of("file_20240502.jpg"), "title3", LocalDateTime.now()));
        this.tableModel1.add(new FileInfo(Path.of("file_20240514.jpg"), "", LocalDateTime.now().minusDays(10)));
        this.tableModel1.add(new FileInfo(Path.of("file_20240523.jpg"), "title2", LocalDateTime.now().minusDays(15)));

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

        this.button2 = new JButton("Process");
        this.button2.addActionListener(e -> this.process());

        this.panel1 = new JPanel();
        this.panel1.setLayout(new BorderLayout());
        this.panel1.setBorder(new EmptyBorder(5,5,5,5));
        this.panel1.add(button2, BorderLayout.EAST);

        {
            JPanel panel2 = new JPanel();

        }

        this.getContentPane().add(scrollPane1, BorderLayout.CENTER);
        this.getContentPane().add(this.panel1, BorderLayout.SOUTH);

        //finish initialization and display UI
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(900, 600);
        this.setVisible(true);
    }

    /**
     * Add a new file or directory.
     * @param directoriesOnly directories or file
     */
    private void add(final boolean directoriesOnly) {
        //set to users default directory
        JFileChooser chooser = new JFileChooser();

        if (directoriesOnly) {
            chooser.setDialogTitle("Choose a directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        } else {
            chooser.setDialogTitle("Choose a file");
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
                return "JPG Images (*.jpg or *.jpeg)";
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
                this.updateUI();
            }
        }
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

            this.updateUI();
        }
    }

    /**
     * Process records.
     */
    private void process() {
        LOGGER.debug("process");

        try {
            //copyright.process();
            throw new WorkflowException("Processing failed");

        } catch (WorkflowException we) {
            LOGGER.error(we.getMessage());
            JOptionPane.showMessageDialog(this, we.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Update the UI.
     */
    private void updateUI() {
        tableModel1.fireTableDataChanged();
        table1.repaint();
        scrollPane1.updateUI();
    }

}
