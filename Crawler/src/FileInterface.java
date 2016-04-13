import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Daniel on 4/6/2016.
 */
public class FileInterface extends JFrame implements ActionListener{

    private final JFileChooser fc = new JFileChooser();

    //Textfield to display user's selected filepath
    private JTextField tf;

    private JButton open, ok;

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

    public static void main(String[] args){
        new FileInterface();
    }

}
