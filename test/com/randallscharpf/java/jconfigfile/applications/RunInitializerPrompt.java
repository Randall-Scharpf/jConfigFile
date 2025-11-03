package com.randallscharpf.java.jconfigfile.applications;

import com.randallscharpf.java.jconfigfile.ConfigFinder;
import com.randallscharpf.java.jconfigfile.ConfigInitializerPrompt;
import java.io.IOException;

public class RunInitializerPrompt {
    public static void main(String[] args) throws IOException {
        ConfigFinder thisApp = new ConfigFinder(RunInitializerPrompt.class, "RunInitializerPrompt");
        ConfigInitializerPrompt prompt = new ConfigInitializerPrompt(thisApp, System.in, System.out, System.err);
        prompt.getInitializedFile();
        new Thread(() -> {
            try { prompt.getInitializedFile(); } catch (Exception ex) { System.err.println("1"); }
        }).start();
        new Thread(() -> {
            try { prompt.getInitializedFile(); } catch (Exception ex) { System.err.println("2"); }
        }).start();
//        System.out.println("RESULT: " + prompt.getInitializedFile());
    }
}
