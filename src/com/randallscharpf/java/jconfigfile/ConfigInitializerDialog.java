/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.randallscharpf.java.jconfigfile;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ConfigInitializerDialog extends javax.swing.JFrame {
    
    private static final Map<String, ConfigLocation> comboBoxLocations;
    private static final List<String> comboBoxLocationsOrdered;
    
    static {
        comboBoxLocations = new HashMap<>(6);
        comboBoxLocations.put("Choose Location",    null);
        comboBoxLocations.put("Appdata",            ConfigLocation.APPDATA);
        comboBoxLocations.put("Documents",          ConfigLocation.DOCUMENTS);
        comboBoxLocations.put("User Profile",       ConfigLocation.USERPROFILE);
        comboBoxLocations.put("Adjacent",           ConfigLocation.SIBLING);
        comboBoxLocations.put("/etc Folder",        ConfigLocation.ETC);
        comboBoxLocationsOrdered = new ArrayList<>(6);
        comboBoxLocationsOrdered.add("Choose Location");
        comboBoxLocationsOrdered.add("Appdata");
        comboBoxLocationsOrdered.add("Documents");
        comboBoxLocationsOrdered.add("User Profile");
        comboBoxLocationsOrdered.add("Adjacent");
        comboBoxLocationsOrdered.add("/etc Folder");
    }

    public ConfigInitializerDialog(ConfigFinder finder) {
        this.finder = finder;
        this.syncKey = new Object();
        this.stateKey = new Object();
        this.state = State.WAITING;
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        locationComboBox = new javax.swing.JComboBox<>();
        pathTextField = new javax.swing.JTextField();
        createBlankButton = new javax.swing.JButton();
        createCopyButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Choose Config File Location");
        setIconImage(java.awt.Toolkit.getDefaultToolkit().createImage(getClass().getResource("/com/randallscharpf/java/jconfigfile/icon.png")));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jLabel1.setText("No config file was found in any valid location!");

        jLabel2.setText("A new config file will be created at the selected location:");

        locationComboBox.setModel(new javax.swing.DefaultComboBoxModel<String>(comboBoxLocationsOrdered.toArray(new String[0])));
        locationComboBox.setSelectedItem("Choose Location");
        locationComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                locationComboBoxItemStateChanged(evt);
            }
        });
        locationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                locationComboBoxActionPerformed(evt);
            }
        });

        pathTextField.setEditable(false);
        pathTextField.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        pathTextField.setText("< Choose Location >");

        createBlankButton.setText("Create New Blank Config File");
        createBlankButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createBlankButtonActionPerformed(evt);
            }
        });

        createCopyButton.setText("Create Config File from Existing File");
        createCopyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createCopyButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(createBlankButton, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(createCopyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addGap(18, 18, 18)
                        .addComponent(locationComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(pathTextField))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, 0)
                        .addComponent(jLabel2))
                    .addComponent(locationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createBlankButton)
                    .addComponent(createCopyButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void setInterfaceEnabled(boolean enabled) {
        locationComboBox.setEnabled(enabled);
        createBlankButton.setEnabled(enabled);
        createCopyButton.setEnabled(enabled);
    }
    
    private void closeSelf() {
        setVisible(false);
        new Thread(() -> {
            dispose();
        }).start();
    }
    
    public void getInitializedFileAsync(BiConsumer<ConfigFile, IOException> callback) {
        synchronized (stateKey) {
            if (state != State.WAITING) {
                throw new IllegalStateException("This dialog is already being used to initialize a config file!");
            }
            state = State.CHOOSING_LOCATION;
        }
        this.callback = callback;
        setInterfaceEnabled(true);
        this.setVisible(true);
    }
    
    public ConfigFile getInitializedFile() throws IOException {
        synchronized (stateKey) {
            if (state != State.WAITING) {
                throw new IllegalStateException("This dialog is already being used to initialize a config file!");
            }
            state = State.CHOOSING_LOCATION;
        }
        result = null;
        error = null;
        this.callback = (res, err) -> {
            synchronized (syncKey) {
                result = res;
                error = err;
                callbackRan = true;
                syncKey.notifyAll();
            }
        };
        callbackRan = false;
        setInterfaceEnabled(true);
        setVisible(true);
        try {
            synchronized (syncKey) {
                while (!callbackRan) {
                    syncKey.wait();
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        if (error == null) {
            return result;
        } else {
            throw error;
        }
    }
    
    private void createBlankButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createBlankButtonActionPerformed
        ConfigLocation choice;
        synchronized (stateKey) {
            if (state != State.CHOOSING_LOCATION) {
                // ignore button press, either the window is closed or the dialog
                // is selecting a file to copy, so we won't interrupt that process
                return;
            }
            choice = comboBoxLocations.get(locationComboBox.getItemAt(locationComboBox.getSelectedIndex()));
            if (choice != null) {
                state = State.WAITING;
                setVisible(false);
                closeSelf();
            } else {
                pathTextField.setText("ERROR: Choose a location before continuing!");
                pathTextField.setForeground(Color.red);
                return;
            }
        }
        try {
            ConfigFile cf = new ConfigFile(finder.configAt(choice));
            callback.accept(cf, null);
        } catch (IOException ex) {
            callback.accept(null, ex);
        }
    }//GEN-LAST:event_createBlankButtonActionPerformed

    private void createCopyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createCopyButtonActionPerformed
        ConfigLocation choice;
        synchronized (stateKey) {
            if (state != State.CHOOSING_LOCATION) {
                // ignore button press, either the window is closed or we're already
                // selecting a file to copy
                return;
            }
            choice = comboBoxLocations.get(locationComboBox.getItemAt(locationComboBox.getSelectedIndex()));
            if (choice != null) {
                state = state = State.CHOOSING_COPY;
            } else {
                pathTextField.setText("ERROR: Choose a location before continuing!");
                pathTextField.setForeground(Color.red);
                return;
            }
        }
        setInterfaceEnabled(false);
        FileSelectFrame fsf = new FileSelectFrame(new FileNameExtensionFilter(".ini files", "ini"), FileSelectFrame.Mode.FILES_ONLY);
        fsf.setLocationRelativeTo(this);
        fsf.selectFile((oldfile) -> {
            if (oldfile == null) {
                state = State.CHOOSING_LOCATION;
                setInterfaceEnabled(true);
            } else {
                File newfile = finder.configAt(choice);
                try {
                    newfile.getParentFile().mkdirs();
                    Files.copy(oldfile.toPath(), newfile.toPath());
                    ConfigFile cf_new = new ConfigFile(newfile);
                    closeSelf();
                    callback.accept(cf_new, null);
                } catch (IOException | java.nio.file.InvalidPathException ex) {
                    closeSelf();
                    callback.accept(null, new IOException(ex));
                }
            }
        });
    }//GEN-LAST:event_createCopyButtonActionPerformed

    private void locationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_locationComboBoxActionPerformed
        ConfigLocation choice = comboBoxLocations.get(locationComboBox.getItemAt(locationComboBox.getSelectedIndex()));
        if (choice == null) {
            pathTextField.setText("< Choose Location >");
        } else {
            pathTextField.setText(finder.configAt(choice).getAbsolutePath());
        }
        pathTextField.setForeground(Color.black);
    }//GEN-LAST:event_locationComboBoxActionPerformed

    private void locationComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_locationComboBoxItemStateChanged
        locationComboBoxActionPerformed(null);
    }//GEN-LAST:event_locationComboBoxItemStateChanged

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        synchronized (stateKey) {
            if (state != State.CHOOSING_LOCATION) {
                // ignore button press, either the window is closed or the dialog
                // is selecting a file to copy, so we won't interrupt that process
                return;
            }
            state = State.WAITING;
            setVisible(false);
            closeSelf();
        }
        callback.accept(null, null);
    }//GEN-LAST:event_formWindowClosing
    
    private enum State {
        WAITING,
        CHOOSING_LOCATION,
        CHOOSING_COPY
    }
    
    private final ConfigFinder finder;
    
    private final Object stateKey;
    private volatile State state;
    private BiConsumer<ConfigFile, IOException> callback;
    
    private final Object syncKey;
    private volatile ConfigFile result;
    private volatile IOException error;
    private volatile boolean callbackRan;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton createBlankButton;
    private javax.swing.JButton createCopyButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JComboBox<String> locationComboBox;
    private javax.swing.JTextField pathTextField;
    // End of variables declaration//GEN-END:variables

    // API to operate the GUI from another Java class

    public void clickCreateNewButton() {
        createBlankButtonActionPerformed(null);
    }
    
    public void clickCreateCopyButton() {
        createCopyButtonActionPerformed(null);
    }
    
    public void setDropdownSelection(ConfigLocation location) {
        boolean succeeded = false;
        while (!succeeded) {
            try {
                for (Map.Entry<String, ConfigLocation> entry : comboBoxLocations.entrySet()) {
                    if (entry.getValue() == location) {
                        locationComboBox.setSelectedItem(entry.getKey());
                    }
                }
                succeeded = true;
            } catch (NullPointerException ex1) {
                // API call came in so fast that the GUI wasn't ready yet
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex2) {
                    throw new RuntimeException(ex2);
                }
            }
        }
    }

    public String getPreviewPath() {
        while (true) {
            try {
                return pathTextField.getText();
            } catch (NullPointerException ex1) {
                // API call came in so fast that the GUI wasn't ready yet
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex2) {
                    throw new RuntimeException(ex2);
                }
            }
        }
    }

}
