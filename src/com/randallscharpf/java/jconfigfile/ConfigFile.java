/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.randallscharpf.java.jconfigfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConfigFile implements Config {

    private final RandomAccessFile fileWriter;
//    private final FileLock lock;
    private final Map<String, String> pairings;

    // Make sure you don't open two copies of the same config file, then edit them both, then
    // close them both. Keeping the PrintWriter open for the whole object duration ensures this.
    public ConfigFile(File persistentCopy) throws IOException {
        persistentCopy.getParentFile().mkdirs();
        persistentCopy.createNewFile();
        this.fileWriter = new RandomAccessFile(persistentCopy, "rw");
        this.pairings = new HashMap<>();
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
                    pairings.put(decode(tokens[0]), decode(tokens[1]));
                }
            }
        }
    }

    @Override
    public String getKeyOrDefault(String key, String fallback) {
        if (pairings.containsKey(key)) {
            return pairings.get(key);
        } else {
            return fallback;
        }
    }

    @Override
    public void setKey(String key, String value) {
        pairings.put(key, value);
    }

    @Override
    public Set<String> getKeys() {
        return pairings.keySet();
    }
    
    @Override
    public void removeKey(String key) {
        pairings.remove(key);
    }

    @Override
    public void close() throws IOException {
        save();
        fileWriter.close();
    }

    @Override
    public void save() throws IOException {
        fileWriter.seek(0);
        for (Map.Entry<String, String> entry : pairings.entrySet()) {
            fileWriter.writeBytes(encode(entry.getKey())+"="+encode(entry.getValue())+"\n");
        }
        fileWriter.setLength(fileWriter.getFilePointer());
    }
    
    public String encode(String humanReadable) {
        String hex = "";
        for (int i = 0; i < humanReadable.length(); i++) {
            hex += String.format("%02x", (int) humanReadable.charAt(i));
        }
        return hex;
    }
    
    public String decode(String hex) {
        String humanReadable = "";
        for (int i = 0; i + 1 < hex.length(); i += 2) {
            humanReadable += (char) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return humanReadable;
    }

}
