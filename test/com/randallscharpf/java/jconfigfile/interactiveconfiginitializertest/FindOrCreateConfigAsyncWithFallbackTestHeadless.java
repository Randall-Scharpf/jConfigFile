/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.randallscharpf.java.jconfigfile.interactiveconfiginitializertest;

import com.randallscharpf.java.jconfigfile.Config;
import com.randallscharpf.java.jconfigfile.ConfigFile;
import com.randallscharpf.java.jconfigfile.ConfigFinder;
import com.randallscharpf.java.jconfigfile.ConfigLocation;
import com.randallscharpf.java.jconfigfile.ConfigMap;
import com.randallscharpf.java.jconfigfile.InteractiveConfigInitializer;

import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.condition.EnabledIf;

@EnabledIf("java.awt.GraphicsEnvironment#isHeadless")
@Timeout(value = 10, unit = TimeUnit.SECONDS)
public class FindOrCreateConfigAsyncWithFallbackTestHeadless {

    private final ConfigFinder standardLocator;
    private final Object syncKey;
    private volatile boolean resultReady;
    private volatile Config helperResult;

    public FindOrCreateConfigAsyncWithFallbackTestHeadless() {
        standardLocator = new ConfigFinder(getClass(), "jConfigFile_InteractiveConfigInitializerTest");
        syncKey = new Object();
    }
    
    private String hexString(int length) {
        String randomHex = "";
        for (int i = 0; i < length; i++) {
            randomHex += String.format("%x", (int) (Math.random() * 16));
        }
        return randomHex;
    }

    @BeforeEach
    public void setUpTest() {
        for (ConfigLocation loc : ConfigLocation.values()) {
            standardLocator.configAt(loc).delete();
        }
    }

    @ParameterizedTest
    @EnumSource(ConfigLocation.class)
    public void testFileAlreadyExists(ConfigLocation loc) {
        // create a unique file for the location we're testing
        assertDoesNotThrow(() -> {
            String fileId = hexString(64);
            File f = standardLocator.configAt(loc);
            Config cfg = new ConfigFile(f);
            cfg.setKey("fileId", fileId);
            cfg.close();
            // make sure InteractiveConfigInitializer picks up the correct file
            InteractiveConfigInitializer.findOrCreateConfigAsyncWithFallback(
                    getClass(),
                    "jConfigFile_InteractiveConfigInitializerTest",
                    (res) -> {
                        assertEquals(fileId, res.getKeyOrDefault("fileId", ""));
                        assertEquals(1, res.getKeys().size());
                        assertDoesNotThrow(res::close);
                        // clean up generated files
                        f.delete();
                    }
            );
        });
    }

    @ParameterizedTest
    @EnumSource(ConfigLocation.class)
    public void testCreateNewFile(ConfigLocation loc) {
        assertDoesNotThrow(() -> {
            // create file interactively
            Config result = callWithInteractionsInHelper(SystemStreamTester.CREATE_NEW_FILE_INTERACTIONS(loc));
            assertInstanceOf(ConfigFile.class, result);
            ConfigFile cfg = (ConfigFile) result;
            String fileId = hexString(64);
            cfg.setKey("fileId", fileId);
            cfg.close();
            // re-open file to ensure persistence
            resultReady = false;
            InteractiveConfigInitializer.findOrCreateConfigAsyncWithFallback(getClass(), "jConfigFile_InteractiveConfigInitializerTest", (res) -> {
                synchronized (syncKey) {
                    helperResult = res;
                    resultReady = true;
                    syncKey.notifyAll();
                }
            });
            synchronized (syncKey) {
                while (!resultReady) {
                    syncKey.wait();
                }
            }
            assertInstanceOf(ConfigFile.class, helperResult);
            cfg = (ConfigFile) helperResult;
            assertEquals(fileId, cfg.getKeyOrDefault("fileId", ""));
            assertEquals(1, cfg.getKeys().size());
            cfg.close();
            // clean up after outselves
            File cfgFile = standardLocator.configAt(loc);
            assertTrue(
                    cfgFile.delete(),
                    String.format("Failed to delete config file %s after findOrCreateConfigWithFallback created it", cfgFile.getAbsolutePath())
            );
        });
    }

    @Test
    public void testCancelWithoutSelection() {
        assertDoesNotThrow(() -> {
            // create file interactively
            Config result = callWithInteractionsInHelper(SystemStreamTester.CANCEL_ERROR_INTERACTIONS());
            assertInstanceOf(ConfigMap.class, result);
        });
    }

    @Test
    public void testFileCreationError() {
        String originalUserHome = System.getProperty("user.home");
        try {
            // Break USERPROFILE by redirecting it to an invalid path and ensure we get an error
            System.setProperty("user.home", "https://error-path");
            Config result = callWithInteractionsInHelper(SystemStreamTester.CREATE_NEW_FILE_ERROR_INTERACTIONS(ConfigLocation.USERPROFILE));
            assertInstanceOf(ConfigMap.class, result);
        } catch (InterruptedException ex) {
            fail(ex);
        } finally {
            System.setProperty("user.home", originalUserHome);
        }
    }

    @ParameterizedTest
    @EnumSource(ConfigLocation.class)
    public void testCreateCopy(ConfigLocation loc) {
        assertDoesNotThrow(() -> {
            File templateFile = new ConfigFinder(getClass(), "jConfigFile_TestTemplate").configAt(ConfigLocation.APPDATA);
            String fileId = hexString(64);
            Config template = new ConfigFile(templateFile);
            template.setKey("fileId", fileId);
            template.close();
            // create file interactively
            Config result = callWithInteractionsInHelper(SystemStreamTester.CREATE_COPY_INTERACTIONS(loc, templateFile.getPath()));
            assertInstanceOf(ConfigFile.class, result);
            ConfigFile cfg = (ConfigFile) result;
            assertEquals(fileId, cfg.getKeyOrDefault("fileId", ""));
            assertEquals(1, cfg.getKeys().size());
            cfg.close();
            // re-open file to ensure persistence
            resultReady = false;
            InteractiveConfigInitializer.findOrCreateConfigAsyncWithFallback(getClass(), "jConfigFile_InteractiveConfigInitializerTest", (res) -> {
                synchronized (syncKey) {
                    helperResult = res;
                    resultReady = true;
                    syncKey.notifyAll();
                }
            });
            synchronized (syncKey) {
                while (!resultReady) {
                    syncKey.wait();
                }
            }
            assertInstanceOf(ConfigFile.class, helperResult);
            cfg = (ConfigFile) helperResult;
            assertEquals(fileId, cfg.getKeyOrDefault("fileId", ""));
            assertEquals(1, cfg.getKeys().size());
            cfg.close();
            // clean up after outselves
            File cfgFile = standardLocator.configAt(loc);
            assertTrue(
                    cfgFile.delete(),
                    String.format("Failed to delete config file %s after findOrCreateConfigWithFallback created it", cfgFile.getAbsolutePath())
            );
            templateFile.delete();
        });
    }

    @Test
    public void testCancelFromCopyRequest() {
        assertDoesNotThrow(() -> {
            // create file interactively
            Config result = callWithInteractionsInHelper(SystemStreamTester.CREATE_COPY_AND_CANCEL_ERROR_INTERACTIONS(ConfigLocation.APPDATA));
            assertInstanceOf(ConfigMap.class, result);
        });
    }

    @Test
    public void testCancelCopyAndContinue() {
        assertDoesNotThrow(() -> {
            // create file interactively
            Config result = callWithInteractionsInHelper(SystemStreamTester.CREATE_COPY_AND_CONTINUE_INTERACTIONS(ConfigLocation.APPDATA));
            assertInstanceOf(ConfigFile.class, result);
            ConfigFile cfg = (ConfigFile) result;
            String fileId = hexString(64);
            cfg.setKey("fileId", fileId);
            cfg.close();
            // re-open file to ensure persistence
            // re-open file to ensure persistence
            resultReady = false;
            InteractiveConfigInitializer.findOrCreateConfigAsyncWithFallback(getClass(), "jConfigFile_InteractiveConfigInitializerTest", (res) -> {
                synchronized (syncKey) {
                    helperResult = res;
                    resultReady = true;
                    syncKey.notifyAll();
                }
            });
            synchronized (syncKey) {
                while (!resultReady) {
                    syncKey.wait();
                }
            }
            assertInstanceOf(ConfigFile.class, helperResult);
            cfg = (ConfigFile) helperResult;
            assertEquals(fileId, cfg.getKeyOrDefault("fileId", ""));
            assertEquals(1, cfg.getKeys().size());
            cfg.close();
            // clean up after outselves
            File cfgFile = standardLocator.configAt(ConfigLocation.APPDATA);
            assertTrue(
                    cfgFile.delete(),
                    String.format("Failed to delete config file %s after findOrCreateConfigWithFallback created it", cfgFile.getAbsolutePath())
            );
        });
    }

    @Test
    public void testCreateCopyError() {
        File templateFile = new ConfigFinder(getClass(), "jConfigFile_TestTemplate").configAt(ConfigLocation.APPDATA);
        String originalUserHome = System.getProperty("user.home");
        try {
            String fileId = hexString(64);
            Config template = new ConfigFile(templateFile);
            template.setKey("fileId", fileId);
            template.close();
            // Break USERPROFILE by redirecting it to an invalid path and ensure we get an error
            System.setProperty("user.home", "https://error-path");
            Config result = callWithInteractionsInHelper(SystemStreamTester.CREATE_COPY_ERROR_INTERACTIONS(ConfigLocation.USERPROFILE, templateFile.getPath()));
            assertInstanceOf(ConfigMap.class, result);
        } catch (InterruptedException | IOException ex) {
            fail(ex);
        } finally {
            // clean up after outselves
            System.setProperty("user.home", originalUserHome);
            templateFile.delete();
        }
    }

    @Test
    public void testInvalidLocationInput() {
        assertDoesNotThrow(() -> {
            // create file interactively
            Config result = callWithInteractionsInHelper(SystemStreamTester.CREATE_NEW_FILE_BAD_INPUT_ERROR_INTERACTIONS());
            assertInstanceOf(ConfigMap.class, result);
        });
    }

    @Test
    @EnumSource(ConfigLocation.class)
    public void testInvalidTemplateInput(ConfigLocation loc) {
        String[] invalidPaths = {
            standardLocator.configAt(loc).getParentFile().getParent(),
            "https://error-path"
        };
        for (String path : invalidPaths) {
            assertDoesNotThrow(() -> {
                // create file interactively
                Config result = callWithInteractionsInHelper(SystemStreamTester.CREATE_COPY_BAD_INPUT_ERROR_INTERACTIONS(loc, path));
                assertInstanceOf(ConfigMap.class, result);
            });
        }
    }

    private Config callWithInteractionsInHelper(Queue<SystemStreamTester.Interaction> interactions) throws InterruptedException {
        SystemStreamTester tester = new SystemStreamTester();
        tester.bindToSystemStreams();
        resultReady = false;
        InteractiveConfigInitializer.findOrCreateConfigAsyncWithFallback(getClass(), "jConfigFile_InteractiveConfigInitializerTest", (res) -> {
            helperResult = res;
            resultReady = true;
            synchronized (syncKey) {
                syncKey.notifyAll();
            }
        });
        assertTrue(tester.validateStreamedData(interactions));
        tester.unbindFromSystemStreams();
        synchronized (syncKey) {
            while (!resultReady) {
                syncKey.wait();
            }
        }
        return helperResult;
    }
}
