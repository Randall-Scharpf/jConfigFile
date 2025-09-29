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
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.FileWriter;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;

@Timeout(value = 20, unit = TimeUnit.SECONDS)
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
            // ensure persistence
            uut.close();
            uut = new ConfigFile(new ConfigFinder(getClass(), "jConfigFile_ConfigFileTest_testSetGet").searchForConfig());
            assertEquals("value 1", uut.getKeyOrDefault("key 1", "fallback 1"));
            assertEquals("value 2", uut.getKeyOrDefault("key 2", "fallback 2"));
            assertEquals("value 3", uut.getKeyOrDefault("key 3", "fallback 3"));
            assertEquals("value 4", uut.getKeyOrDefault("key 4", "fallback 4"));
            uut.close();
        });
    }
    
    @Test
    public void testEvilKeyValue() {
        assertDoesNotThrow(() -> {
            uut = new ConfigFile(new ConfigFinder(getClass(), "jConfigFile_ConfigFileTest_testEvilKeyValue").searchForConfig());
            // make a key and value that contain EVERY special character
            String evilKey = "";
            String evilValue = "";
            for (char c = 0; c < 256; c++) {
                evilKey = evilKey + c;
                evilValue = c + evilValue;
            }
            // ensure we know the pre-test state of the relevant keys in the file
            uut.removeKey(evilKey);
            // test adding and removing new mapping across two program executions
            uut.setKey(evilKey, evilValue);
            assertEquals(evilValue, uut.getKeyOrDefault(evilKey, "null"));
            uut.close();
            uut = new ConfigFile(new ConfigFinder(getClass(), "jConfigFile_ConfigFileTest_testEvilKeyValue").searchForConfig());
            assertEquals(evilValue, uut.getKeyOrDefault(evilKey, "null"));
            assertEquals(1, uut.getKeys().size());
            uut.removeKey(evilKey);
            assertEquals("null", uut.getKeyOrDefault(evilKey, "null"));
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
            uut.removeKey("also doesn't exist");
            // try adding and removing different keys
            assertEquals(uut.getKeys().size(), 0);
            uut.setKey("key 1", "value 1");
            uut.removeKey("also doesn't exist");
            assertEquals(uut.getKeys().size(), 1);
            // ensure removing keys across saves works
            uut.setKey("key 2", "value 1");
            uut.close();
            uut = new ConfigFile(new ConfigFinder(getClass(), "jConfigFile_ConfigFileTest_testRemove").searchForConfig());
            uut.removeKey("key 2");
            assertEquals(uut.getKeys().size(), 1);
            // do it once more to be sure
            uut.setKey("key 3", "value 1");
            uut.setKey("key 4", "value 1");
            uut.removeKey("key 1");
            uut.close();
            uut = new ConfigFile(new ConfigFinder(getClass(), "jConfigFile_ConfigFileTest_testRemove").searchForConfig());
            assertEquals(uut.getKeys().size(), 2);
            // clean up
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
            // ensure we know the pre-test state of the relevant keys in the file
            uut.removeKey("apple");
            uut.removeKey("banana");
            uut.removeKey("carrot");
            uut.removeKey("date");
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
            // ensure persistence
            uut.close();
            uut = new ConfigFile(new ConfigFinder(getClass(), "jConfigFile_ConfigFileTest_testKeySet").searchForConfig());
            assertEquals(expected_result, uut.getKeys());
            // clean up
            uut.removeKey("apple");
            uut.removeKey("banana");
            uut.removeKey("carrot");
            uut.removeKey("date");
            uut.close();
        });
    }
    
    @Test
    public void testEncodeDecode() {
        assertDoesNotThrow(() -> {
            assertEquals("3132333435", ConfigFile.encode("12345"));
            assertEquals("616263", ConfigFile.encode("abc"));
            assertEquals("4142434445464748494a", ConfigFile.encode("ABCDEFGHIJ"));
            assertEquals("", ConfigFile.encode(""));
            assertEquals("000a5c203d", ConfigFile.encode("\0\n\\ ="));
            assertEquals("12345", ConfigFile.decode("3132333435"));
            assertEquals("abc", ConfigFile.decode("616263"));
            assertEquals("ABCDEFGHIJ", ConfigFile.decode("4142434445464748494a"));
            assertEquals("", ConfigFile.decode(""));
            assertEquals("\0\n\\ =", ConfigFile.decode("000a5c203d"));
        });
    }
    
    
    @Test
    public void testCorruptedFile() {
        assertDoesNotThrow(() -> {
            // write a badly-formed config file
            String evilKey = "";
            String evilValue = "";
            for (char c = 0; c < 256; c++) {
                evilKey = evilKey + c;
                evilValue = c + evilValue;
            }
            File configLocation = new ConfigFinder(getClass(), "jConfigFile_ConfigFileTest_testCorruptedFile").searchForConfig();
            configLocation.getParentFile().mkdirs();
            FileWriter testWriter = new FileWriter(configLocation);
            testWriter.write(ConfigFile.encode("abc")+"="+ConfigFile.encode("123")+"\n");
            testWriter.write(ConfigFile.encode("abc")+"="+ConfigFile.encode("234")+"\n");
            testWriter.write(ConfigFile.encode("abc")+"="+ConfigFile.encode("345")+"\n");
            testWriter.write("="+ConfigFile.encode("abc")+"="+ConfigFile.encode("123")+"\n");
            testWriter.write("="+evilValue+"="+evilKey+"\n");
            testWriter.write("="+evilValue+"="+evilKey+"\n");
            testWriter.write("not_encoded=not_encoded_value\n");
            testWriter.write("0o0_0n0o0e0=1o1_1n1o1e1_1a1u1\n");
            testWriter.write(evilValue+evilKey+"\n");
            testWriter.write(evilValue+"="+evilKey+"\n");
            testWriter.write("="+evilValue+evilKey+"\n");
            testWriter.write("="+evilValue+"="+evilKey+"\n");
            testWriter.close();
            // test parsing in the config and using it a little
            uut = new ConfigFile(configLocation);
            // test adding and removing new mapping across two program executions
            assertEquals("empty", uut.getKeyOrDefault("test_key", "empty"));
            uut.setKey("test_key", "hello world!");
            assertEquals("hello world!", uut.getKeyOrDefault("test_key", "empty"));
            uut.close();
            uut = new ConfigFile(configLocation);
            assertEquals("hello world!", uut.getKeyOrDefault("test_key", "empty"));
            uut.removeKey("test_key");
            assertEquals("empty", uut.getKeyOrDefault("test_key", "empty"));
            uut.close();
            // cleanup garbage file
            configLocation.delete();
        });
    }
    
    @Test
    public void testFileContention() {
        assertDoesNotThrow(() -> {
            File configLocation = new ConfigFinder(getClass(), "jConfigFile_ConfigFileTest_testFileContention").searchForConfig();
            configLocation.getParentFile().mkdirs();
            configLocation.createNewFile();
            FileLock lock = FileChannel.open(configLocation.toPath(), StandardOpenOption.WRITE).tryLock();
            assertNotNull(lock);
            assertThrows(java.nio.channels.OverlappingFileLockException.class, () -> {
                uut = new ConfigFile(configLocation);
            });
            lock.release();
        });
    }

}
