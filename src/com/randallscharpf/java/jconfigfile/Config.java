/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.randallscharpf.java.jconfigfile;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;

public interface Config extends Closeable {
    public void setKey(String key, String value);
    public String getKeyOrDefault(String key, String fallback);
    public Set<String> getKeys();
    public void save() throws IOException;
}
