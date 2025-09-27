/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.randallscharpf.java.jconfigfile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConfigMap implements Config {
    
    private final Map<String, String> pairings;

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
            pairings.put(key, fallback);
        }
        return pairings.get(key);
    }

    @Override
    public Set<String> getKeys() {
        return pairings.keySet();
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
