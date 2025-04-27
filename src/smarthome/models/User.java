package smarthome.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;
    private String name;
    private String role;
    private List<String> permissions;
    
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.name = username;
        this.role = "USER";
        this.permissions = new ArrayList<>();
        setupDefaultPermissions();
    }
    
    public User(String username, String password, String name) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = "USER";
        this.permissions = new ArrayList<>();
        setupDefaultPermissions();
    }
    
    private void setupDefaultPermissions() {
        // Regular users can view devices and control them
        this.permissions.add("VIEW_DEVICES");
        this.permissions.add("CONTROL_DEVICES");
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getRole() {
        return role;
    }
    
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
    
    public void addPermission(String permission) {
        if (!permissions.contains(permission)) {
            permissions.add(permission);
        }
    }
    
    public void removePermission(String permission) {
        permissions.remove(permission);
    }
    
    public List<String> getPermissions() {
        return new ArrayList<>(permissions);
    }
    
    public boolean authenticate(String password) {
        return this.password.equals(password);
    }
    
    public void changePassword(String oldPassword, String newPassword) {
        if (authenticate(oldPassword)) {
            this.password = newPassword;
            System.out.println("Password changed successfully.");
        } else {
            System.out.println("Incorrect old password.");
        }
    }
    
    @Override
    public String toString() {
        return name + " (" + username + ")";
    }
}
