import jdk.nashorn.internal.runtime.regexp.joni.Regex;

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
    private Lexer() {}

    /**
     * Called to produce a bitstream from the target file representing tags.
     * @param targetFile - The file to be parsed.
     * @return A List of
     * @throws FileNotFoundException
     */
    static public List<LexTuple> lexFile(File targetFile) throws IOException {
        List<LexTuple> lexTuples = new ArrayList<LexTuple>();

        Scanner scanner = new Scanner(targetFile);
        String token = "";

        FileInputStream inputStream = new FileInputStream(targetFile);
        char currentChar = 's', prevChar;
        boolean readingTag = false;
        int counter = 0;

        while (inputStream.available() > 0) {
            prevChar = currentChar;
            currentChar = (char) inputStream.read();

            if (Character.isWhitespace(currentChar)) {
                if (!readingTag && token != "") {
//                    System.out.println(token);
                    token = token.replaceAll("[^a-zA-z0-9]","");
                    if (token != "") {
                        lexTuples.add(new LexTuple(token,counter,0));
                        token = "";
                    }
                    counter++;
                }
            }
            else {
                // Start a tag
                if (currentChar == '<' && prevChar != '\\') {
                    if (token != "") {
//                        System.out.println(token);
                        token = token.replaceAll("[^a-zA-z0-9]","");
                        if (token != "") {
                            lexTuples.add(new LexTuple(token,counter,0));
                            token = "";
                        }
                        counter++;
                    }
                    readingTag = true;
                }
                // End a tag.
                else if (readingTag && currentChar == '>' && prevChar !='\\') {
                    readingTag = false;
                    token += currentChar;
//                    System.out.println(token);
                    lexTuples.add(new LexTuple(token,counter,1));
                    token = "";
                    counter++;
                    continue;
                }
//                System.out.println(""+currentChar+" - "+token+" - "+readingTag);
                token += currentChar;
            }

        }

        inputStream.close();

        // Used to output all of the tokens of the file for debugging.
//        for (LexTuple tuple : lexTuples) {
//            System.out.println(tuple.getToken() + " - " + tuple.getPosition() +  " - " + tuple.getBit());
//        }

        return lexTuples;
    }

    /**
     * Used to see if a given token is a style opening tag.
     * @param token
     * @return Boolean value.
     */
    static private boolean isStyleStartTag(String token) {
        return token.startsWith("<style ");
    }

    /**
     * Used to see if a given token is a style end tag.
     * @param token
     * @return Boolean value.
     */
    static private boolean isStyleEndTag(String token) {
        return token.equals("</style>");
    }
}