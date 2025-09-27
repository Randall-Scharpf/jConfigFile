/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.randallscharpf.java.jconfigfile.applications;

import com.randallscharpf.java.jconfigfile.Config;
import com.randallscharpf.java.jconfigfile.ConfigFile;
import com.randallscharpf.java.jconfigfile.ConfigFinder;
import java.io.IOException;
import java.util.Scanner;

public class RunningAverage {
    public static void main(String[] args) throws IOException {
        // read configured parameters for program execution
        Config cfg = new ConfigFile(new ConfigFinder(RunningAverage.class, "RunningAverage").searchForConfig());
        int count = Integer.parseInt(cfg.getKeyOrDefault("count", "0"));
        double lastAverage = Double.parseDouble(cfg.getKeyOrDefault("avg", "0"));
        
        // execute program
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter an integer:");
        int in = sc.nextInt();
        double newAverage = (in + lastAverage * count) / (count + 1);
        count++;
        System.out.printf("Running Average: %.4f\n", newAverage);
        
        // update settings for next program execution, then save and close config file
        cfg.setKey("count", String.valueOf(count));
        cfg.setKey("avg", String.format("%.6f", newAverage));
        cfg.close();
    }
}
