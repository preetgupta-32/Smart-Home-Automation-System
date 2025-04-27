package smarthome.models;

import smarthome.interfaces.Switchable;
import java.util.Random;
import java.time.LocalTime;

public class AirConditioner extends Device implements Switchable {
    private boolean isOn;
    private int temperature;
    private String mode; // COOL, HEAT, FAN, DRY, AUTO
    private static final int DEFAULT_TEMPERATURE = 24;
    private static final int MAX_TEMPERATURE = 30;
    private static final int MIN_TEMPERATURE = 16;
    private static final String DEFAULT_MODE = "COOL";
    private boolean energySavingMode;
    private boolean autoTempAdjust; // Added auto temp adjust
    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;
    
    public AirConditioner(String name, String location, String createdBy) {
        super(name, location, createdBy);
        this.isOn = false;
        this.temperature = DEFAULT_TEMPERATURE;
        this.mode = DEFAULT_MODE;
        this.energySavingMode = false;
        this.autoTempAdjust = false; // Default is off
        this.quietHoursStart = LocalTime.of(22, 0); // 10 PM
        this.quietHoursEnd = LocalTime.of(7, 0);    // 7 AM
    }
    
    @Override
    protected String generateDeviceId() {
        Random r = new Random();
        return "AC-" + (10000 + r.nextInt(90000));
    }
    
    @Override
    public void turnOn() {
        if (!isOn) {
            isOn = true;
            updateLastStateChange();
            System.out.println(getName() + " turned ON");
        }
    }
    
    @Override
    public void turnOff() {
        if (isOn) {
            isOn = false;
            updateLastStateChange();
            System.out.println(getName() + " turned OFF");
        }
    }
    
    @Override
    public boolean isOn() {
        return isOn;
    }
    
    public void setTemperature(int temperature) {
        // Enforce temperature limits
        if (temperature < MIN_TEMPERATURE) {
            this.temperature = MIN_TEMPERATURE;
        } else if (temperature > MAX_TEMPERATURE) {
            this.temperature = MAX_TEMPERATURE;
        } else {
            this.temperature = temperature;
        }
        System.out.println(getName() + " temperature set to " + this.temperature + "°C");
    }
    
    public int getTemperature() {
        return temperature;
    }
    
    public void setMode(String mode) {
        // Validate mode
        if (mode.equals("COOL") || mode.equals("HEAT") || mode.equals("FAN") || 
            mode.equals("DRY") || mode.equals("AUTO")) {
            this.mode = mode;
        } else {
            // Invalid mode, use default
            this.mode = DEFAULT_MODE;
        }
        System.out.println(getName() + " mode set to " + this.mode);
    }
    
    public String getMode() {
        return mode;
    }
    
    public boolean isEnergySavingMode() {
        return energySavingMode;
    }
    
    public void setEnergySavingMode(boolean energySavingMode) {
        this.energySavingMode = energySavingMode;
        
        // If energy saving mode is enabled, adjust settings
        if (energySavingMode && isOn) {
            // In cooling mode, increase temperature to save energy
            if (mode.equals("COOL") && temperature < 24) {
                setTemperature(24);
            }
            // In heating mode, decrease temperature to save energy
            else if (mode.equals("HEAT") && temperature > 20) {
                setTemperature(20);
            }
        }
        System.out.println(getName() + " energy saving mode " + (energySavingMode ? "enabled" : "disabled"));
    }
    
    public boolean isAutoTempAdjust() {
        return autoTempAdjust;
    }
    
    public void setAutoTempAdjust(boolean autoTempAdjust) {
        this.autoTempAdjust = autoTempAdjust;
        System.out.println(getName() + " auto temperature adjustment " + (autoTempAdjust ? "enabled" : "disabled"));
        
        // If auto temp adjust is enabled, adjust temperature based on time of day
        if (autoTempAdjust && isOn) {
            adjustTemperatureAuto();
        }
    }
    
    // Auto adjust temperature based on time of day
    public void adjustTemperatureAuto() {
        if (!autoTempAdjust || !isOn) {
            return;
        }
        
        LocalTime now = LocalTime.now();
        
        // Early morning (5-8 AM): Comfortable waking temperature
        if (now.isAfter(LocalTime.of(5, 0)) && now.isBefore(LocalTime.of(8, 0))) {
            if (mode.equals("COOL")) {
                setTemperature(23); // Slightly cooler in the morning
            } else if (mode.equals("HEAT")) {
                setTemperature(22); // Warmer in the morning
            }
        }
        // Daytime (8 AM-5 PM): Energy efficient
        else if (now.isAfter(LocalTime.of(8, 0)) && now.isBefore(LocalTime.of(17, 0))) {
            if (mode.equals("COOL")) {
                setTemperature(25); // Higher during day when people may be out
            } else if (mode.equals("HEAT")) {
                setTemperature(20); // Lower during day
            }
        }
        // Evening (5-10 PM): Comfortable evening temperature
        else if (now.isAfter(LocalTime.of(17, 0)) && now.isBefore(LocalTime.of(22, 0))) {
            if (mode.equals("COOL")) {
                setTemperature(24); // Comfortable evening temperature
            } else if (mode.equals("HEAT")) {
                setTemperature(22); // Comfortable evening temperature
            }
        }
        // Night (10 PM-5 AM): Sleep temperature
        else {
            if (mode.equals("COOL")) {
                setTemperature(26); // Higher at night for sleep
            } else if (mode.equals("HEAT")) {
                setTemperature(19); // Lower at night for sleep
            }
        }
    }
    
    public void setQuietHours(LocalTime start, LocalTime end) {
        this.quietHoursStart = start;
        this.quietHoursEnd = end;
    }
    
    // Check if current time is during quiet hours
    public boolean isQuietHours() {
        LocalTime now = LocalTime.now();
        
        if (quietHoursStart.isBefore(quietHoursEnd)) {
            // Simple case: start time is before end time (e.g., 22:00 to 07:00)
            return now.isAfter(quietHoursStart) && now.isBefore(quietHoursEnd);
        } else {
            // Complex case: start time is after end time (spans midnight)
            return now.isAfter(quietHoursStart) || now.isBefore(quietHoursEnd);
        }
    }
    
    // Adjust settings for quiet hours
    public void adjustForQuietHours() {
        if (isOn && isQuietHours()) {
            // During quiet hours, use more moderate settings
            if (mode.equals("COOL") && temperature < 24) {
                setTemperature(24);
            } else if (mode.equals("HEAT") && temperature > 22) {
                setTemperature(22);
            }
        }
    }
    
    @Override
    public void setToDefaultSettings() {
        this.temperature = DEFAULT_TEMPERATURE;
        this.mode = DEFAULT_MODE;
        System.out.println(getName() + " set to default temperature: " + DEFAULT_TEMPERATURE + 
                          "°C, mode: " + DEFAULT_MODE);
    }
    
    @Override
    public String toString() {
        return super.toString() + " - Status: " + (isOn ? "ON" : "OFF") + 
               ", Temperature: " + temperature + "°C, Mode: " + mode + 
               ", Energy Saving: " + (energySavingMode ? "ON" : "OFF") +
               ", Auto Temp Adjust: " + (autoTempAdjust ? "ON" : "OFF");
    }
}
