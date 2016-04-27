import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Daniel on 4/27/2016.
 */
public class CustomOutputStream extends OutputStream{
    private JTextArea textArea;

    public CustomOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(int b) throws IOException {
        textArea.append(String.valueOf((char)b));
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}
