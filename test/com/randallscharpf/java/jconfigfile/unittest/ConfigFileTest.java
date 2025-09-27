/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.randallscharpf.java.jconfigfile.unittest;

import com.randallscharpf.java.jconfigfile.ConfigFile;
import com.randallscharpf.java.jconfigfile.ConfigFinder;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class ConfigFileTest {
    
    ConfigFile uut;
    
    public ConfigFileTest() {
        // use setUp for initialization
    }

    @BeforeEach
    public void setUp() {
        // give each test case a separate config file, since they're persistent
    }
    
    @AfterEach
    public void tearDown() {
        // since individual methods handle constructing config files, they can close them
    }

    @Test
    public void testSetGet() {
        assertDoesNotThrow(() -> {
            uut = new ConfigFile(new ConfigFinder(getClass(), "jConfigFile_ConfigFileTest_testSetGet").searchForConfig());
            // ensure we know the pre-test state of the relevant keys in the file
            uut.removeKey("key 1");
            uut.removeKey("key 2");
            uut.removeKey("key 3");
            uut.removeKey("key 4");
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
            // ensure persistence
            uut.close();
            uut = new ConfigFile(new ConfigFinder(getClass(), "jConfigFile_ConfigFileTest_testSetGet").searchForConfig());
            assertEquals(uut.getKeyOrDefault("key 1", "fallback 1"), "value 1");
            assertEquals(uut.getKeyOrDefault("key 2", "fallback 2"), "value 2");
            assertEquals(uut.getKeyOrDefault("key 3", "fallback 3"), "value 3");
            assertEquals(uut.getKeyOrDefault("key 4", "fallback 4"), "value 4");
            uut.close();
        });
    }
    
    @Test
    public void testEvilKeyValue() {
        assertDoesNotThrow(() -> {
            uut = new ConfigFile(new ConfigFinder(getClass(), "jConfigFile_ConfigFileTest_testEvilKeyValue").searchForConfig());
            String evilKey = "";
            String evilValue = "";
            for (char c = 0; c < 256; c++) {
                evilKey = evilKey + c;
                evilValue = c + evilValue;
            }
            uut.setKey(evilKey, evilValue);
            assertEquals(uut.getKeyOrDefault(evilKey, "null"), evilValue);
            uut.close();
            uut = new ConfigFile(new ConfigFinder(getClass(), "jConfigFile_ConfigFileTest_testEvilKeyValue").searchForConfig());
            assertEquals(uut.getKeyOrDefault(evilKey, "null"), evilValue);
            uut.close();
        });
    }
    
    @Test
    public void testRemove() {
        assertDoesNotThrow(() -> {
            uut = new ConfigFile(new ConfigFinder(getClass(), "jConfigFile_ConfigFileTest_testRemove").searchForConfig());
            // ensure we know the pre-test state of the relevant keys in the file
            uut.removeKey("key 1");
            uut.removeKey("key 2");
            uut.removeKey("key 3");
            uut.removeKey("key 4");
            uut.removeKey("doesn't exist");
            assertEquals(uut.getKeys().size(), 0);
            uut.setKey("key 1", "value 1");
            uut.removeKey("also doesn't exist");
            assertEquals(uut.getKeys().size(), 1);
            uut.setKey("key 2", "value 1");
            uut.close();
            uut = new ConfigFile(new ConfigFinder(getClass(), "jConfigFile_ConfigFileTest_testRemove").searchForConfig());
            uut.removeKey("key 2");
            assertEquals(uut.getKeys().size(), 1);
            uut.setKey("key 3", "value 1");
            uut.setKey("key 4", "value 1");
            uut.removeKey("key 1");
            uut.close();
            uut = new ConfigFile(new ConfigFinder(getClass(), "jConfigFile_ConfigFileTest_testRemove").searchForConfig());
            assertEquals(uut.getKeys().size(), 2);
            uut.removeKey("key 3");
            uut.removeKey("key 4");
            assertEquals(uut.getKeys().size(), 0);
            uut.close();
        });
    }
    
    @Test
    public void testKeySet() {
        assertDoesNotThrow(() -> {
            uut = new ConfigFile(new ConfigFinder(getClass(), "jConfigFile_ConfigFileTest_testKeySet").searchForConfig());
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
            uut.close();
            uut = new ConfigFile(new ConfigFinder(getClass(), "jConfigFile_ConfigFileTest_testKeySet").searchForConfig());
            assertEquals(uut.getKeys(), expected_result);
            uut.close();
        });
    }
    
    @Test
    public void testEncodeDecode() {
        assertDoesNotThrow(() -> {
            uut = new ConfigFile(new ConfigFinder(getClass(), "jConfigFile_ConfigFileTest_testEncodeDecode").searchForConfig());
            assertEquals("3132333435", uut.encode("12345"));
            assertEquals("616263", uut.encode("abc"));
            assertEquals("4142434445464748494a", uut.encode("ABCDEFGHIJ"));
            assertEquals("", uut.encode(""));
            assertEquals("000a5c203d", uut.encode("\0\n\\ ="));
            assertEquals("12345", uut.decode("3132333435"));
            assertEquals("abc", uut.decode("616263"));
            assertEquals("ABCDEFGHIJ", uut.decode("4142434445464748494a"));
            assertEquals("", uut.decode(""));
            assertEquals("\0\n\\ =", uut.decode("000a5c203d"));
            uut.close();
        });
    }
}
