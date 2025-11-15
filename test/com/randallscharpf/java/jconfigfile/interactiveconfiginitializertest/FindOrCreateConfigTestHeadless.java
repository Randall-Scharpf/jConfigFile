/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.randallscharpf.java.jconfigfile.interactiveconfiginitializertest;

import com.randallscharpf.java.jconfigfile.ConfigFile;
import com.randallscharpf.java.jconfigfile.ConfigFinder;
import com.randallscharpf.java.jconfigfile.ConfigLocation;
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
public class FindOrCreateConfigTestHeadless {

    private final ConfigFinder standardLocator;
    private final Object syncKey;
    private volatile boolean readyForCall;
    private volatile boolean resultReady;
    private volatile boolean helperResult;

    public FindOrCreateConfigTestHeadless() {
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
            ConfigFile cfg = new ConfigFile(f);
            cfg.setKey("fileId", fileId);
            cfg.close();
            // make sure InteractiveConfigInitializer picks up the correct file
            cfg = InteractiveConfigInitializer.findOrCreateConfig(getClass(), "jConfigFile_InteractiveConfigInitializerTest");
            assertEquals(fileId, cfg.getKeyOrDefault("fileId", ""));
            assertEquals(1, cfg.getKeys().size());
            cfg.close();
            // clean up generated files
            f.delete();
        });
    }

    @ParameterizedTest
    @EnumSource(ConfigLocation.class)
    public void testCreateNewFile(ConfigLocation loc) {
        assertDoesNotThrow(() -> {
            // create file interactively
            ConfigFile cfg = callWithInteractionsInHelper(SystemStreamTester.CREATE_NEW_FILE_INTERACTIONS(loc));
            String fileId = hexString(64);
            cfg.setKey("fileId", fileId);
            cfg.close();
            // re-open file to ensure persistence
            cfg = InteractiveConfigInitializer.findOrCreateConfig(getClass(), "jConfigFile_InteractiveConfigInitializerTest");
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
            ConfigFile result = callWithInteractionsInHelper(SystemStreamTester.CANCEL_INTERACTIONS());
            assertNull(result);
        });
    }

    @Test
    public void testFileCreationError() {
        String originalUserHome = System.getProperty("user.home");
        try {
            // Break USERPROFILE by redirecting it to an invalid path and ensure we get an error
            System.setProperty("user.home", "https://error-path");
            callWithInteractionsInHelper(SystemStreamTester.CREATE_NEW_FILE_INTERACTIONS(ConfigLocation.USERPROFILE));
            fail("Expected IO exception to be thrown");
        } catch (InterruptedException ex) {
            fail(ex);
        } catch (IOException ex) {
            // this is the expected behavior, test case passes and is done
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
            ConfigFile template = new ConfigFile(templateFile);
            template.setKey("fileId", fileId);
            template.close();
            // create file interactively
            ConfigFile cfg = callWithInteractionsInHelper(SystemStreamTester.CREATE_COPY_INTERACTIONS(loc, templateFile.getPath()));
            assertEquals(fileId, cfg.getKeyOrDefault("fileId", ""));
            assertEquals(1, cfg.getKeys().size());
            cfg.close();
            // re-open file to ensure persistence
            cfg = InteractiveConfigInitializer.findOrCreateConfig(getClass(), "jConfigFile_InteractiveConfigInitializerTest");
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
            ConfigFile result = callWithInteractionsInHelper(SystemStreamTester.CREATE_COPY_AND_CANCEL_INTERACTIONS(ConfigLocation.APPDATA));
            assertNull(result);
        });
    }

    @Test
    public void testCancelCopyAndContinue() {
        assertDoesNotThrow(() -> {
            // create file interactively
            ConfigFile cfg = callWithInteractionsInHelper(SystemStreamTester.CREATE_COPY_AND_CONTINUE_INTERACTIONS(ConfigLocation.APPDATA));
            String fileId = hexString(64);
            cfg.setKey("fileId", fileId);
            cfg.close();
            // re-open file to ensure persistence
            cfg = InteractiveConfigInitializer.findOrCreateConfig(getClass(), "jConfigFile_InteractiveConfigInitializerTest");
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
            ConfigFile template = new ConfigFile(templateFile);
            template.setKey("fileId", fileId);
            template.close();
            // Break USERPROFILE by redirecting it to an invalid path and ensure we get an error
            System.setProperty("user.home", "https://error-path");
            callWithInteractionsInHelper(SystemStreamTester.CREATE_COPY_INTERACTIONS(ConfigLocation.USERPROFILE, templateFile.getPath()));
            fail("Expected IO exception to be thrown");
        } catch (InterruptedException ex) {
            fail(ex);
        } catch (IOException ex) {
            // this is the expected behavior, test case passes and is done
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
            ConfigFile result = callWithInteractionsInHelper(SystemStreamTester.CREATE_NEW_FILE_BAD_INPUT_INTERACTIONS());
            assertNull(result);
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
                ConfigFile result = callWithInteractionsInHelper(SystemStreamTester.CREATE_COPY_BAD_INPUT_INTERACTIONS(loc, path));
                assertNull(result);
            });
        }
    }

    private ConfigFile callWithInteractionsInHelper(Queue<SystemStreamTester.Interaction> interactions) throws InterruptedException, IOException {
        readyForCall = false;
        resultReady = false;
        new Thread(() -> {
            SystemStreamTester tester = new SystemStreamTester();
            tester.bindToSystemStreams();
            synchronized (syncKey) {
                readyForCall = true;
                syncKey.notifyAll();
            }
            helperResult = tester.validateStreamedData(interactions);
            tester.unbindFromSystemStreams();
            synchronized (syncKey) {
                resultReady = true;
                syncKey.notifyAll();
            }
        }).start();
        synchronized (syncKey) {
            while (!readyForCall) {
                syncKey.wait();
            }
        }
        ConfigFile result = InteractiveConfigInitializer.findOrCreateConfig(getClass(), "jConfigFile_InteractiveConfigInitializerTest");
        synchronized (syncKey) {
            while (!resultReady) {
                syncKey.wait();
            }
        }
        assertTrue(helperResult);
        return result;
    }
}