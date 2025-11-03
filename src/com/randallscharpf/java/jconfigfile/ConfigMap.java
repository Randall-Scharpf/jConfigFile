package com.randallscharpf.java.jconfigfile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Map-backed {@link Config} implementation.
 * 
 * NOTE: Changes made to a config of this type do not persist beyond the lifetime
 * of the object.
 * 
 * Stores key-value pairs in a map object. Null keys and values are permitted
 * and passing `null` works in the same way as passing any actual string would.
 * The getter and setter methods in this config implementation are NOT thread-safe.
 * Users may use the {@code ConfigFile} itself as a synchronization key.
 * The {@code save} and {@code close} methods of this implementation are NOOPs.
 */
public class ConfigMap implements Config {

    private final Map<String, String> pairings;

    /**
     * Creates a new {@link Config} backed by a map object.
     */
    public ConfigMap() {
        this.pairings = new HashMap<>();
    }

    @Override
    public void setKey(String key, String value) {
        pairings.put(key, value);
    }

    @Override
    public String getKeyOrDefault(String key, String fallback) {
        if (!pairings.containsKey(key)) {
            return fallback;
        }
        return pairings.get(key);
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
    public void save() throws IOException {
        // do nothing, as this map cannot be saved and serves as a fallback config
    }

    @Override
    public void close() throws IOException {
        // do nothing, this uses no external resources
    }

}
