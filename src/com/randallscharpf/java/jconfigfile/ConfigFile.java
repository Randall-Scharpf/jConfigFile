package com.randallscharpf.java.jconfigfile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * File-backed {@link Config} implementation.
 * 
 * Stores key-value pairs in a caller-selected file in an accessible, writable
 * file-system. This implementation holds a lock on the backing file for from the
 * time of construction until the object is closed. Null keys and values are permitted
 * and passing `null` works in the same way as passing any actual string would.
 * 
 * The getter and setter methods in this config implementation are NOT thread-safe,
 * but the {@code save} method (and accordingly, the {@code close} method) are
 * synchronized. Both of these methods may require extensive I/O, so synchronizing
 * them makes it possible to call them from a helper thread where such blocking is
 * unacceptable. The getter and setter methods should not require extensive computation
 * or I/O and are not synchronized, users requiring multi-thread access must synchronize
 * externally. Users may use the {@code ConfigFile} itself as a synchronization key.
 * 
 * The internal format of the backing file stores each key-value pair as a line of
 * text in the file. The key and value are separated by {@code =}. Both the key
 * and value are encoded into a string containing only digits and lowercase letters
 * to avoid cases where a key or value contains an equals sign or newline character.
 */
public class ConfigFile implements Config {

    private final RandomAccessFile file;
    private final FileLock fileLock;
    private final Object saveLock = new Object();

    private final Map<String, String> pairings;

    /**
     * Creates a new file-backed {@link Config} and holds it open.
     * 
     * When the constructor is called, the file {@code persistentCopy} is first locked
     * to prevent other programs (or other objects in the same program) from writing
     * to the file. Then, the file is read to determine the existing key-value pairs
     * in the file. The file is kept open after the constructor completes to ensure
     * later operations don't need to re-read the file.
     * 
     * @param persistentCopy backing file to read and write from
     * @throws IOException if the file permissions do not allow reading
     * @throws java.nio.channels.OverlappingFileLockException if the file cannot be locked
     */
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
            // make a copy of the entries to handle concurrent modification of underlying map
            Map.Entry<String, String>[] entries = pairings.entrySet().toArray(new Map.Entry[pairings.size()]);
            // synchornize to handle concurrent modification of backing file
            synchronized (saveLock) {
                file.seek(0);
                for (Map.Entry<String, String> entry : entries) {
                    file.writeBytes(encode(entry.getKey())+"="+encode(entry.getValue())+"\n");
                }
                file.setLength(file.getFilePointer());
            }
        } else {
            throw new IllegalStateException("the file backing this config is not open");
        }
    }

    /**
     * Encodes any string into a non-null string of only lowercase letters and digits.
     * 
     * Using this function guarantees special characters, punctuation, and control
     * characters are not present in a string, ensuring encoded strings can be discerned
     * from enclosing serialization and structural elements. Every input string maps
     * to a unique encoded string.
     * 
     * @param humanReadable any string to escape punctuation from
     * @return a unique string containing only lowercase letters and digits
     */
    public static String encode(String humanReadable) {
        if (humanReadable == null) {
            return "null";
        }
        String hex = "";
        for (int i = 0; i < humanReadable.length(); i++) {
            hex += String.format("%02x", (int) humanReadable.charAt(i));
        }
        return hex;
    }

    /**
     * Reverses {@link ConfigFile#encode}.
     * 
     * Strings which cannot have been created by {@link ConfigFile#encode}
     * and are passed as input to this function may result in an exception being
     * thrown or any value being returned.
     * 
     * @param hex the encoded version of the string to decode
     * @return the original string
     */
    public static String decode(String hex) {
        if ("null".equals(hex)) {
            return null;
        }
        String humanReadable = "";
        for (int i = 0; i + 1 < hex.length(); i += 2) {
            humanReadable += (char) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return humanReadable;
    }

}
