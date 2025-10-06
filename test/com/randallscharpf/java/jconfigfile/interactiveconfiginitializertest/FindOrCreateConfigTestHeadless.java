///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
// */
//package com.randallscharpf.java.jconfigfile.interactiveconfiginitializertest;
//
//import com.randallscharpf.java.jconfigfile.Config;
//import com.randallscharpf.java.jconfigfile.ConfigFile;
//import com.randallscharpf.java.jconfigfile.ConfigFinder;
//import com.randallscharpf.java.jconfigfile.ConfigLocation;
//import com.randallscharpf.java.jconfigfile.InteractiveConfigInitializer;
//
//import java.io.File;
//import java.util.concurrent.TimeUnit;
//
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeAll;
//
//import org.junit.jupiter.api.Timeout;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.EnumSource;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@Timeout(value = 10, unit = TimeUnit.SECONDS)
//public class FindOrCreateConfigTestHeadless {
//
//    private final ConfigFinder standardLocator;
//
//    public FindOrCreateConfigTestHeadless() {
//        standardLocator = new ConfigFinder(getClass(), "jConfigFile_InteractiveConfigInitializerTest");
//    }
//    
//    private String hexString(int length) {
//        String randomHex = "";
//        for (int i = 0; i < length; i++) {
//            randomHex += String.format("%x", (int) (Math.random() * 16));
//        }
//        return randomHex;
//    }
//    
//    private String originalHeadlessProperty;
//    
//    @BeforeAll
//    public void setUpClass() {
//        originalHeadlessProperty = System.getProperty("java.awt.headless");
//        System.setProperty("java.awt.headless", "false");
//    }
//    
//    @AfterAll
//    public void tearDownClass() {
//        System.setProperty("java.awt.headless", originalHeadlessProperty);
//    }
//
//    @BeforeEach
//    public void setUpTest() {
//        for (ConfigLocation loc : ConfigLocation.values()) {
//            standardLocator.configAt(loc).delete();
//        }
//    }
//
//    @ParameterizedTest
//    @EnumSource(ConfigLocation.class)
//    public void testFileAlreadyExists(ConfigLocation loc) {
//        // create a unique file for the location we're testing
//        assertDoesNotThrow(() -> {
//            String fileId = hexString(64);
//            File f = standardLocator.configAt(loc);
//            Config cfg = new ConfigFile(f);
//            cfg.setKey("fileId", fileId);
//            cfg.close();
//            // make sure InteractiveConfigInitializer picks up the correct file
//            cfg = InteractiveConfigInitializer.findOrCreateConfig(getClass(), "jConfigFile_InteractiveConfigInitializerTest");
//            assertEquals(fileId, cfg.getKeyOrDefault("fileId", ""));
//            assertEquals(1, cfg.getKeys().size());
//            cfg.close();
//            // clean up generated files
//            f.delete();
//        });
//    }
//
//    @Test
//    public void testCreateNewFile() {
//        assertThrows(java.awt.HeadlessException.class, () -> {
//            InteractiveConfigInitializer.findOrCreateConfig(getClass(), "jConfigFile_InteractiveConfigInitializerTest");
//        });
//    }
//
//}