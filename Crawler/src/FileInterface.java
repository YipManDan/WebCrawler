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
    private final JFileChooser fc = new JFileChooser();
//    int numberOfSpiders;
//    int numberOfQueues;

    //Textfield to display user's selected filepath
    private JTextField tf;

    private JButton open, ok;

    /**
     * A constructor for the FileInterface class.
     */
    FileInterface(){

        super("Select a Specification File");

        JPanel north, south;

        //North panel contains text field displaying selected file path
        north = new JPanel(new GridLayout(1,1));
        tf = new JTextField();
        tf.setText("Select a file: ");
        tf.setEditable(false);
        tf.setBackground(Color.white);
        north .add(tf);

        //South panel contains select and confirm button
        south = new JPanel();
        open = new JButton("Select a File");
        open.addActionListener(this);
        ok = new JButton("OK");
        ok.addActionListener(this);
        south.add(open);
        south.add(ok);

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
        if(o == open) {
            int returnVal = fc.showOpenDialog(this);
            if(returnVal == JFileChooser.CANCEL_OPTION)
                return;
            tf.setText(fc.getSelectedFile().getAbsolutePath());
            return;
        }
        if(o == ok) {
            //();
            this.dispose();
            return;
        }

    }

    /* Accessors */

    /**
     * Accessor called to get the file selected by the user.
     * @return The file selected by the user.
     */
    public File getFileChosen() {
        return fc.getSelectedFile();
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
        new FileInterface();
    }

}
