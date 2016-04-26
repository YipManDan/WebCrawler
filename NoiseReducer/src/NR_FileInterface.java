import com.sun.deploy.util.SystemUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created by JHarder on 4/21/16.
 */
public class NR_FileInterface extends JFrame implements ActionListener {
    private final JFileChooser inputPathChooser = new JFileChooser();
    private final JFileChooser outputPathChooser = new JFileChooser();
    private final String defaultInputPath = "repository";
    private final String defaultOutputPath = "noise_reduced_repository";
    private NoiseReducer noiseReducer;

    //Textfield to display user's selected filepath
    private JTextField inputPath, outputPath;

    private JButton openButton, outputPathSelectButton, okButton;

    /**
     * A constructor for the FileInterface class.
     */
    NR_FileInterface(NoiseReducer noiseReducer){
        super("Select a Specification File");

        this.noiseReducer = noiseReducer;

        inputPathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        outputPathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        JPanel north, south;

        //North panel contains text field displaying selected file path
        north = new JPanel(new GridLayout(2,1));

        inputPath = new JTextField();
        inputPath.setText("Select an input path: ");
        inputPath.setEditable(false);
        inputPath.setBackground(Color.white);
        north.add(inputPath);

        outputPath = new JTextField("Select an output path: ");
        outputPath.setEditable(false);
        outputPath.setBackground(Color.white);
        north.add(outputPath);


        //South panel contains select and confirm button
        south = new JPanel();
        openButton = new JButton("Input Path");
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
            int returnVal = inputPathChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.CANCEL_OPTION)
                return;
            inputPath.setText(inputPathChooser.getSelectedFile().getAbsolutePath());
            revalidate();
            repaint();
            return;
        }

        if(o == outputPathSelectButton){
            int returnVal = outputPathChooser.showOpenDialog(this);
            if(returnVal == JFileChooser.CANCEL_OPTION)
                return;
            outputPath.setText(outputPathChooser.getSelectedFile().getAbsolutePath());
            revalidate();
            repaint();
            return;
        }

        if(o == okButton) {
            if (noiseReducer != null)  {
                String inPath, outPath;

                // Set the inPath.
                if(inputPathChooser.getSelectedFile() == null) {
                    inPath = defaultInputPath + fileSep();
                }
                else {
                    inPath = inputPathChooser.getSelectedFile().getAbsolutePath() + fileSep();
                }

                // Set the outPath.
                if(outputPathChooser.getSelectedFile() == null) {
                    outPath = defaultOutputPath;
                }
                else {
                    outPath = outputPathChooser.getSelectedFile().getAbsolutePath() + fileSep();
                }

                // Creating or otherwise handling the repository directory.
                File dir = new File(inPath);
                if (!dir.exists()) {
                    System.err.println("Input repository could not be found.");
                    System.err.println("Could not find "+inPath);
                    // Popup something to tell user what went wrong.
                    System.exit(1); // End execution.
                }

                noiseReducer.noiseReduce(inPath, outPath);
            }
            // Interface is disposed of in the noiseReducer's startCrawl() method.
            return;
        }
    }
}