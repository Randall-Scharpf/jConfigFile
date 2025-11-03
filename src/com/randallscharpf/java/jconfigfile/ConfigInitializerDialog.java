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
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Interactive GUI to allow a user to set up a configuration file in a standard location.
 * 
 * Allows the user to select an existing configuration file from an unsupported location
 * and copy it to a supported location or to create a new, blank configuration file in a
 * selected location. The API exposes methods to synchronously or asynchronously
 * request the result of the interactive operation, which is a {@link ConfigFile}.
 * The API also exposes methods to interacting with the GUI automatically for testing.
 */
public class ConfigInitializerDialog extends javax.swing.JFrame {
    
    /**
     * A {@link java.util.Map} with the text for each possible selection in the drop-down
     * box on the GUI and the associated {@link ConfigLocation}.
     * 
     * Includes a default text value shown to users who have not yet selected a location
     * which maps to {@code null} as well as a text value mapping to every declared
     * {@link ConfigLocation}.
     */
    private static final Map<String, ConfigLocation> comboBoxLocations;

    /**
     * A {@link java.util.List} of the keys in {@link #comboBoxLocations} in the order
     * they should appear to the user.
     */
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

    /**
     * Constructs a {@code ConfigInitializerDialog} for the application described
     * by the provided {@link ConfigFinder}.
     * 
     * The created dialog is initially set to hidden ({@code isVisible() == false}).
     * Instead of directly calling {@code setVisible(true)}, user code should call either
     * {@code getInitializedFile()} or {@code getInitializedFileAsync(callback)}, which sets up
     * internal state variables and callbacks to ensure user code receives exactly
     * one user selection once it is made before making the dialog visible itself.
     * 
     * @param finder {@link ConfigFinder} for the application which needs its configuration initialized
     */
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

    /**
     * Opens the dialog and returns immediately, then later calls a provided callback
     * with the configuration produced by the user's selection.
     * 
     * Once this method is called on a dialog, user code must wait until the callback
     * runs before calling it again or calling the synchronous version of this method.
     * An {@code IllegalStateException} will be thrown if user code does not adhere
     * to this requirement.
     * 
     * When the user selects and confirms the desired qualities of the configuration file
     * to initialize, the dialog will close and attempt to create a {@link ConfigFile}
     * corresponding to the selected characteristics.
     * <ul>
     * <li>
     *   If the attempt is successful, {@code callback} will be executed with the
     *   first parameter set to the created file and the second parameter set to {@code null}.
     * </li>
     * <li>
     *   If the attempt is unsuccessful, {@code callback} will be executed with the
     *   first parameter set to {@code null} and the second parameter set to an
     *   appropriate exception describing the failure.
     * </li>
     * <li>
     *   If the user closes the dialog without making any selection, {@code callback}
     *   will be executed with both parameters set to {@code null}.
     * </li>
     * </ul>
     * 
     * @param callback to be executed when the user interaction is complete
     * @throws IllegalStateException if the dialog is already being used to select a file
     */
    public void getInitializedFileAsync(BiConsumer<ConfigFile, IOException> callback) {
        synchronized (stateKey) {
            if (state != State.WAITING) {
                throw new IllegalStateException("This dialog is already being used to initialize a config file!");
            }
            state = State.CHOOSING_LOCATION;
        }
        this.callback = callback;
        java.awt.EventQueue.invokeLater(() -> {
            setInterfaceEnabled(true);
            this.setVisible(true);
        });
    }

    /**
     * Opens the dialog and blocks until user input, then returns the configuration
     * produced by the user's selection.
     * 
     * User code must not call this method from multiple threads at once, or call this
     * method while its asynchronous version is still awaiting user input.
     * An {@code IllegalStateException} will be thrown if user code does not adhere
     * to this requirement.
     * 
     * When the user selects and confirms the desired qualities of the configuration file
     * to initialize, the dialog will close and attempt to create a {@link ConfigFile}
     * corresponding to the selected characteristics. If the attempt is successful,
     * the configuration file will be returned, otherwise an exception is thrown.
     * If the user closes the dialog without making any selection, {@code null} will
     * be returned.
     * 
     * @return an initialized configuration file located according to user input
     * @throws IOException if initializing a configuration file as the user requests is impossible
     * @throws IllegalStateException if the dialog is already being used to select a file
     */
    public ConfigFile getInitializedFile() throws IOException {
        callbackRan = false;
        getInitializedFileAsync((res, err) -> {
            synchronized (syncKey) {
                result = res;
                error = err;
                callbackRan = true;
                syncKey.notifyAll();
            }
        });
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
        new Thread(() -> {
            try {
                ConfigFile cf = new ConfigFile(finder.configAt(choice));
                callback.accept(cf, null);
            } catch (IOException ex) {
                callback.accept(null, ex);
            }
        }).start();
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

    /**
     * Member variable referencing the constructor parameter {@code finder}.
     */
    private final ConfigFinder finder;

    /**
     * Object on which the current state of the object is synchronized, to ensure
     * correct behavior from multiple threads.
     */
    private final Object stateKey;
    /**
     * Current state of the dialog.
     * 
     * <table>
     * <caption>
     *   <b>Possible dialog states</b>
     * </caption>
     * <tr>
     *   <td><u>State</u></td>
     *   <td><u>Usage</u></td>
     * </tr>
     * <tr>
     *   <td>{@link State#WAITING}</td>
     *   <td>
     *     The dialog is hidden and waiting for a call to one of the {@code getInitializedFile} methods.
     *   </td>
     * </tr>
     * <tr>
     *   <td>{@link State#CHOOSING_LOCATION}</td>
     *   <td>
     *     The dialog is visible and waiting for the user to press one of the buttons
     *     or to close the dialog manually. A callback has been registered, and the
     *     {@code getInitializedFile} are not allowed to be called.
     *   </td>
     * </tr>
     * <tr>
     *   <td>{@link State#CHOOSING_COPY}</td>
     *   <td>
     *     The dialog is visible and has opened a {@link FileSelectFrame} where the user
     *     can select a file to copy to make the new configuration file. A callback
     *     has been registered, and the {@code getInitializedFile} are not allowed to
     *     be called.
     *   </td>
     * </tr>
     * </table>
     */
    private volatile State state;
    /**
     * Callback to run when the user takes appropriate action.
     * 
     * This variable's value must not be used when {@code this.state == State.WAITING}.
     */
    private BiConsumer<ConfigFile, IOException> callback;

    /**
     * Object which is waited on in order to implement {@link #getInitializedFile}
     * with a call to {@link #getInitializedFileAsync}.
     */
    private final Object syncKey;
    /**
     * Internal shared memory used to transfer the result of an internally-registered
     * callback to user code which behaves synchronously.
     */
    private volatile ConfigFile result;
    /**
     * Internal shared memory used to transfer an exception generated by an internally-registered
     * callback to user code which behaves synchronously.
     */
    private volatile IOException error;
    /**
     * Internal shared memory used to detect if an internally-registered callback
     * has run from within the thread used by user code.
     */
    private volatile boolean callbackRan;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    /**
    * Button for users to confirm the selected location and request
    * that a new, blank configuration file be created there.
    */
    private javax.swing.JButton createBlankButton;
    /**
    * Button for users to confirm the selected location and request
    * that an existing configuration file be copied there. A dialog
    * will open to ask the user what existing configuration file to
    * use.
    */
    private javax.swing.JButton createCopyButton;
    /**
    * First line of prompt text.
    */
    private javax.swing.JLabel jLabel1;
    /**
    * Second line of prompt text.
    */
    private javax.swing.JLabel jLabel2;
    /**
    * Dropdown menu which is used to select a configuration
    * location.
    */
    private javax.swing.JComboBox<String> locationComboBox;
    /**
    * Text field to indicate what absolute path corresponds to the
    * selected configuration location. Also shows help to the user
    * when they have interacted with the GUI incorrectly. Paths are
    * shown in default black text, while errors are shown in red.
    */
    private javax.swing.JTextField pathTextField;
    // End of variables declaration//GEN-END:variables

    // API to operate the GUI from another Java class

    /**
     * Clicks the GUI button labeled "Create New Blank Config File". The click is
     * invoked asynchronously on the AWT EventQueue.
     */
    public void clickCreateNewButton() {
        java.awt.EventQueue.invokeLater(() -> {
            createBlankButton.doClick();
        });
    }

    /**
     * Clicks the GUI button labeled "Create Config File from Existing File". The
     * click is invoked asynchronously on the AWT EventQueue.
     */
    public void clickCreateCopyButton() {
        java.awt.EventQueue.invokeLater(() -> {
            createCopyButton.doClick();
        });
    }

    /**
     * Sets the selected item in the dropdown menu of possible config locations.
     * The selection is invoked asynchronously on the AWT EventQueue.
     * 
     * @param location the new dropdown menu selection
     */
    public void setDropdownSelection(ConfigLocation location) {
        java.awt.EventQueue.invokeLater(() -> {
            for (Map.Entry<String, ConfigLocation> entry : comboBoxLocations.entrySet()) {
                if (entry.getValue() == location) {
                    locationComboBox.setSelectedItem(entry.getKey());
                }
            }
        });
    }

    /**
     * Determines the text currently displayed in the text field on the GUI. This
     * method first waits for all events on the AWT EventQueue to be processed, then
     * it returns its value.
     * 
     * @return path displayed on the GUI, if one has been selected
     * @throws InterruptedException if this thread is interrupted before all events
     *                              on the AWT EventQueue are processed
     */
    public String getPreviewPath() throws InterruptedException {
        try {
            java.awt.EventQueue.invokeAndWait(() -> {});
        } catch (java.lang.reflect.InvocationTargetException ex) {
            // not possible for an empty runnable to throw an exception
        }
        return pathTextField.getText();
    }

}
