package smarthome.models;

import smarthome.interfaces.Switchable;
import java.util.Random;

public class Fan extends Device implements Switchable {
    private boolean isOn;
    private int speed;
    private static final int DEFAULT_SPEED = 2;
    private static final int MAX_SPEED = 5;
    private static final int MIN_SPEED = 1;
    
    public Fan(String name, String location, String createdBy) {
        super(name, location, createdBy);
        this.isOn = false;
        this.speed = DEFAULT_SPEED;
    }
    
    @Override
    protected String generateDeviceId() {
        Random r = new Random();
        return "FAN-" + (10000 + r.nextInt(90000));
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
    
    public void setSpeed(int speed) {
        // Validate speed is within range
        if (speed < MIN_SPEED) {
            this.speed = MIN_SPEED;
        } else if (speed > MAX_SPEED) {
            this.speed = MAX_SPEED;
        } else {
            this.speed = speed;
        }
        System.out.println(getName() + " speed set to " + this.speed);
    }
    
    public int getSpeed() {
        return speed;
    }
    
    @Override
    public void setToDefaultSettings() {
        this.speed = DEFAULT_SPEED;
        System.out.println(getName() + " set to default speed: " + DEFAULT_SPEED);
    }
    
    @Override
    public String toString() {
        return super.toString() + " - Status: " + (isOn ? "ON" : "OFF") + 
               ", Speed: " + speed;
    }
}
