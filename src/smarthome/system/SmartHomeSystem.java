package smarthome.system;

import smarthome.models.*;
import smarthome.exceptions.*;
import smarthome.interfaces.Switchable;

import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SmartHomeSystem {
    private static SmartHomeSystem instance;
    private Map<String, Device> devices;
    private Map<String, User> users;
    private User currentUser;
    private boolean systemOn;
    private List<String> systemLogs;
    
    private SmartHomeSystem() {
        devices = new HashMap<>();
        users = new HashMap<>();
        systemOn = false;
        systemLogs = new ArrayList<>();
        
        // Add admin user by default
        users.put("admin", new AdminUser("admin", "admin123"));
        
        // Add a regular user for testing
        users.put("user", new User("user", "user123"));
        
        logSystemEvent("System initialized on " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    
    public static SmartHomeSystem getInstance() {
        if (instance == null) {
            instance = new SmartHomeSystem();
        }
        return instance;
    }
    
    public void turnSystemOn() {
        this.systemOn = true;
        
        // Start all devices with default settings
        for (Device device : devices.values()) {
            if (device instanceof Switchable) {
                ((Switchable) device).turnOn();
                device.setToDefaultSettings();
            }
        }
        
        logSystemEvent("System turned ON by " + (currentUser != null ? currentUser.getUsername() : "SYSTEM"));
    }
    
    public void turnSystemOff() {
        this.systemOn = false;
        
        // Turn off all devices
        for (Device device : devices.values()) {
            if (device instanceof Switchable) {
                ((Switchable) device).turnOff();
            }
        }
        
        logSystemEvent("System turned OFF by " + (currentUser != null ? currentUser.getUsername() : "SYSTEM"));
    }
    
    public boolean isSystemOn() {
        return systemOn;
    }
    
    public void addDevice(Device device) throws AuthenticationException {
        // Check if user has permission to add devices
        if (currentUser == null) {
            throw new AuthenticationException("User not authenticated");
        }
        
        if (currentUser.hasPermission("ADD_DEVICE")) {
            devices.put(device.getId(), device);
            logSystemEvent("Device added: " + device.getName() + " by " + currentUser.getUsername());
        } else {
            throw new AuthenticationException("User does not have permission to add devices");
        }
    }
    
    public void removeDevice(String deviceId) throws DeviceNotFoundException, AuthenticationException {
        // Check if user has permission to remove devices
        if (currentUser == null) {
            throw new AuthenticationException("User not authenticated");
        }
        
        if (currentUser.hasPermission("REMOVE_DEVICE")) {
            if (devices.containsKey(deviceId)) {
                Device removed = devices.remove(deviceId);
                logSystemEvent("Device removed: " + removed.getName() + " by " + currentUser.getUsername());
            } else {
                throw new DeviceNotFoundException("Device with ID " + deviceId + " not found.");
            }
        } else {
            throw new AuthenticationException("User does not have permission to remove devices currently");
        }
    }
    
    public Device getDevice(String deviceId) throws DeviceNotFoundException {
        if (devices.containsKey(deviceId)) {
            return devices.get(deviceId);
        } else {
            throw new DeviceNotFoundException("Device with ID " + deviceId + " not found.");
        }
    }
    
    public List<Device> getAllDevices() {
        return new ArrayList<>(devices.values());
    }
    
    public void login(String username, String password) throws AuthenticationException {
        if (users.containsKey(username)) {
            User user = users.get(username);
            if (user.authenticate(password)) {
                currentUser = user;
                logSystemEvent("User logged in: " + username);
            } else {
                logSystemEvent("Failed login attempt for user: " + username);
                throw new AuthenticationException("Invalid password.");
            }
        } else {
            logSystemEvent("Failed login attempt for unknown user: " + username);
            throw new AuthenticationException("User not found.");
        }
    }
    
    public void logout() {
        if (currentUser != null) {
            logSystemEvent("User logged out: " + currentUser.getUsername());
            currentUser = null;
        }
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public void addUser(User user) throws AuthenticationException {
        if (currentUser == null || !currentUser.hasPermission("MANAGE_USERS")) {
            throw new AuthenticationException("Only admin users can add new users.");
        }
        
        users.put(user.getUsername(), user);
        logSystemEvent("New user added: " + user.getUsername() + " by " + currentUser.getUsername());
    }
    
    public void handleMotionDetected(String locationName) {
        if (systemOn) {
            logSystemEvent("Motion detected in " + locationName);
            
            // Turn on lights in the location where motion is detected
            for (Device device : devices.values()) {
                if (device instanceof Light && device.getLocation().equals(locationName)) {
                    Light light = (Light) device;
                    if (light.isMotionActivated() && !light.isOn()) {
                        light.activateByMotion();
                        logSystemEvent("Turned on " + device.getName() + " due to motion detection");
                    }
                }
                
                // If there's a security system, notify it about motion
                if (device instanceof SecuritySystem) {
                    ((SecuritySystem)device).detectMotion(locationName);
                }
            }
        }
    }
    
    public void executeScheduledTasks() {
        if (!systemOn) return;
        
        LocalDateTime now = LocalDateTime.now();
        int dayOfWeek = now.getDayOfWeek().getValue() % 7; // 0 = Sunday, 6 = Saturday
        
        for (Device device : devices.values()) {
            for (ScheduledTask task : device.getScheduledTasks()) {
                if (task.isEnabled() && task.getDaysOfWeek()[dayOfWeek]) {
                    // Check if it's time to execute the task
                    if (task.getTime().getHour() == now.getHour() && 
                        task.getTime().getMinute() == now.getMinute()) {
                        task.execute();
                        logSystemEvent("Executed scheduled task: " + task.getName() + " for " + device.getName());
                    }
                }
            }
        }
    }
    
    private void logSystemEvent(String event) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = timestamp + " - " + event;
        systemLogs.add(logEntry);
        
        // Print to console for debugging
        System.out.println(logEntry);
    }
    
    public List<String> getSystemLogs() {
        // Only admin can access logs
        if (currentUser != null && currentUser.hasPermission("VIEW_LOGS")) {
            return new ArrayList<>(systemLogs);
        }
        return new ArrayList<>();
    }
}
