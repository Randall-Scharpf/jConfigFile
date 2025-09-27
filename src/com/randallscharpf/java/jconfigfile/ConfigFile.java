/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.randallscharpf.java.jconfigfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConfigFile implements Config {
    
    private final File persistentCopy;
    private final FileLock lock;
    private final Map<String, String> pairings;
    private boolean open;

    // Make sure you don't open two copies of the same config file, then edit them both, then
    // close them both. This code checks that there are never two config files with the same
    public ConfigFile(File persistentCopy) throws IOException {
        this.persistentCopy = persistentCopy;
        this.pairings = new HashMap<>();
        persistentCopy.getParentFile().mkdirs();
        persistentCopy.createNewFile();
        try (
            FileReader reader = new FileReader(persistentCopy);
            BufferedReader buf = new BufferedReader(reader)
        ) {
            String line;
            // read key-value lines, which delineate key from value by the first = in the line
            // spacing in the key and value are preserved in our mapping
            // ignore comments (which start with ; in .ini), blank lines, and invalid lines
            while ((line = buf.readLine()) != null) {
                String[] tokens = line.split("=", 2);
                if (line.length() > 0 && line.charAt(0) != ';' && tokens.length == 2) {
                    pairings.put(tokens[0], tokens[1]);
                }
            }
        }
        FileChannel ch = FileChannel.open(persistentCopy.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE);
        lock = ch.lock();
        open = true;
    }
    
    @Override
    public String getKeyOrDefault(String key, String fallback) {
        if (lock != null && lock.isValid()) {
            if (pairings.containsKey(key)) {
                return pairings.get(key);
            } else {
                pairings.put(key, fallback);
                return fallback;
            }
        } else {
            throw new IllegalStateException("Cannot get mapping from uninitialized config file!");
        }
    }
    
    @Override
    public void setKey(String key, String value) {
        if (lock != null && lock.isValid()) {
            pairings.put(key, value);
        } else {
            throw new IllegalStateException("Cannot set mapping in uninitialized config file!");
        }
    }
    
    @Override
    public Set<String> getKeys() {
        return pairings.keySet();
    }

    @Override
    public void close() throws IOException {
        if (!open) {
            return;
        }
        save();
        open = false;
        if (lock != null) {
            lock.release();
        }
    }

    @Override
    public void save() throws IOException {
        if (lock != null) {
            lock.release();
        }
        try (
            PrintWriter p = new PrintWriter(persistentCopy)
        ) {
            for (Map.Entry<String, String> entry : pairings.entrySet()) {
                p.println(entry.getKey()+"="+entry.getValue());
            }
        }
        if (lock != null) {
            lock.channel().lock();
        }
    }
    
}
