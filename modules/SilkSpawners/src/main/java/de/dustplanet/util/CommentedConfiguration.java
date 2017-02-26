package de.dustplanet.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Custom configuration with the ability to add comments.
 *
 * @author dumptruckman
 * @author ElgarL (updated version used by Towny)
 * @author xGhOsTkiLLeRx
 */
public class CommentedConfiguration extends YamlConfiguration {
    private HashMap<String, String> comments;
    private File file;
    private final DumperOptions yamlOptions = new DumperOptions();
    private final Representer yamlRepresenter = new YamlRepresenter();
    private final Yaml yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);

    public CommentedConfiguration(File file) {
        super();
        comments = new HashMap<>();
        this.file = file;
    }

    @Override
    public String saveToString() {
        yamlOptions.setIndent(options().indent());
        yamlOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.LITERAL);
        yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        String header = buildHeader();
        String dump = yaml.dump(getValues(false));

        if (dump.equals(BLANK_CONFIG)) {
            dump = "";
        }

        return header + dump;
    }

    public boolean load() {
        boolean loaded = true;
        try {
            this.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            loaded = false;
            System.out.println("Exception while loading the file: " + e.getMessage());
            e.printStackTrace();
        }
        return loaded;
    }

    public boolean save() {
        boolean saved = true;

        // Save the config just like normal
        try {
            super.save(file);
        } catch (IOException e) {
            saved = false;
            System.out.println("Failed to save the file: " + e.getMessage());
            e.printStackTrace();
        }

        // if there's comments to add and it saved fine, we need to add comments
        if (!comments.isEmpty() && saved) {
            // String array of each line in the config file
            String[] yamlContents = convertFileToString(file).split("[" + System.getProperty("line.separator") + "]");

            // This will hold the newly formatted line
            String newContents = "";
            // This holds the current path the lines are at in the config
            String currentPath = "";
            // This tells if the specified path has already been commented
            boolean commentedPath = false;
            // This flags if the line is a node or unknown text.
            boolean node;
            // The depth of the path. (number of words separated by periods - 1)
            int depth = 0;

            // Loop through the config lines
            for (String line : yamlContents) {
                // If the line is a node (and not something like a list value)
                if (line.contains(": ") || line.length() > 1 && line.charAt(line.length() - 1) == ':') {

                    // This is a new node so we need to mark it for commenting
                    // (if there are comments)
                    commentedPath = false;
                    // This is a node so flag it as one
                    node = true;

                    // Grab the index of the end of the node name
                    int index = line.indexOf(": ");
                    if (index < 0) {
                        index = line.length() - 1;
                    }
                    // If currentPath is empty, store the node name as the
                    // currentPath. (this is only on the first iteration, i
                    // think)
                    if (currentPath.isEmpty()) {
                        currentPath = line.substring(0, index);
                    } else {
                        // Calculate the whitespace preceding the node name
                        int whiteSpace = 0;
                        for (int n = 0; n < line.length(); n++) {
                            if (line.charAt(n) == ' ') {
                                whiteSpace++;
                            } else {
                                break;
                            }
                        }
                        // Find out if the current depth (whitespace * 2) is
                        // greater/lesser/equal to the previous depth
                        if (whiteSpace / 2 > depth) {
                            // Path is deeper. Add a . and the node name
                            currentPath += "." + line.substring(whiteSpace, index);
                            depth++;
                        } else if (whiteSpace / 2 < depth) {
                            // Path is shallower, calculate current depth from
                            // whitespace (whitespace / 2) and subtract that
                            // many levels from the currentPath
                            int newDepth = whiteSpace / 2;
                            for (int i = 0; i < depth - newDepth; i++) {
                                currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf('.')), "");
                            }
                            // Grab the index of the final period
                            int lastIndex = currentPath.lastIndexOf('.');
                            if (lastIndex < 0) {
                                // if there isn't a final period, set the
                                // current path to nothing because we're at root
                                currentPath = "";
                            } else {
                                // If there is a final period, replace
                                // everything after it with nothing
                                currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf('.')), "");
                                currentPath += ".";
                            }
                            // Add the new node name to the path
                            currentPath += line.substring(whiteSpace, index);
                            // Reset the depth
                            depth = newDepth;
                        } else {
                            // Path is same depth, replace the last path node
                            // name to the current node name
                            int lastIndex = currentPath.lastIndexOf('.');
                            if (lastIndex < 0) {
                                // if there isn't a final period, set the
                                // current path to nothing because we're at root
                                currentPath = "";
                            } else {
                                // If there is a final period, replace
                                // everything after it with nothing
                                currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf('.')), "");
                                currentPath += ".";
                            }
                            // currentPath =
                            // currentPath.replace(currentPath.substring(currentPath.lastIndexOf(".")),
                            // "");
                            currentPath += line.substring(whiteSpace, index);

                        }
                    }

                } else {
                    node = false;
                }
                if (node) {
                    String comment = null;
                    if (!commentedPath) {
                        // If there's a comment for the current path, retrieve
                        // it and flag that path as already commented
                        comment = comments.get(currentPath);
                    }
                    if (comment != null) {
                        // Add the comment to the beginning of the current line
                        line = comment + System.getProperty("line.separator") + line + System.getProperty("line.separator");
                        commentedPath = true;
                    } else {
                        // Add a new line as it is a node, but has no comment
                        line += System.getProperty("line.separator");
                    }
                }
                // Add the (modified) line to the total config String
                newContents += line + (!node ? System.getProperty("line.separator") : "");

            }
            /*
             * Due to a bukkit bug we need to strip any extra new lines from the
             * beginning of this file, else they will multiply.
             */
            while (newContents.startsWith(System.getProperty("line.separator"))) {
                newContents = newContents.replaceFirst(System.getProperty("line.separator"), "");
            }

            // Write the string to the config file
            if (!stringToFile(newContents, file)) {
                saved = false;
            }
        }
        return saved;
    }

    /**
     * Adds a comment just before the specified path. The comment can be
     * multiple lines. An empty string will indicate a blank line.
     *
     * @param path - Configuration path to add comment.
     * @param commentLines - Comments to add. One String per line.
     */
    public void addComment(String path, String... commentLines) {
        StringBuilder commentstring = new StringBuilder();
        String leadingSpaces = "";
        for (int n = 0; n < path.length(); n++) {
            if (path.charAt(n) == '.') {
                leadingSpaces += "  ";
            }
        }
        for (String line : commentLines) {
            if (!line.isEmpty()) {
                line = leadingSpaces + line;
            } else {
                line = " ";
            }
            if (commentstring.length() > 0) {
                commentstring.append("\r\n");
            }
            commentstring.append(line);
        }
        comments.put(path, commentstring.toString());
    }

    /**
     * Pass a file and it will return it's contents as a string.
     *
     * @param file - File to read.
     * @return Contents of file. String will be empty in case of any errors.
     */
    public String convertFileToString(File file) {
        if (file != null && file.exists() && file.canRead() && !file.isDirectory()) {
            char[] buffer = new char[1024];
            String s = "";
            try (Writer writer = new StringWriter();
                    Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));) {
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
                s = writer.toString();
            } catch (IOException e) {
                System.out.println("Failed to convert the file to a string: " + e.getMessage());
                e.printStackTrace();
            }
            return s;
        }
        return "";
    }

    /**
     * Writes the contents of a string to a file.
     *
     * @param source - String to write.
     * @param file - File to write to.
     * @return True on success.
     */
    public boolean stringToFile(String source, File file) {
        try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
            out.write(source);
            return true;
        } catch (IOException e) {
            System.out.println("Failed to convert the string to a file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
