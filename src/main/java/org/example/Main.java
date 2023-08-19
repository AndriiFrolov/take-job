package org.example;


import org.example.exceptions.FormValidationFailed;
import org.example.model.Settings;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws FormValidationFailed, IOException {
        Settings settings = new Settings(50, 48, 500);

        JobFinder jobFinder = new JobFinder(settings);
        jobFinder.startScanning();
    }
}