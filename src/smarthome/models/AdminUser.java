package smarthome.models;

import java.util.ArrayList;

public class AdminUser extends User {
    
    public AdminUser(String username, String password) {
        super(username, password);
        setupAdminPermissions();
    }
    
    public AdminUser(String username, String password, String name) {
        super(username, password, name);
        setupAdminPermissions();
    }
    
    private void setupAdminPermissions() {
        // Admin has additional permissions
        addPermission("ADD_DEVICE");
        addPermission("REMOVE_DEVICE");
        addPermission("MANAGE_USERS");
        addPermission("VIEW_LOGS");
        addPermission("SYSTEM_SETTINGS");
    }
    
    @Override
    public String getRole() {
        return "ADMIN";
    }
    
    @Override
    public String toString() {
        return super.toString() + " [ADMIN]";
    }
}
