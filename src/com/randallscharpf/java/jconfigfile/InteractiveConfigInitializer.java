package com.randallscharpf.java.jconfigfile;

import java.io.File;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.JOptionPane;

/**
 * Class containing static utility methods to interactively initialize a configuration
 * for a program. WARNING: This class should not be used in automated scripts where no
 * user is present to select and confirm settings.
 */
public class InteractiveConfigInitializer {

    /**
     * Synchronously find a config file or create one if none exists.
     * 
     * If a config file corresponding to the program-identifying parameters passed
     * already exists, it is returned with no user interaction. Otherwise, the method
     * will block until user input confirms the settings with which the config file
     * is to be initialized. This method returns {@code null} if the user cancels
     * the prompts with which they are provided.
     * 
     * In environments supporting graphical interfaces, a {@link ConfigInitializerDialog}
     * will be displayed and used to interact with the user. In headless environments,
     * a {@link ConfigInitializerPrompt} will interact with the user through {@code System.in}
     * and {@code System.out}.
     * 
     * The arguments to this method are passed to {@link ConfigFinder#ConfigFinder}.
     * 
     * @param callee some class inside the application whose configuration will be located
     * @param configName the name of the application whose configuration will be located
     * @return a new or preexisting configuration file for the described application
     * @throws IOException if the configuration file which was found or selected cannot be opened
     */
    public static ConfigFile findOrCreateConfig(Class<?> callee, String configName) throws IOException {
        ConfigFinder finder = new ConfigFinder(callee, configName);
        File file = finder.searchForConfig();
        if (!file.exists()) {
            if (java.awt.GraphicsEnvironment.isHeadless()) {
                ConfigInitializerPrompt p = new ConfigInitializerPrompt(finder, System.in, System.out, System.err);
                return p.getInitializedFile();
            } else {
                ConfigInitializerDialog d = new ConfigInitializerDialog(finder);
                return d.getInitializedFile();
            }
        } else {
            return new ConfigFile(file);
        }
    }

    /**
     * Asynchronously find a config file or create one if none exists.
     * 
     * If a config file corresponding to the program-identifying parameters passed
     * already exists, it is passed to the callback immediately with no user interaction.
     * Otherwise, the method will return and later (once user input confirms the settings
     * with which the config file is to be initialized) call the provided callback.
     * 
     * <ul>
     * <li>
     *   If the configuration file which was found or selected cannot be opened,
     *   the first parameter to the callback will be {@code null} and the second
     *   will provide the exception that occurred.
     * </li>
     * <li>
     *   If the configuration file which was found or selected has been successfully
     *   opened, it will be passed to the callback as the first parameter and the
     *   second parameter will be {@code null}.
     * </li>
     * <li>
     *   If the user cancels the prompts this method presents, the callback will
     *   be called with both parameters set to {@code null}.
     * </li>
     * </ul>
     * 
     * In environments supporting graphical interfaces, a {@link ConfigInitializerDialog}
     * will be displayed and used to interact with the user. In headless environments,
     * a {@link ConfigInitializerPrompt} will interact with the user through {@code System.in}
     * and {@code System.out}.
     * 
     * The first two arguments to this method are passed to {@link ConfigFinder#ConfigFinder}.
     * 
     * @param callee some class inside the application whose configuration will be located
     * @param configName the name of the application whose configuration will be located
     * @param callback to be executed when the user interaction is complete
     */
    public static void findOrCreateConfigAsync(Class<?> callee, String configName, BiConsumer<ConfigFile, IOException> callback) {
        ConfigFinder finder = new ConfigFinder(callee, configName);
        File file = finder.searchForConfig();
        if (!file.exists()) {
            if (java.awt.GraphicsEnvironment.isHeadless()) {
                ConfigInitializerPrompt p = new ConfigInitializerPrompt(finder, System.in, System.out, System.err);
                p.getInitializedFileAsync(callback);
            } else {
                ConfigInitializerDialog d = new ConfigInitializerDialog(finder);
                d.getInitializedFileAsync(callback);
            }
        } else {
            try {
                ConfigFile cf = new ConfigFile(file);
                callback.accept(cf, null);
            } catch (IOException ex) {
                callback.accept(null, ex);
            }
        }
    }

/**
     * Synchronously find a config file or create one if none exists.
     * 
     * If a config file corresponding to the program-identifying parameters passed
     * already exists, it is returned with no user interaction. Otherwise, the method
     * will block until user input confirms the settings with which the config file
     * is to be initialized. This method presents a warning to the user and returns
     * a {@link ConfigMap} if a persistent {@link ConfigFile} cannot be created.
     * 
     * In environments supporting graphical interfaces, a {@link ConfigInitializerDialog}
     * will be displayed and used to interact with the user. In headless environments,
     * a {@link ConfigInitializerPrompt} will interact with the user through {@code System.in}
     * and {@code System.out}.
     * 
     * The arguments to this method are passed to {@link ConfigFinder#ConfigFinder}.
     * 
     * @param callee some class inside the application whose configuration will be located
     * @param configName the name of the application whose configuration will be located
     * @return a new or preexisting configuration file for the described application
     */
    public static Config findOrCreateConfigWithFallback(Class<?> callee, String configName) {
        try {
            Config cfg = findOrCreateConfig(callee, configName);
            if (cfg == null) {
                String message =
                        "User canceled config initializer without creating config file" +
                        System.getProperty("line.separator") +
                        "Configuration changes will not be persistent.";
                if (java.awt.GraphicsEnvironment.isHeadless()) {
                    System.err.println(message);
                } else {
                    JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.WARNING_MESSAGE);
                }
                return new ConfigMap();
            }
            return cfg;
        } catch (IOException ex) {
            String message =
                    "Failed to open config file with message: " + ex.getMessage() +
                    System.getProperty("line.separator") +
                    "Configuration changes will not be persistent.";
            if (java.awt.GraphicsEnvironment.isHeadless()) {
                System.err.println(message);
            } else {
                JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
            }
            return new ConfigMap();
        }
    }

    /**
     * Asynchronously find a config file or create one if none exists.
     * 
     * If a config file corresponding to the program-identifying parameters passed
     * already exists, it is passed to the callback immediately with no user interaction.
     * Otherwise, the method will return and later (once user input confirms the settings
     * with which the config file is to be initialized) call the provided callback.
     * 
     * This method presents a warning to the user and passes a {@link ConfigMap} to
     * {@code callback} if a persistent {@link ConfigFile} cannot be created.
     * 
     * In environments supporting graphical interfaces, a {@link ConfigInitializerDialog}
     * will be displayed and used to interact with the user. In headless environments,
     * a {@link ConfigInitializerPrompt} will interact with the user through {@code System.in}
     * and {@code System.out}.
     * 
     * The first two arguments to this method are passed to {@link ConfigFinder#ConfigFinder}.
     * 
     * @param callee some class inside the application whose configuration will be located
     * @param configName the name of the application whose configuration will be located
     * @param callback to be executed when the user interaction is complete
     */
    public static void findOrCreateConfigAsyncWithFallback(Class<?> callee, String configName, Consumer<Config> callback) {
        findOrCreateConfigAsync(callee, configName, (res, err) -> {
            if (res == null) {
                if (err != null) {
                    String message =
                            "Failed to open config file with message: " + err.getMessage() +
                            System.getProperty("line.separator") +
                            "Configuration changes will not be persistent.";
                    if (java.awt.GraphicsEnvironment.isHeadless()) {
                        System.err.println(message);
                    } else {
                        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    String message =
                            "User canceled config initializer without creating config file" +
                            System.getProperty("line.separator") +
                            "Configuration changes will not be persistent.";
                    if (java.awt.GraphicsEnvironment.isHeadless()) {
                        System.err.println(message);
                    } else {
                        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.WARNING_MESSAGE);
                    }
                }
                callback.accept(new ConfigMap());
            } else {
                callback.accept(res);
            }
        });
    }
}
