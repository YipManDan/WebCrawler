import com.sun.deploy.util.SystemUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;

/**
 * The FileInterface
 *
 * @author  Daniel Yeh and Jesse Harder
 * @version 1.0
 * @since   2016-04-06
 */
public class FileInterface extends JFrame implements ActionListener{
    //Data
    private final JFileChooser fileChooser = new JFileChooser();
    private final JFileChooser pathChooser = new JFileChooser();
    private final String defaultPath = "repository";
    private Crawler crawler;

    //Textfield to display user's selected filepath
    private JTextField specFile, outputPath;

    private JButton openButton, outputPathSelectButton, okButton;

    /**
     * A constructor for the FileInterface class.
     */
    FileInterface(Crawler crawler){
        super("Select a Specification File");

        this.crawler = crawler;

        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        pathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        JPanel north, south;

        //North panel contains text field displaying selected file path
        north = new JPanel(new GridLayout(2,1));

        specFile = new JTextField();
        specFile.setText("Select a file: ");
        specFile.setEditable(false);
        specFile.setBackground(Color.white);
        north.add(specFile);

        outputPath = new JTextField("Select an output path: ");
        outputPath.setEditable(false);
        outputPath.setBackground(Color.white);
        north.add(outputPath);


        //South panel contains select and confirm button
        south = new JPanel();
        openButton = new JButton("Specification File");
        openButton.addActionListener(this);
        outputPathSelectButton = new JButton("Output Path");
        outputPathSelectButton.addActionListener(this);
        okButton = new JButton("OK");
        okButton.addActionListener(this);
        south.add(openButton);
        south.add(outputPathSelectButton);
        south.add(okButton);

        add(north, BorderLayout.NORTH);
        add(south, BorderLayout.CENTER);

        this.setPreferredSize(new Dimension(400, 140));
        this.setMinimumSize(new Dimension(400, 140));
        this.setLocationRelativeTo(null);

        this.setVisible(true);
    }

    private String fileSep() {
        return System.getProperty("file.separator");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if(o == openButton) {
            int returnVal = fileChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.CANCEL_OPTION)
                return;
            specFile.setText(fileChooser.getSelectedFile().getAbsolutePath());
            revalidate();
            repaint();
            return;
        }
        if(o == outputPathSelectButton){
            int returnVal = pathChooser.showOpenDialog(this);
            if(returnVal == JFileChooser.CANCEL_OPTION)
                return;
            outputPath.setText(pathChooser.getSelectedFile().getAbsolutePath());
            revalidate();
            repaint();
            return;
        }
        if(o == okButton) {
            if (crawler != null)  {
                crawler.csvFile = fileChooser.getSelectedFile();
                String path;
                if(pathChooser.getSelectedFile() == null) {
                    path = defaultPath;
                }
                else {
                    path = pathChooser.getSelectedFile().getAbsolutePath() + fileSep() + "repository";
                }

                // Creating or otherwise handling the repository directory.
                File dir = new File(path);
                if (!dir.exists())
                    new File(path).mkdir(); // Create the repository directory.

                crawler.outputPath = path + fileSep();
                crawler.startCrawl();
            }
            // Interface is disposed of in the crawler's startCrawl() method.
            return;
        }

    }

    /* Accessors */

    /**
     * Accessor called to get the file selected by the user.
     * @return The file selected by the user.
     */
    public File getFileChosen() {
        return fileChooser.getSelectedFile();
    }

}
