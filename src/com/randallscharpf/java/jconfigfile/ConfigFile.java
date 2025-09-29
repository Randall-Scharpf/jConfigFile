/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.randallscharpf.java.jconfigfile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConfigFile implements Config {

    private final RandomAccessFile file;
    private final FileLock fileLock;

    private final Map<String, String> pairings;

    // Make sure you don't open two copies of the same config file, then edit them both, then
    // close them both. Keeping the PrintWriter open for the whole object duration ensures this.
    public ConfigFile(File persistentCopy) throws IOException {
        persistentCopy.getParentFile().mkdirs();
        persistentCopy.createNewFile();
        this.file = new RandomAccessFile(persistentCopy, "rw");
        this.fileLock = file.getChannel().tryLock();
        if (fileLock == null) {
            throw new IOException(String.format(
                    "ConfigFile cannot open %s because another process has locked a portion of the file",
                    persistentCopy.getAbsolutePath()
            ));
        }
        this.pairings = new HashMap<>();
        String line;
        // read key-value lines, which delineate key from value by the first = in the line
        // spacing in the key and value are preserved in our mapping
        // ignore comments (which start with ; in .ini), blank lines, and invalid lines
        file.seek(0);
        while ((line = file.readLine()) != null) {
            String[] tokens = line.split("=");
            if (line.length() > 0 && line.charAt(0) != ';' && tokens.length == 2) {
                try {
                    pairings.put(decode(tokens[0]), decode(tokens[1]));
                } catch(NumberFormatException ex) {
                    // invalid line: skip parsing
                }
            }
        }
    }

    @Override
    public String getKeyOrDefault(String key, String fallback) {
        if (fileLock != null && fileLock.isValid()) {
            if (pairings.containsKey(key)) {
                return pairings.get(key);
            } else {
                return fallback;
            }
        } else {
            throw new IllegalStateException("the file backing this config is not open");
        }
    }

    @Override
    public void setKey(String key, String value) {
        if (fileLock != null && fileLock.isValid()) {
            pairings.put(key, value);
        } else {
            throw new IllegalStateException("the file backing this config is not open");
        }
    }

    @Override
    public Set<String> getKeys() {
        if (fileLock != null && fileLock.isValid()) {
            return pairings.keySet();
        } else {
            throw new IllegalStateException("the file backing this config is not open");
        }
    }
    
    @Override
    public void removeKey(String key) {
        if (fileLock != null && fileLock.isValid()) {
            pairings.remove(key);
        } else {
            throw new IllegalStateException("the file backing this config is not open");
        }
    }

    @Override
    public void close() throws IOException {
        if (fileLock != null && fileLock.isValid()) {
            save();
            fileLock.release();
            file.close();
        } else {
            throw new IllegalStateException("the file backing this config is not open");
        }
    }

    @Override
    public void save() throws IOException {
        if (fileLock != null && fileLock.isValid()) {
            file.seek(0);
            for (Map.Entry<String, String> entry : pairings.entrySet()) {
                file.writeBytes(encode(entry.getKey())+"="+encode(entry.getValue())+"\n");
            }
            file.setLength(file.getFilePointer());
        } else {
            throw new IllegalStateException("the file backing this config is not open");
        }
    }
    
    public static String encode(String humanReadable) {
        String hex = "";
        for (int i = 0; i < humanReadable.length(); i++) {
            hex += String.format("%02x", (int) humanReadable.charAt(i));
        }
        return hex;
    }
    
    public static String decode(String hex) {
        String humanReadable = "";
        for (int i = 0; i + 1 < hex.length(); i += 2) {
            humanReadable += (char) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return humanReadable;
    }

}
