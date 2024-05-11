package mgabelmann.photo.workflow.gui;

import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import mgabelmann.photo.workflow.HashType;
import mgabelmann.photo.workflow.PhotoManifest;
import mgabelmann.photo.workflow.io.FileRecord;
import mgabelmann.photo.workflow.io.FileRecordCodec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author mgabe
 */
public final class PhotoManifestGUI 
    extends JFrame
    implements PropertyChangeListener, ActionListener {
    
    
    /** Logger. */
    private final static Logger LOG = LogManager.getLogger(PhotoManifestGUI.class);
    
    /** Parent. */
    private final PhotoManifest pm;
    
    //-------------------------- GUI COMPONENTS -------------------------------
    private JLabel labelFile;
    private JProgressBar progressBar;
    private JComboBox<HashType> cmbType;
    private JButton btnFile;
    private JButton btnProcess;
    private JButton btnVerify;
    private JButton btnReadFile;
    private JButton btnWriteFile;
    
    /**
     * Constructor.
     * @param pm
     */
    public PhotoManifestGUI(PhotoManifest pm) {
        this.pm = pm;
        
        this.init();
    }
    
    /**
     * Initialize GUI components, listeners, etc.
     */
    private void init() {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        Panel p1 = new Panel(new GridLayout(8,1));
        labelFile = new JLabel("");
        progressBar = new JProgressBar();
        cmbType = new JComboBox<>();
        btnFile = new JButton("Locate Directory");
        btnProcess = new JButton("Hash Files");
        btnVerify = new JButton("Verify Files");
        btnReadFile = new JButton("Read File");
        btnWriteFile = new JButton("Write File"); 
        
        p1.add(labelFile);
        p1.add(progressBar);
        p1.add(cmbType);
        p1.add(btnFile);
        p1.add(btnProcess);
        p1.add(btnVerify);
        p1.add(btnReadFile);
        p1.add(btnWriteFile);
        
        //add items to combobox
        for (HashType type : HashType.values()) {
            cmbType.addItem(type);
        }
        
        //set default item
        cmbType.setSelectedItem(HashType.SHA256);
        
        //add actions
        btnFile.setActionCommand("file");
        btnFile.addActionListener(this);
        
        btnProcess.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processFilesActionPerformed();
            }
        });
        
        btnVerify.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                verifyFilesActionPerformed();
            }
        });
        
        btnReadFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                readFile();
            }
        });
        
        btnWriteFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                writeFile();
            }
        });
        
        this.getContentPane().add(p1);
        
        //show window
        this.setSize(500, 250);
        this.pack();
        this.setVisible(true);
    }
    
    private void selectFileActionPerformed() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select a directory to scan");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setCurrentDirectory(new File("M:/Photos/Mike/03_raw/01_working/2010/2010-08-14"));
        int value = fc.showOpenDialog(this);
        
        if (value == JFileChooser.APPROVE_OPTION) {
            pm.setRootdir(fc.getSelectedFile());
            labelFile.setText(fc.getSelectedFile().getAbsolutePath());
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Selected directory: {}", fc.getSelectedFile().getAbsolutePath());
            }
        }
    }
    
    private void processFilesActionPerformed() {
        pm.processDirectory(pm.getRootdir());
    }
    
    private void verifyFilesActionPerformed() {
        resetProgressBar();
        
        VerifyTask task = new VerifyTask();
        task.addPropertyChangeListener(this);
        task.execute();
    }
    
    class VerifyTask extends SwingWorker<Void, Void> {
        @Override
        public Void doInBackground() {
            int progress = 0;
            setProgress(progress);
            Collection<FileRecord> records = pm.getRecords();
            
            for (final FileRecord record : records) {
                try {
                    File file = new File(record.getPath());
                    LOG.debug("Directory ({}) {}", file.getParent(), FileRecordCodec.calculateChecksum(file.getParent(), HashType.SHA256));
                    
                    boolean verified = FileRecordCodec.verifyFileRecord(record);
                      
                    if (!verified) {
                        LOG.debug("unable to verify file {}", record.getPath());
                    }
                      
                } catch (IOException ie) {
                    LOG.error(ie);
                }
                
                setProgress(++progress);
            }
            
            return null;
        }
    }
    
    private void readFile() {
        try {
            pm.readFile();
        } catch (IOException ie) {
            LOG.error(ie);
        }
    }
    
    private void writeFile() {
        try {
            pm.writeFile();
        } catch (IOException ie) {
            LOG.error(ie);
        }
    }
    
    private void resetProgressBar() {
        progressBar.setIndeterminate(false);
        progressBar.setMinimum(0);
        progressBar.setMaximum(pm.getRecordCount());
    }

    /**
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0) {
        if ("file".equals(arg0.getActionCommand())) {
            selectFileActionPerformed();
        }        
    }

    /**
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent arg0) {
        if ("progress".equals(arg0.getPropertyName())) {
            progressBar.setValue((Integer) arg0.getNewValue());
        }
    }
    
}
