package smarthome.models;

import smarthome.interfaces.Switchable;
import smarthome.interfaces.Dimmable;
import java.util.Random;

public class Light extends Device implements Switchable, Dimmable {
    private boolean isOn;
    private int brightness;
    private boolean motionActivated;
    private int motionBrightness;
    private String color; // Added color property
    private static final int DEFAULT_BRIGHTNESS = 50;
    private static final int MAX_BRIGHTNESS = 100;
    private static final int MIN_BRIGHTNESS = 0;
    
    // Available colors
    public static final String COLOR_WHITE = "White";
    public static final String COLOR_WARM = "Warm White";
    public static final String COLOR_BLUE = "Blue";
    public static final String COLOR_RED = "Red";
    
    public Light(String name, String location, String createdBy) {
        super(name, location, createdBy);
        this.isOn = false;
        this.brightness = DEFAULT_BRIGHTNESS;
        this.motionActivated = true;
        this.motionBrightness = 70;
        this.color = COLOR_WHITE; // Default color
    }
    
    @Override
    protected String generateDeviceId() {
        Random r = new Random();
        return "LIGHT-" + (10000 + r.nextInt(90000));
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
    
    @Override
    public void setBrightness(int level) {
        if (level < MIN_BRIGHTNESS) {
            this.brightness = MIN_BRIGHTNESS;
        } else if (level > MAX_BRIGHTNESS) {
            this.brightness = MAX_BRIGHTNESS;
        } else {
            this.brightness = level;
        }
        System.out.println(getName() + " brightness set to " + this.brightness + "%");
    }
    
    @Override
    public int getBrightness() {
        return brightness;
    }
    
    public boolean isMotionActivated() {
        return motionActivated;
    }
    
    public void setMotionActivated(boolean motionActivated) {
        this.motionActivated = motionActivated;
        System.out.println(getName() + " motion activation " + (motionActivated ? "enabled" : "disabled"));
    }
    
    public int getMotionBrightness() {
        return motionBrightness;
    }
    
    public void setMotionBrightness(int motionBrightness) {
        if (motionBrightness < MIN_BRIGHTNESS) {
            this.motionBrightness = MIN_BRIGHTNESS;
        } else if (motionBrightness > MAX_BRIGHTNESS) {
            this.motionBrightness = MAX_BRIGHTNESS;
        } else {
            this.motionBrightness = motionBrightness;
        }
        System.out.println(getName() + " motion brightness set to " + this.motionBrightness + "%");
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
        System.out.println(getName() + " color set to " + this.color);
    }
    
    public void activateByMotion() {
        if (motionActivated) {
            turnOn();
            int prevBrightness = brightness;
            setBrightness(motionBrightness);
            System.out.println(getName() + " activated by motion detection");
        }
    }
    
    @Override
    public void setToDefaultSettings() {
        this.brightness = DEFAULT_BRIGHTNESS;
        this.color = COLOR_WHITE;
        System.out.println(getName() + " set to default brightness: " + DEFAULT_BRIGHTNESS + "%, color: " + COLOR_WHITE);
    }
    
    @Override
    public String toString() {
        return super.toString() + " - Status: " + (isOn ? "ON" : "OFF") + 
               ", Brightness: " + brightness + "%, Color: " + color + 
               ", Motion Activated: " + (motionActivated ? "Yes" : "No");
    }
}
