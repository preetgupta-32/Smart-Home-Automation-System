package smarthome;

import smarthome.system.SmartHomeSystem;



import smarthome.models.*;
import smarthome.exceptions.*;
import smarthome.interfaces.Switchable;
import smarthome.interfaces.Dimmable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Date;

public class SmartHomeGUI {
    private SmartHomeSystem system;
    private JFrame mainFrame;
    private JPanel mainPanel;
    private JPanel devicePanel;
    private JPanel controlPanel;
    private Timer scheduleTimer;
    
    public SmartHomeGUI(SmartHomeSystem system) {
        this.system = system;
    }
    
    public void launch() {
        // Create the main frame
        mainFrame = new JFrame("Smart Home System");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1000, 600);
        mainFrame.setLayout(new BorderLayout());
        
        // Create the main panel
        mainPanel = new JPanel(new BorderLayout());
        
        // Create the login panel
        createLoginPanel();
        
        // Start a timer to check scheduled tasks every minute
        scheduleTimer = new Timer(60000, e -> system.executeScheduledTasks());
        scheduleTimer.start();
        
        // Show the frame
        mainFrame.setVisible(true);
    }
    
    private void createLoginPanel() {
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JLabel titleLabel = new JLabel("Smart Home System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(titleLabel, gbc);
        
        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        loginPanel.add(usernameLabel, gbc);
        
        JTextField usernameField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(usernameField, gbc);
        
        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        loginPanel.add(passwordLabel, gbc);
        
        JPasswordField passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(passwordField, gbc);
        
        JButton loginButton = new JButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(loginButton, gbc);
        
        JLabel statusLabel = new JLabel(" ");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        loginPanel.add(statusLabel, gbc);
        
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                
                try {
                    system.login(username, password);
                    mainFrame.getContentPane().removeAll();
                    createMainPanel();
                    mainFrame.getContentPane().add(mainPanel);
                    mainFrame.revalidate();
                    mainFrame.repaint();
                } catch (AuthenticationException ex) {
                    statusLabel.setText("Login failed: " + ex.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        });
        
        mainFrame.getContentPane().add(loginPanel);
    }
    
    private void createMainPanel() {
        mainPanel.removeAll();
        
        // Create the top panel with system controls
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // User info and logout
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel userLabel = new JLabel("Logged in as: " + system.getCurrentUser().getName() + 
                                     " (" + system.getCurrentUser().getRole() + ")");
        JButton logoutButton = new JButton("Logout");
        userPanel.add(userLabel);
        userPanel.add(logoutButton);
        
        // System on/off toggle
        JPanel systemControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JToggleButton systemToggle = new JToggleButton("System OFF");
        systemToggle.setSelected(system.isSystemOn());
        if (system.isSystemOn()) {
            systemToggle.setText("System ON");
        }
        
        systemControlPanel.add(systemToggle);
        
        // Admin-only buttons
        if (system.getCurrentUser().hasPermission("MANAGE_USERS")) {
            JButton addUserButton = new JButton("Add User");
            JButton viewLogsButton = new JButton("View Logs");
            
            systemControlPanel.add(addUserButton);
            systemControlPanel.add(viewLogsButton);
            
            addUserButton.addActionListener(e -> showAddUserDialog());
            viewLogsButton.addActionListener(e -> showSystemLogs());
        }
        
        topPanel.add(userPanel, BorderLayout.EAST);
        topPanel.add(systemControlPanel, BorderLayout.WEST);
        
        // Create the device list panel
        devicePanel = new JPanel();
        devicePanel.setLayout(new BoxLayout(devicePanel, BoxLayout.Y_AXIS));
        JScrollPane deviceScrollPane = new JScrollPane(devicePanel);
        deviceScrollPane.setBorder(BorderFactory.createTitledBorder("Devices"));
        
        // Create the control panel (right side)
        controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Control Panel"));
        
        // Create a default message for the control panel
        JLabel defaultControlLabel = new JLabel("Select a device to control");
        defaultControlLabel.setHorizontalAlignment(JLabel.CENTER);
        controlPanel.add(defaultControlLabel, BorderLayout.CENTER);
        
        // Create a split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, deviceScrollPane, controlPanel);
        splitPane.setDividerLocation(300);
        
        // Add "Add Device" button if user has permission
        if (system.getCurrentUser().hasPermission("ADD_DEVICE")) {
            JButton addDeviceButton = new JButton("Add New Device");
            addDeviceButton.addActionListener(e -> showAddDeviceDialog());
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            buttonPanel.add(addDeviceButton);
            
            deviceScrollPane.setColumnHeaderView(buttonPanel);
        }
        
        // Add components to the main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        // Add action listeners
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                system.logout();
                mainFrame.getContentPane().removeAll();
                createLoginPanel();
                mainFrame.revalidate();
                mainFrame.repaint();
            }
        });
        
        systemToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (systemToggle.isSelected()) {
                    system.turnSystemOn();
                    systemToggle.setText("System ON");
                } else {
                    system.turnSystemOff();
                    systemToggle.setText("System OFF");
                }
                updateDeviceList();
            }
        });
        
        // Populate the device list
        updateDeviceList();
    }
    
    private void updateDeviceList() {
        devicePanel.removeAll();
        
        List<Device> devices = system.getAllDevices();
        
        for (Device device : devices) {
            JPanel deviceItemPanel = new JPanel(new BorderLayout());
            deviceItemPanel.setBorder(BorderFactory.createEtchedBorder());
            
            String statusText = device instanceof Switchable ? 
                                (((Switchable) device).isOn() ? "ON" : "OFF") : "N/A";
            
            JLabel deviceLabel = new JLabel(device.getName() + " (" + device.getLocation() + ") - " + statusText);
            deviceItemPanel.add(deviceLabel, BorderLayout.CENTER);
            
            // All users should be able to select devices
            deviceItemPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showDeviceControl(device);
                }
            });
            
            // Only add remove button if user has permission
            if (system.getCurrentUser().hasPermission("REMOVE_DEVICE")) {
                JButton removeButton = new JButton("Remove");
                removeButton.addActionListener(e -> {
                    try {
                        system.removeDevice(device.getId());
                        updateDeviceList();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(mainFrame, 
                                                     "Error removing device: " + ex.getMessage(),
                                                     "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
                deviceItemPanel.add(removeButton, BorderLayout.EAST);
            }
            
            devicePanel.add(deviceItemPanel);
        }
        
        devicePanel.revalidate();
        devicePanel.repaint();
    }
    
    private void showDeviceControl(Device device) {
        controlPanel.removeAll();
        
        JPanel deviceControlPanel = new JPanel();
        deviceControlPanel.setLayout(new BoxLayout(deviceControlPanel, BoxLayout.Y_AXIS));
        
        // Device title
        JLabel titleLabel = new JLabel(device.getName());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        deviceControlPanel.add(titleLabel);
        
        // Location
        JLabel locationLabel = new JLabel("Location: " + device.getLocation());
        locationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        deviceControlPanel.add(locationLabel);
        
        deviceControlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Check if user has control permission before adding controls
        if (system.getCurrentUser().hasPermission("CONTROL_DEVICES")) {
            // On/Off control if device is Switchable
            if (device instanceof Switchable) {
                Switchable switchableDevice = (Switchable) device;
                JPanel switchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                JToggleButton onOffToggle = new JToggleButton(switchableDevice.isOn() ? "ON" : "OFF");
                onOffToggle.setSelected(switchableDevice.isOn());
                
                onOffToggle.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (onOffToggle.isSelected()) {
                            switchableDevice.turnOn();
                            onOffToggle.setText("ON");
                        } else {
                            switchableDevice.turnOff();
                            onOffToggle.setText("OFF");
                        }
                        updateDeviceList();
                    }
                });
                
                switchPanel.add(new JLabel("Power:"));
                switchPanel.add(onOffToggle);
                deviceControlPanel.add(switchPanel);
            }
            
            // Device-specific controls
            if (device instanceof Light) {
                Light light = (Light) device;
                
                // Brightness control
                JPanel brightnessPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                JSlider brightnessSlider = new JSlider(0, 100, light.getBrightness());
                JLabel brightnessValueLabel = new JLabel(light.getBrightness() + "%");
                
                brightnessSlider.addChangeListener(e -> {
                    int value = brightnessSlider.getValue();
                    light.setBrightness(value);
                    brightnessValueLabel.setText(value + "%");
                    if (!brightnessSlider.getValueIsAdjusting()) {
                        updateDeviceList();
                    }
                });
                
                brightnessPanel.add(new JLabel("Brightness:"));
                brightnessPanel.add(brightnessSlider);
                brightnessPanel.add(brightnessValueLabel);
                deviceControlPanel.add(brightnessPanel);
                
                // Color selection - NEW FEATURE
                JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                colorPanel.add(new JLabel("Color:"));
                
                String[] colors = {
                    Light.COLOR_WHITE, 
                    Light.COLOR_WARM, 
                    Light.COLOR_BLUE, 
                    Light.COLOR_RED
                };
                
                JComboBox<String> colorComboBox = new JComboBox<>(colors);
                colorComboBox.setSelectedItem(light.getColor());
                
                // Create color buttons with actual colors
                JPanel colorButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                
                JButton whiteButton = new JButton("   ");
                whiteButton.setBackground(Color.WHITE);
                whiteButton.setToolTipText(Light.COLOR_WHITE);
                whiteButton.addActionListener(e -> {
                    light.setColor(Light.COLOR_WHITE);
                    colorComboBox.setSelectedItem(Light.COLOR_WHITE);
                    updateDeviceList();
                });
                
                JButton warmButton = new JButton("   ");
                warmButton.setBackground(new Color(255, 244, 229)); // Warm white color
                warmButton.setToolTipText(Light.COLOR_WARM);
                warmButton.addActionListener(e -> {
                    light.setColor(Light.COLOR_WARM);
                    colorComboBox.setSelectedItem(Light.COLOR_WARM);
                    updateDeviceList();
                });
                
                JButton blueButton = new JButton("   ");
                blueButton.setBackground(Color.BLUE);
                blueButton.setToolTipText(Light.COLOR_BLUE);
                blueButton.addActionListener(e -> {
                    light.setColor(Light.COLOR_BLUE);
                    colorComboBox.setSelectedItem(Light.COLOR_BLUE);
                    updateDeviceList();
                });
                
                JButton redButton = new JButton("   ");
                redButton.setBackground(Color.RED);
                redButton.setToolTipText(Light.COLOR_RED);
                redButton.addActionListener(e -> {
                    light.setColor(Light.COLOR_RED);
                    colorComboBox.setSelectedItem(Light.COLOR_RED);
                    updateDeviceList();
                });
                
                colorButtonsPanel.add(whiteButton);
                colorButtonsPanel.add(warmButton);
                colorButtonsPanel.add(blueButton);
                colorButtonsPanel.add(redButton);
                
                colorComboBox.addActionListener(e -> {
                    String selectedColor = (String) colorComboBox.getSelectedItem();
                    light.setColor(selectedColor);
                    updateDeviceList();
                });
                
                colorPanel.add(colorComboBox);
                deviceControlPanel.add(colorPanel);
                deviceControlPanel.add(colorButtonsPanel);
                
                // Motion activation control
                JPanel motionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                JCheckBox motionCheckbox = new JCheckBox("Motion Activated", light.isMotionActivated());
                
                motionCheckbox.addActionListener(e -> {
                    light.setMotionActivated(motionCheckbox.isSelected());
                });
                
                motionPanel.add(motionCheckbox);
                deviceControlPanel.add(motionPanel);
                
            } else if (device instanceof Fan) {
                Fan fan = (Fan) device;
                
                // Speed control
                JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                JSlider speedSlider = new JSlider(1, 5, fan.getSpeed());
                speedSlider.setMajorTickSpacing(1);
                speedSlider.setPaintTicks(true);
                speedSlider.setPaintLabels(true);
                JLabel speedValueLabel = new JLabel("Speed: " + fan.getSpeed());
                
                speedSlider.addChangeListener(e -> {
                    int value = speedSlider.getValue();
                    fan.setSpeed(value);
                    speedValueLabel.setText("Speed: " + value);
                    if (!speedSlider.getValueIsAdjusting()) {
                        updateDeviceList();
                    }
                });
                
                speedPanel.add(speedValueLabel);
                speedPanel.add(speedSlider);
                deviceControlPanel.add(speedPanel);
                
                // Oscillation and Auto-adjust options removed as requested
                
            } else if (device instanceof AirConditioner) {
                AirConditioner ac = (AirConditioner) device;
                
                // Temperature control
                JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                JSlider tempSlider = new JSlider(16, 30, ac.getTemperature());
                tempSlider.setMajorTickSpacing(2);
                tempSlider.setPaintTicks(true);
                tempSlider.setPaintLabels(true);
                JLabel tempValueLabel = new JLabel(ac.getTemperature() + "°C");
                
                tempSlider.addChangeListener(e -> {
                    int value = tempSlider.getValue();
                    ac.setTemperature(value);
                    tempValueLabel.setText(value + "°C");
                    if (!tempSlider.getValueIsAdjusting()) {
                        updateDeviceList();
                    }
                });
                
                tempPanel.add(new JLabel("Temperature:"));
                tempPanel.add(tempSlider);
                tempPanel.add(tempValueLabel);
                deviceControlPanel.add(tempPanel);
                
                // Mode control
                JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                String[] modes = {"COOL", "HEAT", "FAN", "DRY", "AUTO"};
                JComboBox<String> modeComboBox = new JComboBox<>(modes);
                modeComboBox.setSelectedItem(ac.getMode());
                
                modeComboBox.addActionListener(e -> {
                    String selectedMode = (String) modeComboBox.getSelectedItem();
                    ac.setMode(selectedMode);
                    updateDeviceList();
                });
                
                modePanel.add(new JLabel("Mode:"));
                modePanel.add(modeComboBox);
                deviceControlPanel.add(modePanel);
                
                // Energy saving mode
                JPanel energyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                JCheckBox energyCheckbox = new JCheckBox("Energy Saving Mode", ac.isEnergySavingMode());
                
                energyCheckbox.addActionListener(e -> {
                    ac.setEnergySavingMode(energyCheckbox.isSelected());
                    updateDeviceList();
                });
                
                energyPanel.add(energyCheckbox);
                deviceControlPanel.add(energyPanel);
                
                // Auto temperature adjust - NEW FEATURE
                JPanel autoTempPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                JCheckBox autoTempCheckbox = new JCheckBox("Auto Temperature Adjust", ac.isAutoTempAdjust());
                
                autoTempCheckbox.addActionListener(e -> {
                    ac.setAutoTempAdjust(autoTempCheckbox.isSelected());
                    updateDeviceList();
                });
                
                autoTempPanel.add(autoTempCheckbox);
                deviceControlPanel.add(autoTempPanel);
                
            } else if (device instanceof SecuritySystem) {
                SecuritySystem security = (SecuritySystem) device;
                
                // Security mode control
                JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                String[] modes = {"DISARMED", "HOME", "AWAY"};
                JComboBox<String> modeComboBox = new JComboBox<>(modes);
                modeComboBox.setSelectedItem(security.getSecurityMode());
                
                modeComboBox.addActionListener(e -> {
                    String selectedMode = (String) modeComboBox.getSelectedItem();
                    security.setSecurityMode(selectedMode);
                    updateDeviceList();
                });
                
                modePanel.add(new JLabel("Security Mode:"));
                modePanel.add(modeComboBox);
                deviceControlPanel.add(modePanel);
                
                // Alarm status
                JPanel alarmPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                JLabel alarmStatusLabel = new JLabel("Alarm Status: " + 
                                                    (security.isAlarmActive() ? "ACTIVE" : "Inactive"));
                alarmStatusLabel.setForeground(security.isAlarmActive() ? Color.RED : Color.BLACK);
                
                JButton alarmButton = new JButton(security.isAlarmActive() ? "Deactivate Alarm" : "Test Alarm");
                
                alarmButton.addActionListener(e -> {
                    if (security.isAlarmActive()) {
                        security.deactivateAlarm();
                    } else {
                        security.activateAlarm();
                    }
                    updateDeviceList();
                    showDeviceControl(device); // Refresh the control panel
                });
                
                alarmPanel.add(alarmStatusLabel);
                alarmPanel.add(alarmButton);
                deviceControlPanel.add(alarmPanel);
                
                // Security logs
                if (system.getCurrentUser().hasPermission("VIEW_LOGS")) {
                    JPanel logsPanel = new JPanel(new BorderLayout());
                    logsPanel.setBorder(BorderFactory.createTitledBorder("Security Logs"));
                    
                    JTextArea logsTextArea = new JTextArea(10, 30);
                    logsTextArea.setEditable(false);
                    JScrollPane logsScrollPane = new JScrollPane(logsTextArea);
                    
                    List<String> logs = security.getSecurityLogs();
                    for (String log : logs) {
                        logsTextArea.append(log + "\n");
                    }
                    
                    logsPanel.add(logsScrollPane, BorderLayout.CENTER);
                    deviceControlPanel.add(logsPanel);
                }
            }
            
            // Scheduling section
            if (system.getCurrentUser().hasPermission("CONTROL_DEVICES")) {
                deviceControlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
                JPanel schedulePanel = new JPanel(new BorderLayout());
                schedulePanel.setBorder(BorderFactory.createTitledBorder("Scheduled Tasks"));
                
                JButton addScheduleButton = new JButton("Add Schedule");
                schedulePanel.add(addScheduleButton, BorderLayout.NORTH);
                
                JPanel tasksPanel = new JPanel();
                tasksPanel.setLayout(new BoxLayout(tasksPanel, BoxLayout.Y_AXIS));
                JScrollPane tasksScrollPane = new JScrollPane(tasksPanel);
                
                // Populate scheduled tasks
                List<ScheduledTask> tasks = device.getScheduledTasks();
                for (ScheduledTask task : tasks) {
                    JPanel taskItemPanel = new JPanel(new BorderLayout());
                    taskItemPanel.setBorder(BorderFactory.createEtchedBorder());
                    
                    JLabel taskLabel = new JLabel(task.toString());
                    JButton removeButton = new JButton("Remove");
                    
                    removeButton.addActionListener(e -> {
                        device.removeScheduledTask(task);
                        showDeviceControl(device); // Refresh the control panel
                    });
                    
                    taskItemPanel.add(taskLabel, BorderLayout.CENTER);
                    taskItemPanel.add(removeButton, BorderLayout.EAST);
                    
                    tasksPanel.add(taskItemPanel);
                }
                
                schedulePanel.add(tasksScrollPane, BorderLayout.CENTER);
                deviceControlPanel.add(schedulePanel);
                
                // Add schedule button action
                addScheduleButton.addActionListener(e -> {
                    showAddScheduleDialog(device);
                });
            }
        } else {
            // If user doesn't have control permission, just show device info
            JLabel infoLabel = new JLabel("You don't have permission to control this device");
            infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            deviceControlPanel.add(infoLabel);
        }
        
        // Add the device control panel to the main control panel
        JScrollPane controlScrollPane = new JScrollPane(deviceControlPanel);
        controlPanel.add(controlScrollPane, BorderLayout.CENTER);
        
        controlPanel.revalidate();
        controlPanel.repaint();
    }
    
    private void showAddDeviceDialog() {
        JDialog dialog = new JDialog(mainFrame, "Add New Device", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Device type selection
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.add(new JLabel("Device Type:"));
        String[] deviceTypes = {"Light", "Fan", "Air Conditioner", "Security System"};
        JComboBox<String> typeComboBox = new JComboBox<>(deviceTypes);
        typePanel.add(typeComboBox);
        formPanel.add(typePanel);
        
        // Device name
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.add(new JLabel("Device Name:"));
        JTextField nameField = new JTextField(20);
        namePanel.add(nameField);
        formPanel.add(namePanel);
        
        // Device location
        JPanel locationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        locationPanel.add(new JLabel("Location:"));
        JTextField locationField = new JTextField(20);
        locationPanel.add(locationField);
        formPanel.add(locationPanel);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton addButton = new JButton("Add Device");
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        addButton.addActionListener(e -> {
            String name = nameField.getText();
            String location = locationField.getText();
            String type = (String) typeComboBox.getSelectedItem();
            
            if (name.isEmpty() || location.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                                             "Name and location cannot be empty",
                                             "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                Device newDevice = null;
                String createdBy = system.getCurrentUser().getUsername();
                
                switch (type) {
                    case "Light":
                        newDevice = new Light(name, location, createdBy);
                        break;
                    case "Fan":
                        newDevice = new Fan(name, location, createdBy);
                        break;
                    case "Air Conditioner":
                        newDevice = new AirConditioner(name, location, createdBy);
                        break;
                    case "Security System":
                        newDevice = new SecuritySystem(name, location, createdBy);
                        break;
                }
                
                if (newDevice != null) {
                    system.addDevice(newDevice);
                    updateDeviceList();
                    dialog.dispose();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                                             "Error adding device: " + ex.getMessage(),
                                             "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(addButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setVisible(true);
    }
    
    private void showAddUserDialog() {
        JDialog dialog = new JDialog(mainFrame, "Add New User", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Username
        JPanel usernamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        usernamePanel.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField(20);
        usernamePanel.add(usernameField);
        formPanel.add(usernamePanel);
        
        // Password
        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        passwordPanel.add(new JLabel("Password:"));
        JPasswordField passwordField = new JPasswordField(20);
        passwordPanel.add(passwordField);
        formPanel.add(passwordPanel);
        
        // Name
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.add(new JLabel("Display Name:"));
        JTextField nameField = new JTextField(20);
        namePanel.add(nameField);
        formPanel.add(namePanel);
        
        // Role
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rolePanel.add(new JLabel("Role:"));
        String[] roles = {"User", "Admin"};
        JComboBox<String> roleComboBox = new JComboBox<>(roles);
        rolePanel.add(roleComboBox);
        formPanel.add(rolePanel);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton addButton = new JButton("Add User");
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        addButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String name = nameField.getText();
            String role = (String) roleComboBox.getSelectedItem();
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                                             "Username and password cannot be empty",
                                             "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                User newUser;
                if (role.equals("Admin")) {
                    newUser = new AdminUser(username, password, name);
                } else {
                    newUser = new User(username, password, name);
                }
                
                system.addUser(newUser);
                dialog.dispose();
                
                JOptionPane.showMessageDialog(mainFrame, 
                                             "User added successfully",
                                             "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                                             "Error adding user: " + ex.getMessage(),
                                             "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(addButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setVisible(true);
    }
    
    private void showSystemLogs() {
        JDialog logsDialog = new JDialog(mainFrame, "System Logs", true);
        logsDialog.setSize(700, 500);
        logsDialog.setLayout(new BorderLayout());
        
        JTextArea logsArea = new JTextArea();
        logsArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logsArea);
        
        List<String> logs = system.getSystemLogs();
        for (String log : logs) {
            logsArea.append(log + "\n");
        }
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> logsDialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        
        logsDialog.add(scrollPane, BorderLayout.CENTER);
        logsDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        logsDialog.setLocationRelativeTo(mainFrame);
        logsDialog.setVisible(true);
    }
    
    private void showAddScheduleDialog(Device device) {
        JDialog scheduleDialog = new JDialog(mainFrame, "Add Schedule", true);
        scheduleDialog.setSize(400, 400);
        scheduleDialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Task name
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.add(new JLabel("Task Name:"));
        JTextField nameField = new JTextField(20);
        namePanel.add(nameField);
        formPanel.add(namePanel);
        
        // Action selection
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.add(new JLabel("Action:"));
        
        String[] actions;
        if (device instanceof Light) {
            actions = new String[]{"ON", "OFF", "SET_BRIGHTNESS"};
        } else if (device instanceof Fan) {
            actions = new String[]{"ON", "OFF", "SET_SPEED"};
        } else if (device instanceof AirConditioner) {
            actions = new String[]{"ON", "OFF", "SET_TEMPERATURE"};
        } else if (device instanceof SecuritySystem) {
            actions = new String[]{"ON", "OFF", "SET_SECURITY_MODE"};
        } else {
            actions = new String[]{"ON", "OFF"};
        }
        
        JComboBox<String> actionComboBox = new JComboBox<>(actions);
        actionPanel.add(actionComboBox);
        formPanel.add(actionPanel);
        
        // Parameters panel (changes based on action)
        JPanel parametersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel paramLabel = new JLabel("Value:");
        JTextField paramField = new JTextField(10);
        parametersPanel.add(paramLabel);
        parametersPanel.add(paramField);
        formPanel.add(parametersPanel);
        
        // Initially hide parameters if not needed
        parametersPanel.setVisible(actionComboBox.getSelectedItem().toString().startsWith("SET_"));
        
        actionComboBox.addActionListener(e -> {
            String selectedAction = actionComboBox.getSelectedItem().toString();
            parametersPanel.setVisible(selectedAction.startsWith("SET_"));
            
            if (selectedAction.equals("SET_BRIGHTNESS")) {
                paramLabel.setText("Brightness (0-100):");
            } else if (selectedAction.equals("SET_SPEED")) {
                paramLabel.setText("Speed (1-5):");
            } else if (selectedAction.equals("SET_TEMPERATURE")) {
                paramLabel.setText("Temperature (16-30):");
            } else if (selectedAction.equals("SET_SECURITY_MODE")) {
                paramLabel.setText("Mode (DISARMED/HOME/AWAY):");
            }
        });
        
        // Time selection
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timePanel.add(new JLabel("Time:"));
        JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        timePanel.add(timeSpinner);
        formPanel.add(timePanel);
        
        // Days of week
        JPanel daysPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        daysPanel.add(new JLabel("Days:"));
        JCheckBox[] dayCheckboxes = new JCheckBox[7];
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        
        for (int i = 0; i < 7; i++) {
            dayCheckboxes[i] = new JCheckBox(dayNames[i]);
            daysPanel.add(dayCheckboxes[i]);
        }
        formPanel.add(daysPanel);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton saveButton = new JButton("Save");
        
        cancelButton.addActionListener(e -> scheduleDialog.dispose());
        
        saveButton.addActionListener(e -> {
            String taskName = nameField.getText();
            String action = actionComboBox.getSelectedItem().toString();
            
            String[] parameters = null;
            if (action.startsWith("SET_")) {
                parameters = new String[]{paramField.getText()};
            } else {
                parameters = new String[0];
            }
            
            // Get time
            Date date = (Date) timeSpinner.getValue();
            LocalTime time = LocalTime.of(date.getHours(), date.getMinutes());
            
            // Get days
            boolean[] days = new boolean[7];
            for (int i = 0; i < 7; i++) {
                days[i] = dayCheckboxes[i].isSelected();
            }
            
            // Create and add the scheduled task
            ScheduledTask task = new ScheduledTask(taskName, device, action, parameters, time, days);
            device.addScheduledTask(task);
            
            scheduleDialog.dispose();
            showDeviceControl(device); // Refresh the control panel
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        scheduleDialog.add(formPanel, BorderLayout.CENTER);
        scheduleDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        scheduleDialog.setLocationRelativeTo(mainFrame);
        scheduleDialog.setVisible(true);
    }
}
