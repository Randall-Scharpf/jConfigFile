package com.randallscharpf.java.jconfigfile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Interactive CLI to allow a user to set up a configuration file in a standard location.
 * 
 * Allows the user to select an existing configuration file from an unsupported location
 * and copy it to a supported location or to create a new, blank configuration file in a
 * selected location. The API exposes methods to synchronously or asynchronously
 * request the result of the interactive operation, which is a {@link ConfigFile}.
 * The prompt can be constructed on any set of I/O streams.
 */
public class ConfigInitializerPrompt {

    private static final Map<String, ConfigLocation> CONFIG_LOCATIONS = new HashMap<>();
    private static final String SELECT_CONFIG_LOCATION_PROMPT;

    static {
        for (ConfigLocation loc : ConfigLocation.values()) {
            CONFIG_LOCATIONS.put(loc.toString(), loc);
        }
        SELECT_CONFIG_LOCATION_PROMPT = generateConfigLocationPrompt();
    }
    
    private static String generateConfigLocationPrompt() {
        String result = "Select a location to place this application's config file. Valid options are ";
        int i = 0;
        for (String opt : CONFIG_LOCATIONS.keySet()) {
            if (i == CONFIG_LOCATIONS.size() - 1) {
                result += "and " + opt;
            } else {
                result += opt + ", ";
            }
            i++;
        }
        return result;
    }

    private final ConfigFinder finder;
    private final InputStream userInput;
    private final PrintStream userOutput;
    private final PrintStream userErrors;

    private final Object stateKey;
    private boolean active;

    /**
     * Creates a CLI on the given streams for the application described by the given
     * {@link ConfigFinder}.
     * 
     * The CLI will not prompt the user or read input until triggered by a call to
     * either {@code getInitializedFile} or {@code getInitializedFileAsync}. The
     * output stream and error stream may be the same stream or may be different
     * streams.
     * 
     * @param finder {@link ConfigFinder} for the application which needs its configuration initialized
     * @param userInput character stream from which user inputs and selections come
     * @param userOutput character stream to which prompts should be written
     * @param userErrors character stream to which input parsing issues should be written
     */
    public ConfigInitializerPrompt(ConfigFinder finder, InputStream userInput, PrintStream userOutput, PrintStream userErrors) {
        this.finder = finder;
        this.userInput = userInput;
        this.userOutput = userOutput;
        this.userErrors = userErrors;

        this.stateKey = new Object();
        this.active = false;
    }

    /**
     * Begins CLI interactions and blocks until the user has handled all prompts,
     * then returns the configuration produced by the user's selection.
     * 
     * User code must not call this method from multiple threads at once, or call this
     * method while its asynchronous version is still awaiting user input.
     * An {@code IllegalStateException} will be thrown if user code does not adhere
     * to this requirement.
     * 
     * When the user selects and confirms the desired qualities of the configuration file
     * to initialize, a {@link ConfigFile} will be created corresponding to the selected
     * characteristics. If the attempt is successful, the configuration file will be
     * returned, otherwise an exception is thrown. If the user cancels the operation,
     * {@code null} will be returned.
     * 
     * @return an initialized configuration file located according to user input
     * @throws IOException if initializing a configuration file as the user requests is impossible
     * @throws IllegalStateException if the CLI is already being used to select a file
     */
    public ConfigFile getInitializedFile() throws IOException {
        synchronized (stateKey) {
            if (active) {
                throw new IllegalStateException("This CLI is already being used to initialize a config file!");
            }
            active = true;
        }
        ConfigFile result = getInitializedFileUnguarded();
        return result;
    }

    /**
     * Begins CLI interactions and returns immediately, then later calls a provided callback
     * with the configuration produced by the user's selection.
     * 
     * Once this method is called, user code must wait until the callback runs before
     * calling it again or calling the synchronous version of this method.
     * An {@code IllegalStateException} will be thrown if user code does not adhere
     * to this requirement.
     * 
     * When the user selects and confirms the desired qualities of the configuration file
     * to initialize, the program will attempt to create a {@link ConfigFile}
     * corresponding to the selected characteristics.
     * <ul>
     * <li>
     *   If the attempt is successful, {@code callback} will be executed with the
     *   first parameter set to the created file and the second parameter set to {@code null}.
     * </li>
     * <li>
     *   If the attempt is unsuccessful, {@code callback} will be executed with the
     *   first parameter set to {@code null} and the second parameter set to an
     *   appropriate exception describing the failure.
     * </li>
     * <li>
     *   If the user cancels the operation, {@code callback} will be executed with
     *   both parameters set to {@code null}.
     * </li>
     * </ul>
     * 
     * @param callback to be executed when the user interaction is complete
     * @throws IllegalStateException if the CLI is already being used to select a file
     */
    public void getInitializedFileAsync(BiConsumer<ConfigFile, IOException> callback) {
        synchronized (stateKey) {
            if (active) {
                throw new IllegalStateException("This CLI is already being used to initialize a config file!");
            }
            active = true;
        }
        new Thread(() -> {
            try {
                ConfigFile result = getInitializedFileUnguarded();
                active = false;
                callback.accept(result, null);
            } catch (IOException ex) {
                active = false;
                callback.accept(null, ex);
            }
        }).start();
    }
    
    private ConfigFile getInitializedFileUnguarded() throws IOException {
        String location = prompt(SELECT_CONFIG_LOCATION_PROMPT, CONFIG_LOCATIONS::containsKey);
        if (location == null) return null;
        File newfile = finder.configAt(CONFIG_LOCATIONS.get(location));
        boolean submenuCancelled;
        do {
            submenuCancelled = false;
            String copy = prompt(
                    "Is there an existing file to use to import configuration settings? Valid options are YES and NO",
                    (str) -> {
                        return "YES".equals(str) || "NO".equals(str);
                    }
            );
            if (copy == null) return null;
            if ("YES".equals(copy)) {
                String oldfile = prompt("  ", "Enter the file path to copy",
                        (path) -> {
                            File f = new File(path);
                            return f.exists() && f.isFile();
                        }
                );
                if (oldfile == null) {
                    submenuCancelled = true;
                } else {
                    try {
                        newfile.getParentFile().mkdirs();
                        Files.copy(new File(oldfile).toPath(), newfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (RuntimeException ex) {
                        // java.nio.file.Files throws unchecked exceptions for certain errors
                        throw new IOException(ex);
                    }
                }
            } else {
                try {
                    Files.deleteIfExists(newfile.toPath());
                } catch (RuntimeException ex) {
                    // java.nio.file.Files throws unchecked exceptions for certain errors
                    throw new IOException(ex);
                }
            }
        } while (submenuCancelled);
        return new ConfigFile(newfile);
    }
    
    private String prompt(String query, Predicate<String> validator) throws IOException {
        return prompt("", query, validator);
    }

    private String prompt(String prefix, String query, Predicate<String> validator) throws IOException {
        String resp = null;
        do {
            if (resp != null) {
                userErrors.println(prefix + "Input \"" + resp + "\" was not accepted");
                userErrors.flush();
            }
            userOutput.println(prefix + query + " (or type 'cancel')");
            userOutput.print(prefix);
            userOutput.flush();
            resp = "";
            int next = userInput.read();
            boolean withheldR = false;
            while (next != '\n') {
                if (withheldR) {
                    resp += '\r';
                    withheldR = false;
                }
                if (next == '\r') {
                    withheldR = true;
                } else {
                    if (next != -1) {
                        resp += (char) next;
                    }
                }
                next = userInput.read();
            }
            if ("cancel".equals(resp)) {
                return null;
            }
        } while (!validator.test(resp));
        return resp;
    }
}
