/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.randallscharpf.java.jconfigfile.unittest;

import com.randallscharpf.java.jconfigfile.ConfigFinder;
import com.randallscharpf.java.jconfigfile.ConfigLocation;

import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@Timeout(value = 10, unit = TimeUnit.SECONDS)
public class ConfigFinderTest {
    
    ConfigFinder uut;
    
    public ConfigFinderTest() {
    }
    
    @BeforeEach
    public void setUpTest() {
        uut = new ConfigFinder(getClass(), "jConfigFile_ConfigFinderTest");
    }
    
    @AfterEach
    public void tearDownTest() {
        // no resources allocated internally to ConfigFinder
    }

    @ParameterizedTest
    @EnumSource(ConfigLocation.class)
    public void testPathConflicts(ConfigLocation loc) {
        File f = uut.configAt(loc);
        assertFalse(f.exists());
        System.out.printf("Path for %s is %s\n", loc.toString(), f.getAbsolutePath());
    }

    @ParameterizedTest
    @EnumSource(ConfigLocation.class)
    public void testPathsWritable(ConfigLocation loc) {
        File f = uut.configAt(loc);
        boolean appDirCreated = false;
        if (!f.getParentFile().exists()) {
            assertTrue(f.getParentFile().mkdir());
            appDirCreated = true;
        }
        assertDoesNotThrow(() -> {
            FileWriter testWriter = new FileWriter(f);
            testWriter.write("Hello World!\n");
            testWriter.flush();
            testWriter.close();
            f.delete();
        });
        if (appDirCreated) {
            assertTrue(f.getParentFile().delete());
        }
    }

    @ParameterizedTest
    @EnumSource(ConfigLocation.class)
    public void testSearching(ConfigLocation loc) {
        File f = uut.configAt(loc);
        boolean appDirCreated = false;
        if (!f.getParentFile().exists()) {
            assertTrue(f.getParentFile().mkdir());
            appDirCreated = true;
        }
        assertDoesNotThrow(() -> {
            FileWriter testWriter = new FileWriter(f);
            testWriter.write("616263=303132\n");
            testWriter.flush();
            assertEquals(f, uut.searchForConfig());
            testWriter.close();
            f.delete();
        });
        if (appDirCreated) {
            assertTrue(f.getParentFile().delete());
        }
    }
    
    @Test
    public void testDefault() {
        Set<File> valid_defaults = new HashSet<>();
        for (ConfigLocation loc : ConfigLocation.values()) {
            File f = uut.configAt(loc);
            valid_defaults.add(f);
        }
        assertTrue(valid_defaults.contains(uut.searchForConfig()));
    }

}
