package com.randallscharpf.java.jconfigfile.applications;

import com.randallscharpf.java.jconfigfile.ConfigFile;
import com.randallscharpf.java.jconfigfile.ConfigFinder;
import com.randallscharpf.java.jconfigfile.ConfigInitializerPrompt;
import java.io.IOException;
import java.util.Scanner;

public class TestCli {
    private static volatile boolean quit = false;
    private static volatile IOException exitReason = null;

    public static void main(String[] args) throws IOException {
        ConfigFinder thisApp = new ConfigFinder(TestCli.class, "jConfigFile_TestCli");
        ConfigFile cfg;
        if (thisApp.searchForConfig().exists()) {
            cfg = new ConfigFile(thisApp.searchForConfig());
        } else {
            ConfigInitializerPrompt prompt = new ConfigInitializerPrompt(thisApp, System.in, System.out, System.err);
            cfg = prompt.getInitializedFile();
        }

        if (cfg != null) {
            System.out.println("Starting CLI for jConfigFile...");
            new Thread(() -> {
                try {
                    while (!quit) {
                            cfg.save();
                    }
                    cfg.close();
                } catch (IOException ex) {
                    exitReason = ex;
                    quit = true;
                }
            }).start();
            Scanner sc = new Scanner(System.in);
            while (!quit) {
                System.out.print("> ");
                String command = sc.nextLine();
                String[] tokens = command.split("\\s+");
                switch (tokens[0]) {
                    case "READ":
                        if (tokens.length == 2) {
                            if (cfg.getKeys().contains(tokens[1])) {
                                System.out.printf("Value `%s` found for key `%s`%n", cfg.getKeyOrDefault(tokens[1], ""), tokens[1]);
                            } else {
                                System.out.printf("No value found for key `%s`%n", tokens[1]);
                            }
                        } else {
                            System.err.printf("Invalid use of READ command: expected 1 argument, but got %d%n", tokens.length - 1);
                            System.err.println("Usage: READ key");
                        }
                        break;
                    case "WRITE":
                        if (tokens.length == 3) {
                            System.out.printf("Wrote value `%s` for key `%s`%n", tokens[2], tokens[1]);
                            cfg.setKey(tokens[1], tokens[2]);
                        } else {
                            System.err.printf("Invalid use of WRITE command: expected 2 arguments, but got %d%n", tokens.length - 1);
                            System.err.println("Usage: WRITE key value");
                        }
                        break;
                    case "EXIT":
                        quit = true;
                        break;
                    default:
                        System.err.printf("Invalid command `%s` received: valid commands are READ, WRITE, and EXIT%n", tokens[0]);
                        break;
                }
            }
            if (exitReason != null) {
                throw exitReason;
            }
        }
    }
}
