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
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisabledIf("java.awt.GraphicsEnvironment#isHeadless")
@Timeout(value = 10, unit = TimeUnit.SECONDS)
public class FindOrCreateConfigAsyncWithFallbackTestGui {

    private final ConfigFinder standardLocator;
    private final ParallelGuiTester guiTester;
    
    private Config secondCallReturnValue;
    
    private final Object callbackLock;
    private volatile boolean callbackRan;
    private final Object innerCallbackLock;
    private volatile boolean innerCallbackRan;

    public FindOrCreateConfigAsyncWithFallbackTestGui() {
        standardLocator = new ConfigFinder(getClass(), "jConfigFile_InteractiveConfigInitializerTest");
        guiTester = new ParallelGuiTester(standardLocator);
        callbackLock = new Object();
        innerCallbackLock = new Object();
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

    // === TESTS WHICH DON'T REQUIRE USER INTERACTION ===

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
            cfg = guiTester.callSyncExpectNoPopup(() -> {
                callbackRan = false;
                InteractiveConfigInitializer.findOrCreateConfigAsyncWithFallback(
                        getClass(),
                        "jConfigFile_InteractiveConfigInitializerTest",
                        (res) -> {
                            assertEquals(fileId, res.getKeyOrDefault("fileId", ""));
                            assertEquals(1, res.getKeys().size());
                            assertDoesNotThrow(res::close);
                            // clean up generated files
                            f.delete();
                            synchronized(callbackLock) {
                                callbackRan = true;
                                callbackLock.notifyAll();
                            }
                        }
                );
                synchronized (callbackLock) {
                    while (!callbackRan) {
                        callbackLock.wait();
                    }
                }
                return null;
            });
            assertNull(cfg);
        });
    }

    // === TESTS WHICH REQUIRE USER INTERACTION WITH ONE POP-UP WINDOW ===

    @ParameterizedTest
    @EnumSource(ConfigLocation.class)
    public void testCreateNewFile(ConfigLocation loc) {
        assertDoesNotThrow(() -> {
            String fileId = hexString(64);
            secondCallReturnValue = new ConfigMap();
            Config cfg = guiTester.callSyncExpectPopup(loc, () -> {
                callbackRan = false;
                InteractiveConfigInitializer.findOrCreateConfigAsyncWithFallback(
                        getClass(),
                        "jConfigFile_InteractiveConfigInitializerTest",
                        (res) -> {
                            res.setKey("fileId", fileId);
                            assertDoesNotThrow(res::close);
                            secondCallReturnValue = guiTester.callSyncExpectNoPopup(() -> {
                                innerCallbackRan = false;
                                InteractiveConfigInitializer.findOrCreateConfigAsyncWithFallback(
                                        getClass(),
                                        "jConfigFile_InteractiveConfigInitializerTest",
                                        (res2) -> {
                                            assertEquals(fileId, res2.getKeyOrDefault("fileId", ""));
                                            assertEquals(1, res2.getKeys().size());
                                            assertDoesNotThrow(res2::close);
                                            // clean up after outselves
                                            File cfgFile = standardLocator.configAt(loc);
                                            assertTrue(
                                                    cfgFile.delete(),
                                                    String.format(
                                                            "Failed to delete config file %s after findOrCreateConfigAsyncWithFallback created it",
                                                            cfgFile.getAbsolutePath()
                                                    )
                                            );
                                            synchronized (innerCallbackLock) {
                                                innerCallbackRan = true;
                                                innerCallbackLock.notifyAll();
                                            }
                                        }
                                );
                                synchronized (innerCallbackLock) {
                                    while (!innerCallbackRan) {
                                        innerCallbackLock.wait();
                                    }
                                }
                                return null;
                            });
                            synchronized (callbackLock) {
                                callbackRan = true;
                                callbackLock.notifyAll();
                            }
                        }
                );
                synchronized (callbackLock) {
                    assertFalse(callbackRan);
                    while (!callbackRan) {
                        callbackLock.wait();
                    }
                }
                return null;
            }, null, "CreateNew");
            assertNull(cfg);
            assertNull(secondCallReturnValue);
        });
    }

    @Test
    public void testCloseDialogWithoutSelection() {
        assertDoesNotThrow(() -> {
            guiTester.awaitAndAcknowledgeDialog();
            Config cfg = guiTester.callSyncExpectPopup(ConfigLocation.APPDATA, () -> {
                callbackRan = false;
                InteractiveConfigInitializer.findOrCreateConfigAsyncWithFallback(
                        getClass(),
                        "jConfigFile_InteractiveConfigInitializerTest",
                        (res) -> {
                            assertInstanceOf(ConfigMap.class, res);
                            synchronized (callbackLock) {
                                callbackRan = true;
                                callbackLock.notifyAll();
                            }
                        }
                );
                synchronized (callbackLock) {
                    assertFalse(callbackRan);
                    while (!callbackRan) {
                        callbackLock.wait();
                    }
                }
                return null;
            }, null, "CloseWindow");
            assertNull(cfg);
        });
    }
 
    @Test
    public void testFileCreationError() {
        String originalUserHome = System.getProperty("user.home");
        try {
            // Break USERPROFILE by redirecting it to an invalid path and ensure we get an error
            System.setProperty("user.home", "https://error-path");
            guiTester.awaitAndAcknowledgeDialog();
            Config cfg = guiTester.callSyncExpectPopup(ConfigLocation.USERPROFILE, () -> {
                callbackRan = false;
                InteractiveConfigInitializer.findOrCreateConfigAsyncWithFallback(
                        getClass(),
                        "jConfigFile_InteractiveConfigInitializerTest",
                        (res) -> {
                            assertInstanceOf(ConfigMap.class, res);
                            synchronized (callbackLock) {
                                callbackRan = true;
                                callbackLock.notifyAll();
                            }
                        }
                );
                synchronized (callbackLock) {
                    assertFalse(callbackRan);
                    while (!callbackRan) {
                        callbackLock.wait();
                    }
                }
                return null;
            }, null, "CreateNew");
            assertNull(cfg);
        } finally {
            System.setProperty("user.home", originalUserHome);
        }
    }

    // === TESTS WHICH REQUIRE USER INTERACTION WITH BOTH ConfigInitializerDialog AND FileSelectFrame WINDOWS ===

    @ParameterizedTest
    @EnumSource(ConfigLocation.class)
    public void testCreateCopyAndConfirmSelectedTemplate(ConfigLocation loc) {
        assertDoesNotThrow(() -> {
            File templateFile = new ConfigFinder(getClass(), "jConfigFile_TestTemplate").configAt(ConfigLocation.APPDATA);
            String fileId = hexString(64);
            Config template = new ConfigFile(templateFile);
            template.setKey("fileId", fileId);
            template.close();
            secondCallReturnValue = new ConfigMap();
            Config cfg = guiTester.callSyncExpectPopup(loc, () -> {
                callbackRan = false;
                InteractiveConfigInitializer.findOrCreateConfigAsyncWithFallback(
                        getClass(),
                        "jConfigFile_InteractiveConfigInitializerTest",
                        (res) -> {
                            assertEquals(fileId, res.getKeyOrDefault("fileId", ""));
                            assertEquals(1, res.getKeys().size());
                            assertDoesNotThrow(res::close);
                            secondCallReturnValue = guiTester.callSyncExpectNoPopup(() -> {
                                innerCallbackRan = false;
                                InteractiveConfigInitializer.findOrCreateConfigAsyncWithFallback(
                                        getClass(),
                                        "jConfigFile_InteractiveConfigInitializerTest",
                                        (res2) -> {
                                            assertEquals(fileId, res2.getKeyOrDefault("fileId", ""));
                                            assertEquals(1, res2.getKeys().size());
                                            assertDoesNotThrow(res2::close);
                                            // clean up after outselves
                                            File cfgFile = standardLocator.configAt(loc);
                                            assertTrue(
                                                    cfgFile.delete(),
                                                    String.format(
                                                            "Failed to delete config file %s after findOrCreateConfigAsyncWithFallback created it",
                                                            cfgFile.getAbsolutePath()
                                                    )
                                            );
                                            templateFile.delete();
                                            synchronized (innerCallbackLock) {
                                                innerCallbackRan = true;
                                                innerCallbackLock.notifyAll();
                                            }
                                        }
                                );
                                synchronized (innerCallbackLock) {
                                    while(!innerCallbackRan) {
                                        innerCallbackLock.wait();
                                    }
                                }
                                return null;
                            });
                            synchronized (callbackLock) {
                                callbackRan = true;
                                callbackLock.notifyAll();
                            }
                        }
                );
                synchronized (callbackLock) {
                    assertFalse(callbackRan);
                    while (!callbackRan) {
                        callbackLock.wait();
                    }
                }
                return null;
            }, templateFile.getPath(), "ApproveSelection");
            assertNull(cfg);
            assertNull(secondCallReturnValue);
        });
    }
    
    @Test
    public void testCreateCopyAndCancelSelection() {
        assertDoesNotThrow(() -> {
            File templateFile = new ConfigFinder(getClass(), "jConfigFile_TestTemplate").configAt(ConfigLocation.APPDATA);
            String fileId = hexString(64);
            Config template = new ConfigFile(templateFile);
            template.setKey("fileId", fileId);
            template.close();
            guiTester.awaitAndAcknowledgeDialog();
            Config cfg = guiTester.callSyncExpectPopup(ConfigLocation.APPDATA, () -> {
                callbackRan = false;
                InteractiveConfigInitializer.findOrCreateConfigAsyncWithFallback(
                        getClass(),
                        "jConfigFile_InteractiveConfigInitializerTest",
                        (res) -> {
                            assertInstanceOf(ConfigMap.class, res);
                            synchronized (callbackLock) {
                                callbackRan = true;
                                callbackLock.notifyAll();
                            }
                        }
                );
                synchronized (callbackLock) {
                    assertFalse(callbackRan);
                    while (!callbackRan) {
                        callbackLock.wait();
                    }
                }
                return null;
            }, templateFile.getPath(), "CancelSelection");
            assertNull(cfg);
            // clean up after outselves
            templateFile.delete();
        });
    }

    @Test
    public void testCreateCopyAndCloseDialogWithoutSelection() {
        assertDoesNotThrow(() -> {
            File templateFile = new ConfigFinder(getClass(), "jConfigFile_TestTemplate").configAt(ConfigLocation.APPDATA);
            String fileId = hexString(64);
            Config template = new ConfigFile(templateFile);
            template.setKey("fileId", fileId);
            template.close();
            guiTester.awaitAndAcknowledgeDialog();
            Config cfg = guiTester.callSyncExpectPopup(ConfigLocation.APPDATA, () -> {
                callbackRan = false;
                InteractiveConfigInitializer.findOrCreateConfigAsyncWithFallback(
                        getClass(),
                        "jConfigFile_InteractiveConfigInitializerTest",
                        (res) -> {
                            assertInstanceOf(ConfigMap.class, res);
                            synchronized (callbackLock) {
                                callbackRan = true;
                                callbackLock.notifyAll();
                            }
                        }
                );
                synchronized (callbackLock) {
                    assertFalse(callbackRan);
                    while (!callbackRan) {
                        callbackLock.wait();
                    }
                }
                return null;
            }, templateFile.getPath(), "CloseWindow");
            assertNull(cfg);
            // clean up after outselves
            templateFile.delete();
        });
    }

    @Test
    public void testCreateCopyFileCreationError() {
        File templateFile = new ConfigFinder(getClass(), "jConfigFile_TestTemplate").configAt(ConfigLocation.APPDATA);
        String fileId = hexString(64);
        assertDoesNotThrow(() -> {
            Config template = new ConfigFile(templateFile);
            template.setKey("fileId", fileId);
            template.close();
        });
        String originalUserHome = System.getProperty("user.home");
        try {
            // Break USERPROFILE by redirecting it to an invalid path and ensure we get an error
            System.setProperty("user.home", "https://error-path");
            guiTester.awaitAndAcknowledgeDialog();
            Config cfg = guiTester.callSyncExpectPopup(ConfigLocation.USERPROFILE, () -> {
                callbackRan = false;
                InteractiveConfigInitializer.findOrCreateConfigAsyncWithFallback(
                        getClass(),
                        "jConfigFile_InteractiveConfigInitializerTest",
                        (res) -> {
                            assertInstanceOf(ConfigMap.class, res);
                            synchronized (callbackLock) {
                                callbackRan = true;
                                callbackLock.notifyAll();
                            }
                        }
                );
                synchronized (callbackLock) {
                    assertFalse(callbackRan);
                    while (!callbackRan) {
                        callbackLock.wait();
                    }
                }
                return null;
            }, templateFile.getPath(), "ApproveSelection");
            assertNull(cfg);
        } finally {
            System.setProperty("user.home", originalUserHome);
        }
    }

}