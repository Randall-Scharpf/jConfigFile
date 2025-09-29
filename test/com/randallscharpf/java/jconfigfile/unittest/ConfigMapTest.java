/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.randallscharpf.java.jconfigfile.unittest;

import com.randallscharpf.java.jconfigfile.ConfigMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Timeout(value = 20, unit = TimeUnit.SECONDS)
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
        assertEquals("fallback 1", uut.getKeyOrDefault("key 1", "fallback 1"));
        assertEquals("fallback 2", uut.getKeyOrDefault("key 1", "fallback 2"));
        assertEquals("fallback 3", uut.getKeyOrDefault("key 2", "fallback 3"));
        assertEquals("fallback 4", uut.getKeyOrDefault("key 2", "fallback 4"));
        // test adding values
        uut.setKey("key 1", "value 1");
        uut.setKey("key 2", "value 2");
        uut.setKey("key 3", "value 3");
        uut.setKey("key 4", "value 4");
        // test getting non-default values
        assertEquals("value 1", uut.getKeyOrDefault("key 1", "fallback 1"));
        assertEquals("value 2", uut.getKeyOrDefault("key 2", "fallback 2"));
        assertEquals("value 3", uut.getKeyOrDefault("key 3", "fallback 3"));
        assertEquals("value 4", uut.getKeyOrDefault("key 4", "fallback 4"));
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
        assertEquals(evilValue, uut.getKeyOrDefault(evilKey, "null"));
    }
    
    @Test
    public void testRemove() {
        uut.removeKey("doesn't exist");
        assertEquals(0, uut.getKeys().size());
        uut.setKey("key 1", "value 1");
        uut.removeKey("also doesn't exist");
        assertEquals(1, uut.getKeys().size());
        uut.setKey("key 2", "value 1");
        uut.removeKey("key 2");
        assertEquals(1, uut.getKeys().size());
        uut.setKey("key 3", "value 1");
        uut.setKey("key 4", "value 1");
        uut.removeKey("key 1");
        assertEquals(2, uut.getKeys().size());
    }
    
    @Test
    public void testKeySet() {
        Set<String> expected_result = new HashSet<>();
        // check empty key set
        assertEquals(expected_result, uut.getKeys());
        // add key
        uut.setKey("apple", "10");
        expected_result.add("apple");
        assertEquals(expected_result, uut.getKeys());
        // add new key
        uut.setKey("banana", "20");
        expected_result.add("banana");
        assertEquals(expected_result, uut.getKeys());
        // overwrite old key
        uut.setKey("apple", "30");
        assertEquals(expected_result, uut.getKeys());
        // add a few more keys
        uut.setKey("carrot", "40");
        uut.setKey("banana", "50");
        uut.setKey("date", "60");
        expected_result.add("carrot");
        expected_result.add("date");
        assertEquals(expected_result, uut.getKeys());
    }
}
