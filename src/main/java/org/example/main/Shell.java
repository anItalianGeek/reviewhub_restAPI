package org.example.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Shell extends Thread {
    
    @Override
    public void run() {
        // Create a new BufferedReader to read user input
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        // Create a new ProcessBuilder to execute shell commands
        ProcessBuilder processBuilder = new ProcessBuilder();

        while (true) {
            // Print the shell prompt
            System.out.print("shell> ");

            // Read the user input
            String input = null;
            try {
                input = reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Exit the shell if the user types 'exit'
            if (input.equals("exit")) {
                break;
            }

            // Execute the user input as a shell command
            processBuilder.command("bash", "-c", input);
            try {
                // Start the process and capture its output
                Process process = processBuilder.start();
                BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = outputReader.readLine()) != null) {
                    System.out.println(line);
                }
                outputReader.close();
            } catch (IOException e) {
                System.out.println("Error executing command: " + e.getMessage());
            }
        }
    }
    
}