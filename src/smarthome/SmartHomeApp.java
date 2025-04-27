package smarthome;

import smarthome.system.SmartHomeSystem;
import smarthome.models.*;
import smarthome.exceptions.*;
import smarthome.SmartHomeGUI;

import java.util.Scanner;

public class SmartHomeApp {
    private static SmartHomeSystem system;
    private static Scanner scanner;
    
    public static void main(String[] args) {
        // Initialize the system
        system = SmartHomeSystem.getInstance();
        scanner = new Scanner(System.in);
        
        // For console testing before GUI launch
        boolean useConsole = false;
        
        if (useConsole) {
            runConsoleInterface();
        } else {
            // Initialize default devices
            initializeDefaultDevices();
            
            // Launch the GUI
            SmartHomeGUI gui = new SmartHomeGUI(system);
            gui.launch();
        }
    }
    
    private static void runConsoleInterface() {
        System.out.println("Welcome to Smart Home System");
        System.out.println("----------------------------");
        
        boolean running = true;
        while (running) {
            System.out.println("\nPlease login to continue:");
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            
            try {
                system.login(username, password);
                System.out.println("Login successful!");
                
                // Main menu after login
                showMainMenu();
                
            } catch (AuthenticationException e) {
                System.out.println("Login failed: " + e.getMessage());
            }
            
            System.out.print("\nDo you want to exit? (y/n): ");
            String exit = scanner.nextLine();
            if (exit.equalsIgnoreCase("y")) {
                running = false;
            }
        }
        
        System.out.println("Thank you for using Smart Home System. Goodbye!");
        scanner.close();
    }
    
    private static void showMainMenu() {
        boolean logout = false;
        
        while (!logout) {
            System.out.println("\n===== Main Menu =====");
            System.out.println("1. View All Devices");
            System.out.println("2. Control a Device");
            
            // Admin-only options
            if (system.getCurrentUser().getRole().equals("ADMIN")) {
                System.out.println("3. Add New Device");
                System.out.println("4. Remove Device");
                System.out.println("5. View System Logs");
            }
            
            System.out.println("0. Logout");
            
            System.out.print("\nEnter your choice: ");
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1":
                    viewAllDevices();
                    break;
                case "2":
                    controlDevice();
                    break;
                case "3":
                    if (system.getCurrentUser().getRole().equals("ADMIN")) {
                        addNewDevice();
                    } else {
                        System.out.println("Invalid option!");
                    }
                    break;
                case "4":
                    if (system.getCurrentUser().getRole().equals("ADMIN")) {
                        removeDevice();
                    } else {
                        System.out.println("Invalid option!");
                    }
                    break;
                case "5":
                    if (system.getCurrentUser().getRole().equals("ADMIN")) {
                        viewSystemLogs();
                    } else {
                        System.out.println("Invalid option!");
                    }
                    break;
                case "0":
                    system.logout();
                    logout = true;
                    System.out.println("Logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid option! Please try again.");
            }
        }
    }
    
    private static void viewAllDevices() {
        System.out.println("\n===== All Devices =====");
        
        for (Device device : system.getAllDevices()) {
            System.out.println(device);
        }
        
        if (system.getAllDevices().isEmpty()) {
            System.out.println("No devices found.");
        }
    }
    
    private static void controlDevice() {
        // Implementation omitted for brevity
    }
    
    private static void addNewDevice() {
        System.out.println("\n===== Add New Device =====");
        System.out.println("Select device type:");
        System.out.println("1. Light");
        System.out.println("2. Fan");
        System.out.println("3. Air Conditioner");
        System.out.println("4. Security System");
        System.out.println("0. Cancel");
        
        System.out.print("\nEnter your choice: ");
        String choice = scanner.nextLine();
        
        if (choice.equals("0")) {
            return;
        }
        
        System.out.print("Enter device name: ");
        String name = scanner.nextLine();
        
        System.out.print("Enter device location: ");
        String location = scanner.nextLine();
        
        try {
            Device newDevice = null;
            
            switch (choice) {
                case "1":
                    newDevice = new Light(name, location, system.getCurrentUser().getUsername());
                    break;
                case "2":
                    newDevice = new Fan(name, location, system.getCurrentUser().getUsername());
                    break;
                case "3":
                    newDevice = new AirConditioner(name, location, system.getCurrentUser().getUsername());
                    break;
                case "4":
                    newDevice = new SecuritySystem(name, location, system.getCurrentUser().getUsername());
                    break;
                default:
                    System.out.println("Invalid device type!");
                    return;
            }
            
            system.addDevice(newDevice);
            System.out.println("Device added successfully: " + newDevice.getName());
            
        } catch (AuthenticationException e) {
            System.out.println("Error adding device: " + e.getMessage());
        }
    }
    
    private static void removeDevice() {
        // Implementation omitted for brevity
    }
    
    private static void viewSystemLogs() {
        // Implementation omitted for brevity
    }
    
    private static void initializeDefaultDevices() {
        try {
            // Login as admin to add devices
            system.login("admin", "admin123");
            
            // Add some default devices
            system.addDevice(new Light("Living Room Light", "Living Room", "admin"));
            system.addDevice(new Fan("Bedroom Fan", "Bedroom", "admin"));
            system.addDevice(new AirConditioner("Living Room AC", "Living Room", "admin"));
            system.addDevice(new SecuritySystem("Main Security System", "Entrance", "admin"));
            
            // Logout
            system.logout();
        } catch (AuthenticationException e) {
            System.err.println("Error during initialization: " + e.getMessage());
        }
    }
}
