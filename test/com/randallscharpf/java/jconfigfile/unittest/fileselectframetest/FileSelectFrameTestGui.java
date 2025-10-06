/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.randallscharpf.java.jconfigfile.unittest.fileselectframetest;

import com.randallscharpf.java.jconfigfile.FileSelectFrame;
import java.io.File;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledIf;

@DisabledIf("java.awt.GraphicsEnvironment#isHeadless")
@Timeout(value = 10, unit = TimeUnit.SECONDS)
public class FileSelectFrameTestGui {
    
    private FileSelectFrame uut;
    private File testFile1;
    private File testFile2;
    private File testDir;
    
    private final Object syncLock = new Object();
    private volatile boolean callbackRan = false;
    
    private final int GUI_SYNC_DELAY = 500;
    
    public FileSelectFrameTestGui() {
    }
    
    @BeforeAll
    public void setUpClass() {
        testFile1 = new File(System.getProperty("user.home"), "FileSelectFrame_testfile1");
        testFile2 = new File(System.getProperty("user.home"), "FileSelectFrame_testfile2");
        testDir  = new File(System.getProperty("user.home"), "FileSelectFrame_testdir");
        assertDoesNotThrow(() -> { testFile1.createNewFile(); });
        assertDoesNotThrow(() -> { testFile2.createNewFile(); });
        assertDoesNotThrow(() -> { testDir.mkdirs(); });
    }
    
    @AfterAll
    public void tearDownClass() {
        assertTrue(testFile1.delete());
        assertTrue(testFile2.delete());
        assertTrue(testDir.delete());
    }

    @Test
    public void testFilesOnly() {
        uut = new FileSelectFrame(FileSelectFrame.Mode.FILES_ONLY);
        uut.selectFile((file) -> {
            assertEquals(testFile1, file);
            synchronized (syncLock) {
                callbackRan = true;
                syncLock.notifyAll();
            }
        });
        assertDoesNotThrow(() -> {
            Thread.sleep(GUI_SYNC_DELAY);
            uut.setSelectedFile(testDir);
            Thread.sleep(GUI_SYNC_DELAY);
            uut.approveSelection();
            Thread.sleep(GUI_SYNC_DELAY);
            uut.setSelectedFile(testFile1);
            Thread.sleep(GUI_SYNC_DELAY);
            uut.approveSelection();
            synchronized (syncLock) {
                while (!callbackRan) {
                    syncLock.wait();
                }
            }
        });
    }
    
    @Test
    public void testFoldersOnly() {
        uut = new FileSelectFrame(FileSelectFrame.Mode.DIRECTORIES_ONLY);
        uut.selectFile((file) -> {
            assertEquals(null, file);
            synchronized (syncLock) {
                callbackRan = true;
                syncLock.notifyAll();
            }
        });
        assertDoesNotThrow(() -> {
            Thread.sleep(GUI_SYNC_DELAY);
            uut.setSelectedFile(testFile1);
            Thread.sleep(GUI_SYNC_DELAY);
            uut.approveSelection();
            synchronized (syncLock) {
                while (!callbackRan) {
                    syncLock.wait();
                }
            }
        });
        uut.selectFile((file) -> {
            assertEquals(testDir, file);
            synchronized (syncLock) {
                callbackRan = true;
                syncLock.notifyAll();
            }
        });
        assertDoesNotThrow(() -> {
            Thread.sleep(GUI_SYNC_DELAY);
            uut.setSelectedFile(testDir);
            Thread.sleep(GUI_SYNC_DELAY);
            uut.approveSelection();
            synchronized (syncLock) {
                while (!callbackRan) {
                    syncLock.wait();
                }
            }
        });
    }
    
    @Test
    public void testFilesAndFolders() {
        uut = new FileSelectFrame(FileSelectFrame.Mode.FILES_AND_DIRECTORIES);
        uut.selectFile((file) -> {
            assertEquals(testFile1, file);
            synchronized (syncLock) {
                callbackRan = true;
                syncLock.notifyAll();
            }
        });
        assertDoesNotThrow(() -> {
            Thread.sleep(GUI_SYNC_DELAY);
            uut.setSelectedFile(testFile1);
            Thread.sleep(GUI_SYNC_DELAY);
            uut.approveSelection();
            synchronized (syncLock) {
                while (!callbackRan) {
                    syncLock.wait();
                }
            }
        });
        uut.selectFile((file) -> {
            assertEquals(testDir, file);
            synchronized (syncLock) {
                callbackRan = true;
                syncLock.notifyAll();
            }
        });
        assertDoesNotThrow(() -> {
            Thread.sleep(GUI_SYNC_DELAY);
            uut.setSelectedFile(testDir);
            Thread.sleep(GUI_SYNC_DELAY);
            uut.approveSelection();
            synchronized (syncLock) {
                while (!callbackRan) {
                    syncLock.wait();
                }
            }
        });
    }
    
    @Test
    public void testCancel() {
        uut = new FileSelectFrame(FileSelectFrame.Mode.FILES_AND_DIRECTORIES);
        uut.selectFile((file) -> {
            assertEquals(null, file);
            synchronized (syncLock) {
                callbackRan = true;
                syncLock.notifyAll();
            }
        });
        assertDoesNotThrow(() -> {
            Thread.sleep(GUI_SYNC_DELAY);
            uut.setSelectedFile(testFile1);
            Thread.sleep(GUI_SYNC_DELAY);
            uut.cancelSelection();
            synchronized (syncLock) {
                while (!callbackRan) {
                    syncLock.wait();
                }
            }
        });
    }
    
    @Test
    public void testCloseWindow() {
        uut = new FileSelectFrame(FileSelectFrame.Mode.FILES_AND_DIRECTORIES);
        uut.selectFile((file) -> {
            assertEquals(null, file);
            synchronized (syncLock) {
                callbackRan = true;
                syncLock.notifyAll();
            }
        });
        assertDoesNotThrow(() -> {
            Thread.sleep(GUI_SYNC_DELAY);
            uut.setSelectedFile(testFile1);
            Thread.sleep(GUI_SYNC_DELAY);
            java.awt.EventQueue.invokeLater(() -> {
                java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(
                        new java.awt.event.WindowEvent(uut, java.awt.event.WindowEvent.WINDOW_CLOSING)
                );
            });
            synchronized (syncLock) {
                while (!callbackRan) {
                    syncLock.wait();
                }
            }
        });
    }
}
