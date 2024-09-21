package mgabelmann.photo.copyright.gui;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import mgabelmann.photo.copyright.Copyright;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.nio.file.Path;


public class CopyrightOptionsPanel extends JPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(CopyrightOptionsPanel.class);

    private final Copyright copyright;

    private final JTextField textField1;
    private final JTextField textField2;
    private final JCheckBox checkBox1;
    private final JTextField textField3;


    public CopyrightOptionsPanel(Copyright copyright) {
        this.copyright = copyright;

        this.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow", "center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        this.setBorder(new EmptyBorder(5,5,5,5));
        final JLabel label1 = new JLabel();
        label1.setText(getResourceByKey("panel.label.directory"));
        CellConstraints cc = new CellConstraints();
        this.add(label1, cc.xy(1, 1));
        textField1 = new JTextField();
        textField1.setEditable(false);
        this.add(textField1, cc.xy(3, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label2 = new JLabel();
        label2.setText(getResourceByKey("panel.label.case"));
        this.add(label2, cc.xy(1, 3));
        textField2 = new JTextField();
        this.add(textField2, cc.xy(3, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label3 = new JLabel();
        label3.setText(getResourceByKey("panel.label.published"));
        this.add(label3, cc.xy(1, 5));
        checkBox1 = new JCheckBox();
        checkBox1.setText("");
        this.add(checkBox1, cc.xy(3, 5));
        final JLabel label4 = new JLabel();
        label4.setText(getResourceByKey("panel.label.quantity"));
        this.add(label4, cc.xy(1, 7));
        textField3 = new JTextField();
        textField3.setColumns(5);
        textField3.setEditable(false);
        this.add(textField3, cc.xy(3, 7, CellConstraints.LEFT, CellConstraints.DEFAULT));
        JButton browseButton = new JButton();
        browseButton.setText(getResourceByKey("panel.button.browse"));
        this.add(browseButton, cc.xy(5, 1));

        //add tooltip text
        textField1.setToolTipText(getResourceByKey("panel.tooltip.directory"));
        textField2.setToolTipText(getResourceByKey("panel.tooltip.case"));
        checkBox1.setToolTipText(getResourceByKey("panel.tooltip.published"));
        textField3.setToolTipText(getResourceByKey("panel.tooltip.quantity"));

        //add listeners
        browseButton.addActionListener(e -> browse());
        checkBox1.addActionListener(e -> published());
        textField2.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                copyright.setCaseNumber(textField2.getText());
            }
        });
    }

    private String getResourceByKey(final String key) {
        return copyright.getResourceBundle().getString(key);
    }

    private void browse() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(getResourceByKey("dialog.filechooser.dir"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnVal = chooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            Path p = chooser.getSelectedFile().toPath();
            copyright.setDirectory(p);
            textField1.setText(p.toString());
        }
    }

    public void published() {
        boolean status = checkBox1.isSelected();
        copyright.setPublished(status);
    }

    /**
     * Update the UI based on values in backing object.
     */
    public void update() {
        this.textField1.setText(copyright.getDirectory().toString());
        this.textField2.setText(copyright.getCaseNumber());
        this.checkBox1.setSelected(copyright.isPublished());
        this.updateImageCount();
    }

    public void updateImageCount() {
        textField3.setText("" + copyright.getFileInfos().size());
    }

}
