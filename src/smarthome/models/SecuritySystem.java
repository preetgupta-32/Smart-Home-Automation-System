package smarthome.models;

import smarthome.interfaces.Switchable;
import java.util.ArrayList;
import java.util.List;

public class SecuritySystem extends Device implements Switchable {
    private boolean isOn;
    private boolean alarmActive;
    private List<String> securityLogs;
    private String securityMode; // AWAY, HOME, DISARMED
    private static final String DEFAULT_MODE = "DISARMED";
    
    public SecuritySystem(String id, String name, String location) {
        super(id, name, location);
        this.isOn = false;
        this.alarmActive = false;
        this.securityLogs = new ArrayList<>();
        this.securityMode = DEFAULT_MODE;
    }
    
    @Override
    public void turnOn() {
        if (!isOn) {
            isOn = true;
            updateLastStateChange();
            System.out.println(getName() + " turned ON");
            addSecurityLog("System armed");
        }
    }
    
    @Override
    public void turnOff() {
        if (isOn) {
            isOn = false;
            updateLastStateChange();
            System.out.println(getName() + " turned OFF");
            addSecurityLog("System disarmed");
            deactivateAlarm(); // Ensure alarm is off when system is off
        }
    }
    
    @Override
    public boolean isOn() {
        return isOn;
    }
    
    public void setSecurityMode(String mode) {
        if (mode.equals("AWAY") || mode.equals("HOME") || mode.equals("DISARMED")) {
            this.securityMode = mode;
            addSecurityLog("Security mode changed to " + mode);
            System.out.println(getName() + " security mode set to " + mode);
        } else {
            System.out.println("Invalid security mode. Using default: " + DEFAULT_MODE);
            this.securityMode = DEFAULT_MODE;
        }
    }
    
    public String getSecurityMode() {
        return securityMode;
    }
    
    public void activateAlarm() {
        if (isOn && !alarmActive) {
            alarmActive = true;
            addSecurityLog("ALARM ACTIVATED!");
            System.out.println("ALARM ACTIVATED on " + getName() + "!");
        }
    }
    
    public void deactivateAlarm() {
        if (alarmActive) {
            alarmActive = false;
            addSecurityLog("Alarm deactivated");
            System.out.println("Alarm deactivated on " + getName());
        }
    }
    
    public boolean isAlarmActive() {
        return alarmActive;
    }
    
    public void detectMotion(String location) {
        if (isOn) {
            addSecurityLog("Motion detected in " + location);
            
            if (securityMode.equals("AWAY")) {
                // In AWAY mode, any motion triggers the alarm
                activateAlarm();
            } else if (securityMode.equals("HOME")) {
                // In HOME mode, only motion in certain areas triggers the alarm
                if (location.equals("Entrance") || location.equals("Window")) {
                    activateAlarm();
                }
            }
            // In DISARMED mode, just log the motion but don't trigger alarm
        }
    }
    
    private void addSecurityLog(String event) {
        String logEntry = java.time.LocalDateTime.now() + ": " + event;
        securityLogs.add(logEntry);
    }
    
    public List<String> getSecurityLogs() {
        return new ArrayList<>(securityLogs);
    }
    
    @Override
    public void setToDefaultSettings() {
        this.securityMode = DEFAULT_MODE;
        this.alarmActive = false;
        System.out.println(getName() + " set to default mode: " + DEFAULT_MODE);
    }
    
    @Override
    public String toString() {
        return super.toString() + " - Status: " + (isOn ? "ON" : "OFF") + 
               ", Mode: " + securityMode + ", Alarm: " + (alarmActive ? "ACTIVE" : "Inactive");
    }
}

