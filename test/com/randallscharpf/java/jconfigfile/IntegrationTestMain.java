/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.randallscharpf.java.jconfigfile;

import com.randallscharpf.java.jconfigfile.interactiveconfiginitializertest.FindOrCreateConfigAsyncTestHeadless;
import com.randallscharpf.java.jconfigfile.interactiveconfiginitializertest.FindOrCreateConfigAsyncWithFallbackTestHeadless;
import com.randallscharpf.java.jconfigfile.interactiveconfiginitializertest.FindOrCreateConfigTestHeadless;
import com.randallscharpf.java.jconfigfile.interactiveconfiginitializertest.FindOrCreateConfigWithFallbackTestHeadless;
import java.io.IOException;
import java.io.PrintStream;

public class IntegrationTestMain {
    public static void main(String[] args) throws IOException {
        // TODO: add integration tests
        System.out.println("INTEGRATION TEST");
        PrintStream err = System.err;
        ConfigFinder finder = new ConfigFinder(IntegrationTestMain.class, "jConfigFile_InteractiveConfigInitializerTest");

        FindOrCreateConfigAsyncTestHeadless tester = new FindOrCreateConfigAsyncTestHeadless();
        tester.setUpTest();
        tester.testFileAlreadyExists(ConfigLocation.SIBLING);
        tester.testCreateNewFile(ConfigLocation.SIBLING);
        tester.testCancelWithoutSelection();
        tester.testFileCreationError();
        tester.testCreateCopy(ConfigLocation.SIBLING);
        tester.testCancelFromCopyRequest();
        tester.testCancelCopyAndContinue();
        tester.testCreateCopyError();
        tester.testInvalidLocationInput();
        tester.testInvalidTemplateInput(ConfigLocation.SIBLING);

//        ConfigInitializerPrompt prompt = new ConfigInitializerPrompt(finder, System.in, System.out, System.err);
//        System.out.println(prompt.getInitializedFile());
    }
}

