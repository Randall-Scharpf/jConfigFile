/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.randallscharpf.java.jconfigfile.unittest.interactiveconfiginitializertest;

import com.randallscharpf.java.jconfigfile.Config;
import com.randallscharpf.java.jconfigfile.ConfigFinder;
import com.randallscharpf.java.jconfigfile.ConfigInitializerDialog;
import com.randallscharpf.java.jconfigfile.ConfigLocation;
import com.randallscharpf.java.jconfigfile.FileSelectFrame;
import java.awt.Window;
import java.io.File;
import java.util.concurrent.Callable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ParallelGuiTester {
    
    private final ConfigFinder standardLocator;
    
    public ParallelGuiTester(ConfigFinder standardLocator) {
        this.standardLocator = standardLocator;
    }
    
    private Thread worker;
    private volatile boolean syncGotConfig;
    private volatile boolean workerSucceeded;
    private volatile String workerMessage;
    
    private Config lambda_result;
    
    private final int GUI_SYNC_DELAY = 500;

    private ConfigInitializerDialog getActiveConfigInitializerDialog() {
        for (Window w : Window.getWindows()) {
            if ((w.isShowing()) && (w instanceof ConfigInitializerDialog)) {
                return (ConfigInitializerDialog) w;
            }
        }
        return null;
    }
    
    private FileSelectFrame getActiveFileSelectFrame() {
        for (Window w : Window.getWindows()) {
            if ((w.isShowing()) && (w instanceof FileSelectFrame)) {
                return (FileSelectFrame) w;
            }
        }
        return null;
    }
    
    private void postWindowCloseEvent(Window frame) {
        java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(
                new java.awt.event.WindowEvent(frame, java.awt.event.WindowEvent.WINDOW_CLOSING)
        );
    }
    
    public Config callSyncExpectNoPopup(Callable<Config> testFunction) {
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

    public Config callSyncExpectPopup(
            ConfigLocation targetLocation, Callable<Config> testFunction,
            String templatePath, String action
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
                        switch (action) {
                            case "ApproveSelection":
                                guiFsf.approveSelection();
                                break;
                            case "CancelSelection":
                                guiFsf.cancelSelection();
                                Thread.sleep(GUI_SYNC_DELAY);
                                postWindowCloseEvent(gui);
                                break;
                            case "CloseWindow":
                                postWindowCloseEvent(guiFsf);
                                Thread.sleep(GUI_SYNC_DELAY);
                                postWindowCloseEvent(gui);
                                break;
                            default:
                                fail("Invalid action specified for file select dialog");
                                break;
                        }
                    } else {
                        switch (action) {
                            case "CreateNew":
                                gui.clickCreateNewButton();
                                break;
                            case "CreateCopy":
                                fail("Provide a templatePath to copy, then specify the action for the copy dialog");
                                break;
                            case "CloseWindow":
                                postWindowCloseEvent(gui);
                                break;
                            default:
                                fail("Invalid action specified for config initializer dialog");
                                break;
                        }
                    }
                }
            } catch (InterruptedException ex) {
                workerSucceeded = false;
                workerMessage = "Worker thread was interrupted";
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
    
    public void awaitAndAcknowledgeDialog() {
        new Thread(() -> {
            while (true) {
                for (Window w : Window.getWindows()) {
                    if ((w.isShowing()) && (w instanceof javax.swing.JDialog)) {
                        assertDoesNotThrow(() -> {
                            Thread.sleep(GUI_SYNC_DELAY);
                        });
                        postWindowCloseEvent(w);
                        return;
                    }
                }
            }
        }).start();
    }
    
}
