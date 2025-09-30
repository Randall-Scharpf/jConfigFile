/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.randallscharpf.java.jconfigfile.unittest;

import com.randallscharpf.java.jconfigfile.Config;
import com.randallscharpf.java.jconfigfile.ConfigFile;
import com.randallscharpf.java.jconfigfile.ConfigFinder;
import com.randallscharpf.java.jconfigfile.ConfigInitializerDialog;
import com.randallscharpf.java.jconfigfile.ConfigLocation;
import com.randallscharpf.java.jconfigfile.FileSelectFrame;
import com.randallscharpf.java.jconfigfile.InteractiveConfigInitializer;

import java.awt.Window;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

// which method to use?
// - findOrCreateConfig
// - findOrCreateConfigAsync
// - findOrCreateConfigWithFallback
// - findOrCreateConfigAsyncWithFallback
// what's the preexisting state?
// - there's a file (in LOCATION, error not possible to manifest)
// - there's no file: use dialog
// what can I do with a dialog
// - new file (in LOCATION or USERPROFILE with error)
// - close dialog
// - copy file (in LOCATION or USERPROFILE with error)
// what can i do with copy dialog
// - close copy dialog
// - cancel selection
// - select location

// four methods * (preexisting * loc, new * loc, newerr, copy * loc, copyerr,

@Timeout(value = 10, unit = TimeUnit.SECONDS)
public class InteractiveConfigInitializerTest {

    private final ConfigFinder standardLocator;

    private Thread worker;
    private volatile boolean syncGotConfig;
    private volatile boolean workerSucceeded;
    private volatile String workerMessage;
    
    private Config lambda_result;
    
    private static final int GUI_SYNC_DELAY = 500;

    public InteractiveConfigInitializerTest() {
        standardLocator = new ConfigFinder(getClass(), "jConfigFile_InteractiveConfigInitializerTest");
    }
    
    private static ConfigInitializerDialog getActiveConfigInitializerDialog() {
        for (Window w : Window.getWindows()) {
            if ((w.isShowing()) && (w instanceof ConfigInitializerDialog)) {
                return (ConfigInitializerDialog) w;
            }
        }
        return null;
    }
    
    private static FileSelectFrame getActiveFileSelectFrame() {
        for (Window w : Window.getWindows()) {
            if ((w.isShowing()) && (w instanceof FileSelectFrame)) {
                return (FileSelectFrame) w;
            }
        }
        return null;
    }
    
    private String hexString(int length) {
        String randomHex = "";
        for (int i = 0; i < 64; i++) {
            randomHex += String.format("%x", (int) (Math.random() * 16));
        }
        return randomHex;
    }

    private Config callSyncExpectNoPopup(Callable<Config> testFunction) {
        // start a worker thread so that if a dialog gets erroneously opened, the test doesn't hang
        worker = new Thread(() -> {
            workerSucceeded = true;
            // wait for window to be visible or for findOrCreateConfig to terminate otherwise
            try {
                while (!syncGotConfig && (getActiveConfigInitializerDialog() == null)) {
                    Thread.sleep(1);
                }
                Thread.sleep(GUI_SYNC_DELAY);
                // ensure that the file from before was found; cancel the test if another dialog opened
                ConfigInitializerDialog gui = getActiveConfigInitializerDialog();
                if (gui != null) {
                    workerSucceeded = false;
                    workerMessage = "findorCreateConfig created a dialog for an already-existing config file";
                    // close the dialog so we don't hang
                    gui.setDropdownSelection(ConfigLocation.APPDATA); // just pick a default, we've already failed the test
                    Thread.sleep(GUI_SYNC_DELAY);
                    gui.clickCreateNewButton();
                }
            } catch (InterruptedException ex) {
                workerSucceeded = false;
                workerMessage = "Worker thread was interrupted";
                return;
            }
        });
        // main thread calls findOrCreateConfig to synchronously get config file
        assertDoesNotThrow(() -> {
            syncGotConfig = false;
            worker.start();
            lambda_result = testFunction.call();
            assertNull(getActiveConfigInitializerDialog());
            syncGotConfig = true;
            while (worker.isAlive()) {
                Thread.sleep(1);
            }
            assertTrue(workerSucceeded, workerMessage);
            });
        return lambda_result;
    }
    
    private Config callSyncExpectPopup(ConfigLocation targetLocation, Callable<Config> testFunction) {
        return callSyncExpectPopupAndCopy(targetLocation, testFunction, null, null);
    }

    private Config callSyncExpectPopupAndCopy(
            ConfigLocation targetLocation, Callable<Config> testFunction,
            String templatePath, String copyAction
    ) {
        // start a worker thread to poke the API for the dialog that will get opened
        worker = new Thread(() -> {
            workerSucceeded = true;
            // wait for window to be visible or for findOrCreateConfig to terminate otherwise
            try {
                while (!syncGotConfig && (getActiveConfigInitializerDialog() == null)) {
                    Thread.sleep(1);
                }
                Thread.sleep(GUI_SYNC_DELAY);
                // ensure the condition to continue was that a window got opened
                ConfigInitializerDialog gui = getActiveConfigInitializerDialog();
                if (gui == null) {
                    workerSucceeded = false;
                    workerMessage = "findOrCreateConfig created a new config without user input";
                } else {
                    // select "new" mode in dialog with location we're currently testing
                    gui.setDropdownSelection(targetLocation);
                    Thread.sleep(GUI_SYNC_DELAY);
                    String previewPath = gui.getPreviewPath();
                    String expectedPath = standardLocator.configAt(targetLocation).getAbsolutePath();
                    if (!gui.getPreviewPath().equals(expectedPath)) {
                        workerSucceeded = false;
                        workerMessage = String.format("Dialog preview path %s did not match expected path %s", previewPath, expectedPath);
                    }
                    if (templatePath != null) {
                        gui.clickCreateCopyButton();
                        // wait for window to be visible or for findOrCreateConfig to terminate otherwise
                        while (!syncGotConfig && (getActiveFileSelectFrame() == null)) {
                            Thread.sleep(1);
                        }
                        Thread.sleep(GUI_SYNC_DELAY);
                        FileSelectFrame guiFsf = getActiveFileSelectFrame();
                        guiFsf.setSelectedFile(new File(templatePath));
                        Thread.sleep(GUI_SYNC_DELAY);
                        guiFsf.approveSelection();
                    } else {
                        gui.clickCreateNewButton();
                    }
                }
            } catch (InterruptedException ex) {
                workerSucceeded = false;
                workerMessage = "Worker thread was interrupted";
                return;
            }
        });
        assertDoesNotThrow(() -> {
            // main thread calls findOrCreateConfig to synchronously get config file
            syncGotConfig = false;
            worker.start();
            lambda_result = testFunction.call();
            assertNull(getActiveConfigInitializerDialog());
            syncGotConfig = true;
            while (worker.isAlive()) {
                Thread.sleep(1);
            }
            assertTrue(workerSucceeded, workerMessage);
        });
        return lambda_result;
    }
    
    @BeforeEach
    public void setUpTest() {
        for (ConfigLocation loc : ConfigLocation.values()) {
            standardLocator.configAt(loc).delete();
        }
    }
    
    @ParameterizedTest
    @EnumSource(ConfigLocation.class)
    public void testSyncNoFallbackPreexisting(ConfigLocation loc) {
        // create a unique file for the location we're testing
        String fileId = hexString(64);
        File f = standardLocator.configAt(loc);
        assertDoesNotThrow(() -> {
            Config cfg = new ConfigFile(f);
            cfg.setKey("fileId", fileId);
            cfg.close();
            // make sure InteractiveConfigInitializer picks up the correct file
            cfg = callSyncExpectNoPopup(() -> {
                return InteractiveConfigInitializer.findOrCreateConfig(getClass(), "jConfigFile_InteractiveConfigInitializerTest");
            });
            assertEquals(fileId, cfg.getKeyOrDefault("fileId", ""));
            assertEquals(1, cfg.getKeys().size());
            cfg.close();
            // clean up generated files
            f.delete();
        });
    }

    @ParameterizedTest
    @EnumSource(ConfigLocation.class)
    public void testSyncNoFallbackNew(ConfigLocation loc) {
        String fileId = hexString(64);
        assertDoesNotThrow(() -> {
            Config cfg = callSyncExpectPopup(loc, () -> {
                return InteractiveConfigInitializer.findOrCreateConfig(getClass(), "jConfigFile_InteractiveConfigInitializerTest");
            });
            cfg.setKey("fileId", fileId);
            cfg.close();
            cfg = callSyncExpectNoPopup(() -> {
                return InteractiveConfigInitializer.findOrCreateConfig(getClass(), "jConfigFile_InteractiveConfigInitializerTest");
            });
            assertEquals(fileId, cfg.getKeyOrDefault("fileId", ""));
            assertEquals(1, cfg.getKeys().size());
            cfg.close();
            // clean up after outselves
            File cfgFile = standardLocator.configAt(loc);
            assertTrue(
                    cfgFile.delete(),
                    String.format("Failed to delete config file %s after findOrCreateConfig created it", cfgFile.getAbsolutePath())
            );
        });
    }

    @ParameterizedTest
    @EnumSource(ConfigLocation.class)
    public void testSyncNoFallbackCopyApprove(ConfigLocation loc) {
        File templateFile = new ConfigFinder(getClass(), "jConfigFile_TestTemplate").configAt(ConfigLocation.APPDATA);
        String fileId = hexString(64);
        assertDoesNotThrow(() -> {
            Config template = new ConfigFile(templateFile);
            template.setKey("fileId", fileId);
            template.close();
            Config cfg = callSyncExpectPopupAndCopy(loc, () -> {
                return InteractiveConfigInitializer.findOrCreateConfig(getClass(), "jConfigFile_InteractiveConfigInitializerTest");
            }, templateFile.getPath(), "ApproveSelection");
            assertEquals(fileId, cfg.getKeyOrDefault("fileId", ""));
            assertEquals(1, cfg.getKeys().size());
            cfg.close();
            cfg = callSyncExpectNoPopup(() -> {
                return InteractiveConfigInitializer.findOrCreateConfig(getClass(), "jConfigFile_InteractiveConfigInitializerTest");
            });
            assertEquals(fileId, cfg.getKeyOrDefault("fileId", ""));
            assertEquals(1, cfg.getKeys().size());
            cfg.close();
            // clean up after outselves
            File cfgFile = standardLocator.configAt(loc);
            assertTrue(
                    cfgFile.delete(),
                    String.format("Failed to delete config file %s after findOrCreateConfig created it", cfgFile.getAbsolutePath())
            );
            templateFile.delete();
        });
    }
    
    @Test
    public void testSyncNoFallbackErr() {
        // Break USERPROFILE by redirecting it to an invalid path and ensure we get an error
        String originalUserHome = System.getProperty("user.home");
        System.setProperty("user.home", "https://error-path");
        Config cfg = callSyncExpectPopup(ConfigLocation.USERPROFILE, () -> {
            assertThrows(java.io.IOException.class, () -> {
                InteractiveConfigInitializer.findOrCreateConfig(getClass(), "jConfigFile_InteractiveConfigInitializerTest");
            });
            return null;
        });
        assertNull(cfg);
        System.setProperty("user.home", originalUserHome);
    }

}