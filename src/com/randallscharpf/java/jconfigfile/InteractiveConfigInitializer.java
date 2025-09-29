/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.randallscharpf.java.jconfigfile;

import java.io.File;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.JOptionPane;

public class InteractiveConfigInitializer {
    
    public static Config findOrCreateConfig(Class<?> callee, String configName) throws IOException {
        ConfigFinder finder = new ConfigFinder(callee, configName);
        File file = finder.searchForConfig();
        if (!file.exists()) {
            ConfigInitializerDialog d = new ConfigInitializerDialog(finder);
            return d.getInitializedFile();
        } else {
            return new ConfigFile(file);
        }
    }
    
    public static void findOrCreateConfigAsync(Class<?> callee, String configName, BiConsumer<Config, IOException> callback) {
        ConfigFinder finder = new ConfigFinder(callee, configName);
        File file = finder.searchForConfig();
        if (!file.exists()) {
            ConfigInitializerDialog d = new ConfigInitializerDialog(finder);
            d.getInitializedFileAsync(callback);
        } else {
            try {
                Config cf = new ConfigFile(file);
                callback.accept(cf, null);
            } catch (IOException ex) {
                callback.accept(null, ex);
            }
        }
    }
    
    public static Config findOrCreateConfigWithFallback(Class<?> callee, String configName) {
        try {
            Config cfg = findOrCreateConfig(callee, configName);
            if (cfg == null) {
                return new ConfigMap();
            }
            return cfg;
        } catch (IOException ex) {
                JOptionPane.showMessageDialog(null,
                       "Failed to open config file with message: " + ex.getMessage() +
                       "\nConfiguration changes will not be persistent.",
                       "Error", JOptionPane.ERROR_MESSAGE);
            return new ConfigMap();
        }
    }
    
    public static void findOrCreateConfigAsyncWithFallback(Class<?> callee, String configName, Consumer<Config> callback) {
        findOrCreateConfigAsync(callee, configName, (res, err) -> {
            if (res == null) {
                JOptionPane.showMessageDialog(null,
                        "Failed to open config file with message: " + (err == null ? "user cancelled" : err.getMessage()) +
                        "\nConfiguration changes will not be persistent.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                callback.accept(new ConfigMap());
            } else {
                callback.accept(res);
            }
        });
    }
}
