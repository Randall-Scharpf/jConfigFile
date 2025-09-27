/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.randallscharpf.java.jconfigfile;

import java.io.*;
import java.net.URISyntaxException;
import javax.swing.filechooser.FileSystemView;

public class ConfigFinder {
    private final String appName;
    private final File app;
    
    public ConfigFinder(Class constructee, String appName) {
        this.appName = appName;
        File f;
        try {
            f = new File(constructee.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException ex) {
            f = new File(constructee.getProtectionDomain().getCodeSource().getLocation().getPath());
        }
        app = f;
    }
    
    public File searchForConfig() {
        for (ConfigLocation l : ConfigLocation.values()) {
            if (configAt(l).exists()) {
                return configAt(l);
            }
        }
        return configAt(ConfigLocation.APPDATA);
    }
    
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
