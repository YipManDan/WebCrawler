import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

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
    private Crawler crawler;

    //Textfield to display user's selected filepath
    private JTextField textField;

    private JButton openButton, okButton;

    /**
     * A constructor for the FileInterface class.
     */
    FileInterface(Crawler crawler){
        super("Select a Specification File");

        this.crawler = crawler;

        JPanel north, south;

        //North panel contains text field displaying selected file path
        north = new JPanel(new GridLayout(1,1));
        textField = new JTextField();
        textField.setText("Select a file: ");
        textField.setEditable(false);
        textField.setBackground(Color.white);
        north .add(textField);

        //South panel contains select and confirm button
        south = new JPanel();
        openButton = new JButton("Select a File");
        openButton.addActionListener(this);
        okButton = new JButton("OK");
        okButton.addActionListener(this);
        south.add(openButton);
        south.add(okButton);

        add(north, BorderLayout.NORTH);
        add(south, BorderLayout.CENTER);

        this.setPreferredSize(new Dimension(400, 140));
        this.setMinimumSize(new Dimension(400, 140));
        this.setLocationRelativeTo(null);

        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if(o == openButton) {
            int returnVal = fileChooser.showOpenDialog(this);
            if(returnVal == JFileChooser.CANCEL_OPTION)
                return;
            textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            return;
        }
        if(o == okButton) {
            if (crawler != null)  {
                crawler.csvFile = fileChooser.getSelectedFile();
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

//    /**
//     * Called to find out how many spiders the user wishes to use in the crawl.
//     * @return An integer for the number of spiders to use in the crawl.
//     */
//    public int getNumberOfSpiders() {
//        return numberOfSpiders;
//    }
//
//    /**
//     * Called to find out how many queues the user wishes to use for the crawl.
//     * @return An integer for the number of queues to be used.
//     */
//    public int getNumberOfQueues() {
//        return numberOfQueues;
//    }

    public static void main(String[] args){
        new FileInterface(null);
    }

}
