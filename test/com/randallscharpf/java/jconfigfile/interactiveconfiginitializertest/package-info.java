/**
 * Tests for com.randallscharpf.java.jconfigfile.InteractiveConfigInitializer and,
 * by thin proxy, com.randallscharpf.java.jconfigfile.ConfigInitializerDialog.
 * 
 * Separate test classes exist for the CLI and GUI environments, since the behavior
 * of the {@code InteractiveConfigInitializer} depends on whether or not the JDK
 * executing the tests is headless. Because the test cases for each method are complex,
 * each method's test cases are organized into a separate test class.
 * 
 * User input and file system state determine a variety of possible interaction flows
 * for any particular interactive method, each of which is tested:
 * 
 * <ul>
 * <li>
 * If the configuration file already exists, no user interaction to select a config
 * file is necessary. Otherwise, the user must select an option.
 * </li>
 * <li>
 * The user can select the "Create New Blank Config File" option in a file system
 * where it is not possible to create the file corresponding to the selected location.
 * </li>
 * <li>
 * The user can select the "Create New Blank Config File" option and then successfully
 * add the new file to the file system.
 * </li>
 * <li>
 * The user can use the X button in the corner of the dialog to close the window
 * and cancel configuration file creation.
 * </li>
 * <li>
 * The user can select the "Create Config File from Existing File" option and then
 * press the "Open" button to select a file to copy in a file system where it is not
 * possible to copy the file to the desired location.
 * </li>
 * <li>
 * The user can select the "Create Config File from Existing File" option and then
 * press the "Open" button to successfully copy a selected file to the desired location
 * in the file system.
 * </li>
 * <li>
 * The user can select the "Create Config File from Existing File" option and then
 * press the "Cancel" button on the secondary dialog that opens.
 * </li>
 * <li>
 * The user can select the "Create Config File from Existing File" option and then
 * press the X button on the corner of the secondary dialog that opens to close it.
 * </li>
 * </ul>
 * 
 * Note that these tests use assertions and test fixtures from jUnit and are executed
 * as unit tests, but really serve as end-to-end tests of the library that operate
 * through its typical API entry points.
 */
package com.randallscharpf.java.jconfigfile.interactiveconfiginitializertest;
