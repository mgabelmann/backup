package mgabelmann.photo.copyright.gui;

import mgabelmann.photo.copyright.Copyright;
import mgabelmann.photo.copyright.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    private JPanel panel1;
    private JScrollPane scrollPane1;
    private JTable table1;
//    private JButton button1;
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
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        //TODO: initialize components
        this.menuBar = new JMenuBar();
        this.fileMenu = new JMenu("File");
        this.editMenu = new JMenu("Edit");
        this.exitMenuItem = new JMenuItem("Exit");
        this.addFileMenuItem = new JMenuItem("Add File");
        this.addDirectoryMenuItem = new JMenuItem("Add Directory");

        this.fileMenu.add(this.exitMenuItem);
        this.editMenu.add(this.addFileMenuItem);
        this.editMenu.add(this.addDirectoryMenuItem);
        this.addFileMenuItem.addActionListener(e -> add(false));
        this.addDirectoryMenuItem.addActionListener(e -> add(true));

        this.scrollPane1 = new JScrollPane();
        this.scrollPane1.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.scrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.table1 = new JTable();
        this.table1.setFillsViewportHeight(true);
        this.table1.addColumn(new TableColumn());
        this.table1.addColumn(new TableColumn());
        this.scrollPane1.setViewportView(table1);
        this.getContentPane().add(scrollPane1, BorderLayout.CENTER);

        this.tableModel1 = new FileInfoTableModel(copyright.getFileInfos(), DateTimeFormatter.ofPattern(FileInfoTableModel.DATE_FORMAT));
        this.table1.setModel(this.tableModel1);

        //add some test data
        this.tableModel1.add(new FileInfo(Path.of("file_20240502.jpg"), "title3", LocalDateTime.now()));
        this.tableModel1.add(new FileInfo(Path.of("file_20240514.jpg"), "title1", LocalDateTime.now().minusDays(10)));
        this.tableModel1.add(new FileInfo(Path.of("file_20240523.jpg"), "title2", LocalDateTime.now().minusDays(15)));

        this.menuBar.add(this.fileMenu);
        this.menuBar.add(this.editMenu);
        this.setJMenuBar(this.menuBar);

        this.panel1 = new JPanel();
        this.panel1.setLayout(new BorderLayout());
        this.getContentPane().add(this.panel1, BorderLayout.SOUTH);

//        this.button1 = new JButton("Scan");
//        this.panel1.add(button1, BorderLayout.WEST);
        this.panel1.setBorder(new EmptyBorder(5,5,5,5));

        this.button2 = new JButton("Process");
        this.panel1.add(button2, BorderLayout.EAST);

        //TODO: listeners
        this.exitMenuItem.addActionListener(e -> System.exit(0));

        //finish initialization and display UI
        this.setSize(500, 250);
        this.pack();
        this.setVisible(true);
    }

    /**
     * Add a new file or directory.
     */
    private void add(boolean directoriesOnly) {
        //set to users default directory
        JFileChooser chooser = new JFileChooser();
        //chooser.setDialogTitle("Choose file or directory");

        if (directoriesOnly) {
            chooser.setDialogTitle("Choose a directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        } else {
            chooser.setDialogTitle("Choose a file");
        }

        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
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

                /*
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
                 */

                //redraw table
                tableModel1.fireTableDataChanged();
                table1.repaint();
            }
        }
    }

}
