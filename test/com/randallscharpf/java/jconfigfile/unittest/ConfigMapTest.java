/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.randallscharpf.java.jconfigfile.unittest;

import com.randallscharpf.java.jconfigfile.ConfigMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class ConfigMapTest {
    
    ConfigMap uut;
    
    public ConfigMapTest() {
        // use setUp for initialization
    }

    @BeforeEach
    public void setUp() {
        uut = new ConfigMap();
    }
    
    @AfterEach
    public void tearDown() {
        assertDoesNotThrow(() -> {
            uut.close();
        });
    }

    @Test
    public void testSetGet() {
        // test default values
        assertEquals(uut.getKeyOrDefault("key 1", "fallback 1"), "fallback 1");
        assertEquals(uut.getKeyOrDefault("key 1", "fallback 2"), "fallback 2");
        assertEquals(uut.getKeyOrDefault("key 2", "fallback 3"), "fallback 3");
        assertEquals(uut.getKeyOrDefault("key 2", "fallback 4"), "fallback 4");
        // test adding values
        uut.setKey("key 1", "value 1");
        uut.setKey("key 2", "value 2");
        uut.setKey("key 3", "value 3");
        uut.setKey("key 4", "value 4");
        // test getting non-default values
        assertEquals(uut.getKeyOrDefault("key 1", "fallback 1"), "value 1");
        assertEquals(uut.getKeyOrDefault("key 2", "fallback 2"), "value 2");
        assertEquals(uut.getKeyOrDefault("key 3", "fallback 3"), "value 3");
        assertEquals(uut.getKeyOrDefault("key 4", "fallback 4"), "value 4");
    }
    
    @Test
    public void testEvilKeyValue() {
        String evilKey = "";
        String evilValue = "";
        for (char c = 0; c < 256; c++) {
            evilKey = evilKey + c;
            evilValue = c + evilValue;
        }
        uut.setKey(evilKey, evilValue);
        assertEquals(uut.getKeyOrDefault(evilKey, "null"), evilValue);
    }
    
    @Test
    public void testRemove() {
        uut.removeKey("doesn't exist");
        assertEquals(uut.getKeys().size(), 0);
        uut.setKey("key 1", "value 1");
        uut.removeKey("also doesn't exist");
        assertEquals(uut.getKeys().size(), 1);
        uut.setKey("key 2", "value 1");
        uut.removeKey("key 2");
        assertEquals(uut.getKeys().size(), 1);
        uut.setKey("key 3", "value 1");
        uut.setKey("key 4", "value 1");
        uut.removeKey("key 1");
        assertEquals(uut.getKeys().size(), 2);
    }
    
    @Test
    public void testKeySet() {
        Set<String> expected_result = new HashSet<>();
        // check empty key set
        assertEquals(uut.getKeys(), expected_result);
        // add key
        uut.setKey("apple", "10");
        expected_result.add("apple");
        assertEquals(uut.getKeys(), expected_result);
        // add new key
        uut.setKey("banana", "20");
        expected_result.add("banana");
        assertEquals(uut.getKeys(), expected_result);
        // overwrite old key
        uut.setKey("apple", "30");
        assertEquals(uut.getKeys(), expected_result);
        // add a few more keys
        uut.setKey("carrot", "40");
        uut.setKey("banana", "50");
        uut.setKey("date", "60");
        expected_result.add("carrot");
        expected_result.add("date");
        assertEquals(uut.getKeys(), expected_result);
    }
}
