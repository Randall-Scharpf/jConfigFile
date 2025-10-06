/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.randallscharpf.java.jconfigfile;

import java.awt.Container;
import java.io.File;
import javax.swing.filechooser.FileFilter;
import java.util.function.Consumer;
import javax.swing.AbstractButton;
import javax.swing.JFileChooser;
import javax.swing.text.JTextComponent;

public class FileSelectFrame extends javax.swing.JFrame {

    public FileSelectFrame(Mode mode) {
        this(new FileFilter() {
            @Override public boolean accept(File f) { return true; }
            @Override public String getDescription() { return mode.getDescription(); }
        }, mode);
    }
    
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
        synchronized (stateLock) {
            if (!this.open) {
                return;
            }
            this.open = false;
        }
        new Thread(() -> {
            switch (evt.getActionCommand()) {
                case "CancelSelection":
                    dispose();
                    callback.accept(null);
                    break;
                case "ApproveSelection":
                    dispose();
                    callback.accept(jFileChooser1.getSelectedFile());
                    break;
            }
        }).start();
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
    
    private Consumer<File> callback;
    private final FileFilter filter;
    private final Mode mode;
    private final Object stateLock;
    private volatile boolean open;
    public enum Mode {
        FILES_ONLY(JFileChooser.FILES_ONLY, "All Files"),
        FILES_AND_DIRECTORIES(JFileChooser.FILES_AND_DIRECTORIES, "All Files and Directories"),
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
    
    public void cancelSelection() {
        java.awt.EventQueue.invokeLater(() -> {
            findButton(this, "Cancel").doClick();
        });
    }
    
    public void approveSelection() {
        java.awt.EventQueue.invokeLater(() -> {
            findButton(this, "Open").doClick();
        });
    }
    
    public void setSelectedFile(File f) {
        java.awt.EventQueue.invokeLater(() -> {
            JTextComponent fileInput = findTextInput(this);
            fileInput.setText(f.getAbsolutePath());
        });
    }

}
