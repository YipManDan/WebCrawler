import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by JHarder on 4/24/16.
 */
public final class Lexer {
    protected List<LexTuple> lexTuples;
    private String currentToken;
    private int counter;

    public Lexer() {
        lexTuples = null;
    }

    /**
     * Called to produce a bitstream from the target file representing tags.
     * @param targetFile - The file to be parsed.
     * @return A List of
     * @throws FileNotFoundException
     */
    public List<LexTuple> lexFile(File targetFile) throws IOException {
        Scanner scanner = new Scanner(targetFile);
        lexTuples = new ArrayList<LexTuple>();
        currentToken = "";
        counter = 0;

        FileInputStream inputStream = new FileInputStream(targetFile);
        char currentChar = 's', prevChar;
        boolean readingTag = false;

        while (inputStream.available() > 0) {
            prevChar = currentChar;
            currentChar = (char) inputStream.read();

            if (Character.isWhitespace(currentChar)) {
                if (!readingTag && currentToken != "") {
                    addToken();
                    counter++;
                }
            }
            else {
                // Start a tag
                if (currentChar == '<' && prevChar != '\\') {
                    if (currentToken != "") {
                        addToken();
                        counter++;
                    }
                    readingTag = true;
                }
                // End a tag.
                else if (readingTag && currentChar == '>' && prevChar !='\\') {
                    readingTag = false;
                    currentToken += currentChar;
                    addToken();
                    continue;
                }
//                System.out.println(""+currentChar+" - "+currentToken+" - "+readingTag);
                currentToken += currentChar;
            }

        }

        inputStream.close();

        // Used to output all of the tokens of the file for debugging.
        for (LexTuple tuple : lexTuples) {
            System.out.println(tuple.getToken() + " - " + tuple.getPosition() +  " - " + tuple.getBit());
        }

        return lexTuples;
    }

    private void addToken() {
        // Before adding, remove punctuation.
        currentToken = currentToken.replace(",","");
        currentToken = currentToken.replace(".","");
//        currentToken = currentToken.replace("\"","");

        lexTuples.add(new LexTuple(currentToken,counter,1));
        currentToken = "";
        counter++;
    }

    /**
     * Used to see if a given currentToken is a style opening tag.
     * @param token
     * @return Boolean value.
     */
    static private boolean isStyleStartTag(String token) {
        return token.startsWith("<style ");
    }

    /**
     * Used to see if a given currentToken is a style end tag.
     * @param token
     * @return Boolean value.
     */
    static private boolean isStyleEndTag(String token) {
        return token.equals("</style>");
    }
}
