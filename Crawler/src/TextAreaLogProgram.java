import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.PrintStream;

/**
 * Created by Daniel on 4/27/2016.
 */
public class TextAreaLogProgram extends JFrame implements Runnable, WindowListener{

    private Crawler crawler;

    private JTextArea textArea;

    private PrintStream standardOut;
    private PrintStream printStream;

    protected Thread thread;

    public TextAreaLogProgram(Crawler crawler) {

        this.crawler = crawler;

        thread = new Thread(this);

        textArea = new JTextArea(50,100);
        textArea.setEditable(false);

        printStream = new PrintStream(new CustomOutputStream(textArea));

        standardOut = System.out;

        System.setOut(printStream);
        System.setErr(printStream);

//        setLayout(new GridBagLayout());
//        GridBagConstraints constraints = new GridBagConstraints();
//        constraints.gridx = 0;
//        constraints.gridy = 0;
//        constraints.insets = new Insets(10, 10, 10, 10);
//        constraints.anchor = GridBagConstraints.WEST;

        add(new JScrollPane(textArea));

        setSize(480, 320);
        setLocationRelativeTo(null);

        this.setVisible(true);

        this.setVisible(true);

        System.out.println("Test");
        System.err.println("Test2");
    }
    @Override
    public void run(){

        while(true){
            repaint();

        }

    }

    public PrintStream getPrintStream() {
        return printStream;
    }

    public PrintStream getStandardOut() {
        return standardOut;
    }

    @Override
    /**
     * Window listener to determine if user closes TextAreaLogProgram.
     * Method will call the endProgram() method from crawler which will
     * stop execution of the program.
     */
    public void windowClosing(WindowEvent e) {
        System.setOut(standardOut);
        System.out.println("Closing the Text Area");
        try {
            thread.join();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
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
