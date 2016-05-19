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
        Map<String, Integer> disallowedTagsSeen = new HashMap<String, Integer>();

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

            // Logic for encountering a white space character.
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
                            if (disallowedTagsSeen.isEmpty()) lexTuples.add(new LexTuple(token,counter,0));
                        }
                        if (disallowedTagsSeen.isEmpty()) counter++;
                        token = "";
                    }

                    // Add white space tokens.
                    if (wantWhiteSpaceTokens && disallowedTagsSeen.isEmpty())
                        lexTuples.add(new LexTuple(""+currentChar,counter++,0));
                }
            }
            // Logic for encountering the start of a tag.
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
                            if (disallowedTagsSeen.isEmpty()) lexTuples.add(new LexTuple(token,counter,0));
                            token = "";
                        }
                        if (disallowedTagsSeen.isEmpty()) counter++;
                    }
                    readingTag = true;
                }
                // Logic for encountering the end of a tag.
                else if (readingTag && currentChar == '>' && prevChar !='\\') {
                    boolean wasEmpty = disallowedTagsSeen.isEmpty();
                    readingTag = false;
                    token += currentChar;

                    // --- The following if-else handles disallowed tags. ---

                    // Logic for encountering a closing tag.
                    if (token.startsWith("</")){
                        String stripped = token.substring(2);
                        // Check to see if it is in the disallowed list.
                        for (String seenDisallowed : disallowedTagsSeen.keySet()) {
                            if (stripped.startsWith(seenDisallowed)) {
                                System.out.println(token);

                                // Reduce the tag's value by 1 unless it is 1, in which case remove.
                                Integer cnt = disallowedTagsSeen.get(seenDisallowed);
                                if (cnt == 1)
                                    disallowedTagsSeen.remove(seenDisallowed);
                                else
                                    disallowedTagsSeen.put(seenDisallowed, cnt - 1);
                                break;
                            }
                        }
                    }
                    // Logic for encountering an opening tag.
                    else
                    {
                        String stripped = token.substring(1);
                        boolean match = false;
                        for (String disallowed : disallowedTags) {
                            if (stripped.startsWith(disallowed)) {
                                match = true;
                                System.out.println(token);

                                // Increase the tag's value by 1 if there, else add it with value 1.
                                if (disallowedTagsSeen.keySet().contains(disallowed)) {
                                    Integer cnt = disallowedTagsSeen.get(disallowed);
                                    disallowedTagsSeen.put(disallowed, cnt + 1);
                                }
                                else
                                {
                                    disallowedTagsSeen.put(disallowed, 1);
                                }
                            }
                        }
                    }

                    // Add the tag if you're not inside a disallowed tag.
                    if (wasEmpty && disallowedTagsSeen.isEmpty()){
                        lexTuples.add(new LexTuple(token,counter,1));
                        counter++;
                    }

                    token = "";
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