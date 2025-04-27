package smarthome.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Device {
    private String id;
    private String name;
    private String location;
    private LocalDateTime lastStateChange;
    private List<ScheduledTask> scheduledTasks;
    private String createdBy; // Track who created this device
    
    // Constructor with random ID generation
    public Device(String name, String location, String createdBy) {
        // Generate a somewhat random ID with a prefix based on device type
        this.id = generateDeviceId();
        this.name = name;
        this.location = location;
        this.lastStateChange = LocalDateTime.now();
        this.scheduledTasks = new ArrayList<>();
        this.createdBy = createdBy;
    }
    
    // Constructor with specific ID
    public Device(String id, String name, String location, String createdBy) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.lastStateChange = LocalDateTime.now();
        this.scheduledTasks = new ArrayList<>();
        this.createdBy = createdBy;
    }
    
    // Generate a random device ID
    protected String generateDeviceId() {
        // Each implementation can override this to create type-specific IDs
        Random rand = new Random();
        return "DEV-" + rand.nextInt(10000);
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public LocalDateTime getLastStateChange() {
        return lastStateChange;
    }
    
    protected void updateLastStateChange() {
        this.lastStateChange = LocalDateTime.now();
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void addScheduledTask(ScheduledTask task) {
        scheduledTasks.add(task);
    }
    
    public void removeScheduledTask(ScheduledTask task) {
        scheduledTasks.remove(task);
    }
    
    public List<ScheduledTask> getScheduledTasks() {
        return new ArrayList<>(scheduledTasks);
    }
    
    public abstract void turnOn();
    
    public abstract void turnOff();
    
    public abstract boolean isOn();
    
    public abstract void setToDefaultSettings();
    
    @Override
    public String toString() {
        return name + " (" + location + ")";
    }
}
