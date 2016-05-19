import jdk.nashorn.internal.runtime.regexp.joni.Regex;

import java.io.*;
import java.util.*;

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
        List<String> disallowedTags = new ArrayList<String>();

        loadDisallowedTags(disallowedTags);

        Scanner scanner = new Scanner(targetFile);
        String token = "";

        FileInputStream inputStream = new FileInputStream(targetFile);
        boolean wantWhiteSpaceTokens = true;
        boolean doCleaning = false;

        char currentChar = 's', prevChar;
        int counter = 0;
        boolean readingTag = false;

        while (inputStream.available() > 0) {
            prevChar = currentChar;
            currentChar = (char) inputStream.read();

            if (Character.isWhitespace(currentChar)) {
                if (!readingTag) {
                    if (!token.equals("")) {
                        if (doCleaning) {
                            token = token.replaceAll("[^a-zA-z0-9]", "");
                            token = token.replace("[", "");
                            token = token.replace("]","");
                            token = token.replace("^", "");
                        }
                        if (!token.equals("")) {
                            lexTuples.add(new LexTuple(token,counter,0));
                            token = "";
                        }
                        counter++;
                    }

                    // Add white space tokens.
                    if (wantWhiteSpaceTokens)
                        lexTuples.add(new LexTuple(""+currentChar,counter++,0));
                }
            }
            else {
                // Start a tag
                if (currentChar == '<' && prevChar != '\\') {
                    if (token.equals("")) {
                        if (doCleaning) {
                            token = token.replaceAll("[^a-zA-z0-9]", "");
                            token = token.replace("[", "");
                            token = token.replace("]","");
                            token = token.replace("^", "");
                        }
                        if (token.equals("")) {
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

                    // Handle disallowed tags logic here.

                    lexTuples.add(new LexTuple(token,counter,1));
                    token = "";
                    counter++;
                    continue;
                }
                token += currentChar;
            }

        }

        inputStream.close();

        return lexTuples;
    }

    static private void loadDisallowedTags(List<String> list) {
        BufferedReader reader;
        String disallowedTag;

        if (list == null) list = new ArrayList<String>();
        try {
            reader = new BufferedReader(new FileReader("Disallowed_Tags.txt"));
            while ((disallowedTag = reader.readLine()) != null) {
                list.add(disallowedTag);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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