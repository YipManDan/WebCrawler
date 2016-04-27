import com.sun.deploy.util.SystemUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.nio.file.Files;

/**
 * The FileInterface
 *
 * @author  Daniel Yeh and Jesse Harder
 * @version 1.0
 * @since   2016-04-06
 */
public class FileInterface extends JFrame implements ActionListener, WindowListener {
    //Data
    private final JFileChooser fileChooser = new JFileChooser();
    private final JFileChooser pathChooser = new JFileChooser();
    private final String defaultPath = "repository";
    private Crawler crawler;

    //Textfield to display user's selected filepath
    private JTextField specFile, outputPath;
    private SpinnerNumberModel spinnerNumberModel;
    private JSpinner numberOfSpiderField;
    private int numberOfSpiders = 1;    //Default spider thread count

    // Java Swing buttons
    private JButton openButton, outputPathSelectButton, okButton;

    /**
     * A constructor for the FileInterface class.
     */
    FileInterface(Crawler crawler) {

        super("Select a Specification File");

        this.crawler = crawler;

        // Set restriction for the JFileChoosers
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        pathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        JPanel north, south, numberPanel;

        //North panel contains text field displaying selected file paths and thread count
        north = new JPanel(new GridLayout(3, 1));

        // Filepath specified for specification file
        specFile = new JTextField();
        specFile.setText("Select a file: ");
        specFile.setEditable(false);
        specFile.setBackground(Color.white);
        north.add(specFile);

        // Filepath specified for output of repository
        outputPath = new JTextField("Select an output path: ");
        outputPath.setEditable(false);
        outputPath.setBackground(Color.white);
        north.add(outputPath);


        // Number of threads to be generated
        numberPanel = new JPanel();
        spinnerNumberModel = new SpinnerNumberModel(numberOfSpiders, 1, 500, 1);
        numberOfSpiderField = new JSpinner(spinnerNumberModel);
        numberPanel.add(new JLabel("Select a number of spider threads: "));
        numberPanel.add(numberOfSpiderField);
        north.add(numberPanel);

        //South panel contains select and confirm buttons
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

        // Add north and south panels to JFrame
        add(north, BorderLayout.NORTH);
        add(south, BorderLayout.CENTER);

        this.setPreferredSize(new Dimension(400, 180));
        this.setMinimumSize(new Dimension(400, 180));
        this.setLocationRelativeTo(null);

        this.addWindowListener(this); // Allows class to check when user closes window

        this.setVisible(true);
    }

    /**
     * Class to obtain what type of file separator is used by the operating system
     * @return  A string containing the file separator '/' or '\' in standard systems
     */
    private String fileSep() {
        return System.getProperty("file.separator");
    }

    @Override
    /**
     * ActionListener for the JButtons
     * openButton opens a JFileChooser to determine filepath of specification file
     * outputPathSelectButton opens a JFileChooser to determine filepath to create repository
     * okButton passes the information to the Crawler class
     */
    public void actionPerformed(ActionEvent e) {

        Object o = e.getSource();

        // Button for spec file
        if (o == openButton) {
            int returnVal = fileChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.CANCEL_OPTION)
                return;
            specFile.setText(fileChooser.getSelectedFile().getAbsolutePath());
            revalidate();
            repaint();
            return;
        }

        // Button for repository output folder
        if (o == outputPathSelectButton) {
            int returnVal = pathChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.CANCEL_OPTION)
                return;
            outputPath.setText(pathChooser.getSelectedFile().getAbsolutePath());
            revalidate();
            repaint();
            return;
        }

        // User confirmation button
        if (o == okButton) {
            if (crawler != null) {

                //Sends spec file to crawler
                crawler.csvFile = fileChooser.getSelectedFile();

                // Assigns output path to crawler
                String path;
                if (pathChooser.getSelectedFile() == null) {
                    path = defaultPath;
                } else {
                    path = pathChooser.getSelectedFile().getAbsolutePath() + fileSep() + "repository";
                }

                // Creating or otherwise handling the repository directory.
                File dir = new File(path);
                if (!dir.exists())
                    new File(path).mkdir(); // Create the repository directory.

                crawler.outputPath = path + fileSep();

                // Sets number of spiders threads to create
                numberOfSpiders = spinnerNumberModel.getNumber().intValue();
                System.out.println("Number of spiders: " + numberOfSpiders);
                crawler.numberOfSpiders = numberOfSpiders;


                crawler.startCrawl();
            }
            // Interface is disposed of in the crawler's startCrawl() method.
            return;
        }

    }

    /* Accessors */

    /**
     * Accessor called to get the file selected by the user.
     *
     * @return The file selected by the user.
     */
    public File getFileChosen() {
        return fileChooser.getSelectedFile();
    }

    @Override
    /**
     * Window listener to determine if user closes FileInterface.
     * Method will call the endProgram() method from crawler which will
     * stop execution of the program.
     */
    public void windowClosing(WindowEvent e) {
        crawler.endProgram();
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }
}
