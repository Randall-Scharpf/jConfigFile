package com.randallscharpf.java.jconfigfile;

import java.awt.Container;
import java.io.File;
import javax.swing.filechooser.FileFilter;
import java.util.function.Consumer;
import javax.swing.AbstractButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.text.JTextComponent;

/**
 * Interactive GUI to select a file.
 * 
 * Wraps a {@link JFileChooser} in a {@link JFrame}. Includes a simple asynchronous
 * API for user code to invoke the frame and methods for automated testing.
 */
public class FileSelectFrame extends JFrame {

    /**
     * Constructs a {@code FileSelectFrame} with the selected mode that allows all
     * files to pass its filter.
     * 
     * @param mode whether files, directories, or both are allowed by this file selector
     */
    public FileSelectFrame(Mode mode) {
        this(new FileFilter() {
            @Override public boolean accept(File f) { return true; }
            @Override public String getDescription() { return mode.getDescription(); }
        }, mode);
    }
 
    /**
     * Constructs a {@code FileSelectFrame} with the selected mode and custom filter.
     * 
     * @param filter a custom filter to only display specific files to the user
     * @param mode whether files, directories, or both are allowed by this file selector
     */
    public FileSelectFrame(FileFilter filter, Mode mode) {
        this.filter = filter;
        this.mode = mode;
        this.stateLock = new Object();
        this.open = false;
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser1 = new javax.swing.JFileChooser();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Select a File");
        setIconImage(java.awt.Toolkit.getDefaultToolkit().createImage(getClass().getResource("/com/randallscharpf/java/jconfigfile/file_folder.png")));
        setMinimumSize(new java.awt.Dimension(520, 346));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jFileChooser1.setFileFilter(filter);
        jFileChooser1.setFileSelectionMode(mode.getAssociatedInteger());
        jFileChooser1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFileChooser1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jFileChooser1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jFileChooser1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jFileChooser1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFileChooser1ActionPerformed
        boolean runCallback = false;
        switch (evt.getActionCommand()) {
            case "CancelSelection":
                callbackArg = null;
                runCallback = true;
                dispose();
                break;
            case "ApproveSelection":
                callbackArg = jFileChooser1.getSelectedFile();
                runCallback = true;
                dispose();
                break;
        }
        if (runCallback) {
            new Thread(() -> {
                Consumer<File> callbackCopy = callback;
                synchronized (stateLock) {
                    if (!this.open) {
                        return;
                    }
                    this.open = false;
                }
                callbackCopy.accept(callbackArg);
            }).start();
        }
    }//GEN-LAST:event_jFileChooser1ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        jFileChooser1ActionPerformed(new java.awt.event.ActionEvent(
                jFileChooser1,
                java.awt.event.ActionEvent.ACTION_PERFORMED,
                JFileChooser.CANCEL_SELECTION,
                System.currentTimeMillis(),
                java.awt.event.MouseEvent.BUTTON1_MASK
        ));
    }//GEN-LAST:event_formWindowClosing

    /**
     * Opens the dialog and returns immediately, then later calls a provided callback
     * with the file selected by a user.
     * 
     * Once this method is called on a dialog, user code must wait until the callback
     * runs before calling it again. An {@code IllegalStateException} will be thrown
     * if user code does not adhere to this requirement.
     * 
     * When the user selects and confirms a file, the dialog will close and invoke the
     * callback with the selected file. If the window is closed or the operation
     * is cancelled without a file being selected, the callback will still be invoked
     * but its parameter will be {@code null}.
     * 
     * @param callback to be executed when the user interaction is complete
     */
    public void selectFile(Consumer<File> callback) {
        synchronized (stateLock) {
            if (this.open) {
                throw new IllegalStateException("A file is already being selected!");
            }
            this.open = true;
        }
        this.callback = callback;
        java.awt.EventQueue.invokeLater(() -> {
            setVisible(true);
        });
    }

    /**
     * Callback to run when the user takes appropriate action.
     * 
     * This variable's value must not be used when {@code this.open == false}.
     */
    private Consumer<File> callback;
    /**
     * Internal shared memory used to transfer the argument from the AWT EventQueue
     * to the independent thread where it runs.
     * 
     * The callback must not run on the EventQueue because it may contain arbitrary
     * user code which blocks indefinitely.
     */
    private File callbackArg;

    /**
     * {@code FileFilter} passed to this object's constructor.
     */
    private final FileFilter filter;
    /**
     * {@code Mode} passed to this object's constructor.
     */
    private final Mode mode;

    /**
     * Object which is synchronized on to prevent read-write conflicts on the value
     * of {@link #open} as the window is opened or closed.
     */
    private final Object stateLock;
    /**
     * Whether this window is currently being used to select a file.
     * 
     * Loosely tracks {@link #isVisible}, but is thread-safe.
     */
    private volatile boolean open;

    /**
     * Modes which can be used to open the {@link JFileChooser} wrapped by this frame.
     * 
     * Each mode here maps to a mode integer used by {@link JFileChooser#setFileSelectionMode}.
     * This enum is used to provide a description to each mode integer and to enforce
     * type safety.
     */
    public enum Mode {
        /**
         * Corresponds to {@link JFileChooser#FILES_ONLY}.
         */
        FILES_ONLY(JFileChooser.FILES_ONLY, "All Files"),
        /**
         * Corresponds to {@link JFileChooser#FILES_AND_DIRECTORIES}.
         */
        FILES_AND_DIRECTORIES(JFileChooser.FILES_AND_DIRECTORIES, "All Files and Directories"),
        /**
         * Corresponds to {@link JFileChooser#DIRECTORIES_ONLY}.
         */
        DIRECTORIES_ONLY(JFileChooser.DIRECTORIES_ONLY, "Directories");
        
        private final int i;
        private final String desc;
        private Mode(int i, String desc) {
            this.i = i;
            this.desc = desc;
        }
        private int getAssociatedInteger() {
            return i;
        }
        // description of the set of files a frame in this mode can select BEFORE a file filter is added
        private String getDescription() {
            return desc;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    /**
    * The file selector wrapped by this window.
    */
    private javax.swing.JFileChooser jFileChooser1;
    // End of variables declaration//GEN-END:variables

    // API to operate the GUI from another Java class
    
    private AbstractButton findButton(Container container, String text) {
        for (java.awt.Component component : container.getComponents()) {
            if (component instanceof AbstractButton) {
                if (text.equals(((AbstractButton) component).getText())) {
                    return (AbstractButton) component;
                }
            }
            if (component instanceof Container) {
                AbstractButton find = findButton((Container) component, text);
                if (find != null) {
                    return find;
                }
            }
        }
        return null;
    }
    
    private JTextComponent findTextInput(Container container) {
        for (java.awt.Component component : container.getComponents()) {
            if (component instanceof JTextComponent) {
                if (((JTextComponent) component).isEditable()) {
                    return (JTextComponent) component;
                }
            }
            if (component instanceof Container) {
                JTextComponent find = findTextInput((Container) component);
                if (find != null) {
                    return find;
                }
            }
        }
        return null;
    }

    /**
     * Clicks the GUI button labeled "Cancel". The click is invoked asynchronously
     * on the AWT EventQueue.
     */
    public void cancelSelection() {
        java.awt.EventQueue.invokeLater(() -> {
            findButton(this, "Cancel").doClick();
        });
    }

    /**
     * Clicks the GUI button labeled "Open". The click is invoked asynchronously
     * on the AWT EventQueue.
     */
    public void approveSelection() {
        java.awt.EventQueue.invokeLater(() -> {
            findButton(this, "Open").doClick();
        });
    }

    /**
     * Sets the file selected by the GUI.
     * 
     * This method does not interact with the graphical file tree components, rather
     * it simply simulates typing in an absolute file path to the file selector.
     * The text update occurs asynchronously on the AWT EventQueue.
     * 
     * @param f the file whose path will be entered into the GUI
     */
    public void setSelectedFile(File f) {
        java.awt.EventQueue.invokeLater(() -> {
            JTextComponent fileInput = findTextInput(this);
            fileInput.setText(f.getAbsolutePath());
        });
    }

}
