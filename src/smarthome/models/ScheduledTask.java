package smarthome.models;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import smarthome.interfaces.Switchable;

public class ScheduledTask {
    private String id;
    private String name;
    private Device device;
    private String action; // ON, OFF, SET_TEMPERATURE, etc.
    private String[] parameters; // Additional parameters for the action
    private LocalTime time; // Time to execute the task
    private boolean[] daysOfWeek; // Sunday to Saturday
    private boolean isEnabled;
    
    public ScheduledTask(String name, Device device, String action, String[] parameters, 
                         LocalTime time, boolean[] daysOfWeek) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.device = device;
        this.action = action;
        this.parameters = parameters;
        this.time = time;
        this.daysOfWeek = daysOfWeek;
        this.isEnabled = true;
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
    
    public Device getDevice() {
        return device;
    }
    
    public String getAction() {
        return action;
    }
    
    public String[] getParameters() {
        return parameters;
    }
    
    public LocalTime getTime() {
        return time;
    }
    
    public void setTime(LocalTime time) {
        this.time = time;
    }
    
    public boolean[] getDaysOfWeek() {
        return daysOfWeek;
    }
    
    public void setDaysOfWeek(boolean[] daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }
    
    public boolean isEnabled() {
        return isEnabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }
    
    public void execute() {
        if (!isEnabled) return;
        
        System.out.println("Executing scheduled task: " + name);
        
        switch (action) {
            case "ON":
                if (device instanceof Switchable) {
                    ((Switchable) device).turnOn();
                }
                break;
            case "OFF":
                if (device instanceof Switchable) {
                    ((Switchable) device).turnOff();
                }
                break;
            case "SET_TEMPERATURE":
                if (device instanceof AirConditioner && parameters.length > 0) {
                    try {
                        int temp = Integer.parseInt(parameters[0]);
                        ((AirConditioner) device).setTemperature(temp);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid temperature parameter: " + parameters[0]);
                    }
                }
                break;
            case "SET_BRIGHTNESS":
                if (device instanceof Light && parameters.length > 0) {
                    try {
                        int brightness = Integer.parseInt(parameters[0]);
                        ((Light) device).setBrightness(brightness);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid brightness parameter: " + parameters[0]);
                    }
                }
                break;
            case "SET_SPEED":
                if (device instanceof Fan && parameters.length > 0) {
                    try {
                        int speed = Integer.parseInt(parameters[0]);
                        ((Fan) device).setSpeed(speed);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid speed parameter: " + parameters[0]);
                    }
                }
                break;
            case "SET_SECURITY_MODE":
                if (device instanceof SecuritySystem && parameters.length > 0) {
                    ((SecuritySystem) device).setSecurityMode(parameters[0]);
                }
                break;
            default:
                System.out.println("Unknown action: " + action);
        }
    }
    
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String timeStr = time.format(formatter);
        
        String daysStr = "";
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < daysOfWeek.length; i++) {
            if (daysOfWeek[i]) {
                daysStr += dayNames[i] + " ";
            }
        }
        
        return name + " - " + device.getName() + " - " + action + " at " + timeStr + " on " + daysStr.trim();
    }
}
