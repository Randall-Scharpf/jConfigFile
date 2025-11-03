package com.randallscharpf.java.jconfigfile;

import java.io.*;
import java.net.URISyntaxException;
import javax.swing.filechooser.FileSystemView;

/**
 * Object to find consistent, logical places for an application configuration file.
 * 
 * Some configuration locations are determined based on the location that the application
 * runs from, allowing copies of an application on one computer to refer to separate
 * configurations. This also makes it easy for the user to find the configuration.
 * However, it can create apparent clutter in the application location, especially
 * if many applications share that location.
 * 
 * Other configuration locations are determined based on the name of the application.
 * This means that multiple copies of the application will share a configuration,
 * but care must be taken to ensure distinct applications use distinct names, lest
 * their configuration files conflict.
 * 
 * @see ConfigLocation
 */
public class ConfigFinder {
    private final String appName;
    private final File app;

    /**
     * Creates an object which finds configuration locations for a specific application.
     * 
     * The location of the application is determined based on the {@code Class} passed
     * to the constructor. A particular type of location inspected for configuration
     * files may not refer to both the app name and app location, but some do.
     * 
     * @param constructee some class inside the application whose configuration will be located
     * @param appName the name of the application whose configuration will be located
     */
    public ConfigFinder(Class<?> constructee, String appName) {
        this.appName = appName;
        File f;
        try {
            f = new File(constructee.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException ex) {
            f = new File(constructee.getProtectionDomain().getCodeSource().getLocation().getPath());
        }
        app = f;
    }

    /**
     * Returns the configuration file for the app corresponding to this object.
     * 
     * If a configuration file already exists in exactly one of the expected locations
     * for the application handled by this object, it will be returned.
     * 
     * If  configuration files exist in multiple of the expected locations for the
     * application handled by this object, one will be selected and returned. The
     * file corresponding to the location declared earliest in {@link ConfigLocation}
     * will override the other files.
     * 
     * If no file exists at any of the expected locations, an expected location for
     * a new configuration file will be returned. This method will not create any
     * files or directories implied by that path. The current default location corresponds
     * to {@link ConfigLocation#APPDATA}.
     * 
     * @return the configuration file found, or a path where one can be created
     */
    public File searchForConfig() {
        for (ConfigLocation l : ConfigLocation.values()) {
            if (configAt(l).exists()) {
                return configAt(l);
            }
        }
        return configAt(ConfigLocation.APPDATA);
    }

    /**
     * Computes the path corresponding to a logical configuration file location.
     * 
     * @param loc the type of configuration file location to use
     * @return the configuration file at the location
     */
    public File configAt(ConfigLocation loc) {
        File appdir;
        switch (loc) {
            case APPDATA:
                appdir = new File(System.getenv("appdata"), appName);
                return new File(appdir, appName + ".ini");
            case DOCUMENTS:
                appdir = new File(FileSystemView.getFileSystemView().getDefaultDirectory(), appName);
                return new File(appdir, appName + ".ini");
            case SIBLING:
                return new File(app.getParent(), appName + ".ini");
            case ETC:
                appdir = new File(app.getParent(), "etc");
                return new File(appdir, appName + ".ini");
            case USERPROFILE:
                appdir = new File(System.getProperty("user.home"), "." + appName);
                return new File(appdir, appName + ".ini");
            default:
                throw new UnsupportedOperationException("Unknown location " + loc.toString() + " provided for config file");
        }
    }
}
