import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.utilities.Logger;

public class PresetGUI extends JFrame {
    private static final String PRESET_DIRECTORY = System.getProperty("user.home") +
            "/DreamBot/Scripts/KLUS Naguas/";
    private final Color BACKGROUND_COLOR = new Color(32, 34, 37);
    private final Color SECONDARY_COLOR = new Color(47, 49, 54);
    private final Color TEXT_COLOR = new Color(220, 221, 222);
    private final Color ACCENT_COLOR = new Color(114, 137, 218);
    private final Color SUCCESS_COLOR = new Color(67, 181, 129);

    private final DefaultListModel<String> inventoryListModel;
    private final DefaultListModel<String> gearListModel;
    private final JList<String> inventoryList;
    private final JList<String> gearList;
    private final DefaultListModel<Preset> presetListModel;
    private final JList<Preset> presetList;
    private final JTextField presetNameField;
    private final JButton loadInventoryButton;
    private final JButton loadGearButton;

    private Map<String, Integer[]> currentGearPreset;
    private Map<String, Integer[]> currentInventoryPreset;

    public PresetGUI() {
        super("Gear & Inventory Preset Manager");
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Initialize components
        inventoryListModel = new DefaultListModel<>();
        gearListModel = new DefaultListModel<>();
        inventoryList = createStyledList(inventoryListModel);
        gearList = createStyledList(gearListModel);
        presetListModel = new DefaultListModel<>();
        presetList = createStyledList(presetListModel);
        presetNameField = createStyledTextField();
        loadInventoryButton = createStyledButton("Load Inventory");
        loadGearButton = createStyledButton("Load Gear");

        // Initialize maps
        currentGearPreset = new HashMap<>();
        currentInventoryPreset = new HashMap<>();

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Load existing presets
        loadExistingPresets();

        // Add double-click listener
        presetList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    loadSelectedPreset();
                }
            }
        });

        // Add panels
        mainPanel.add(createLeftPanel(), BorderLayout.CENTER);
        mainPanel.add(createRightPanel(), BorderLayout.EAST);
        add(mainPanel);

        // Frame settings
        setupFrame();
    }

    private void loadExistingPresets() {
        File directory = new File(PRESET_DIRECTORY);
        if (!directory.exists()) {
            return;
        }

        File[] presetFiles = directory.listFiles((dir, name) -> name.endsWith(".preset"));
        if (presetFiles == null) return;

        for (File file : presetFiles) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Preset preset = (Preset) ois.readObject();
                presetListModel.addElement(preset);
            } catch (IOException | ClassNotFoundException e) {
                Logger.error("Failed to load preset " + file.getName() + ": " + e.getMessage());
            }
        }
    }

    private void loadSelectedPreset() {
        Preset selectedPreset = presetList.getSelectedValue();
        if (selectedPreset == null) return;

        // Clear existing lists
        inventoryListModel.clear();
        gearListModel.clear();
        currentInventoryPreset.clear();
        currentGearPreset.clear();

        // Load inventory items
        Map<String, Integer[]> inventoryItems = selectedPreset.getInventoryItems();
        for (Map.Entry<String, Integer[]> entry : inventoryItems.entrySet()) {
            String itemName = entry.getKey();
            Integer[] itemData = entry.getValue();
            int itemId = itemData[0];
            int itemAmount = itemData[1];

            // Add to model and current preset
            String itemInfo = String.format("%s (ID: %d, Amount: %d)", itemName, itemId, itemAmount);
            inventoryListModel.addElement(itemInfo);
            currentInventoryPreset.put(itemName, new Integer[]{itemId, itemAmount});
        }

        // Load gear items
        Map<String, Integer[]> gearItems = selectedPreset.getGearItems();
        for (Map.Entry<String, Integer[]> entry : gearItems.entrySet()) {
            String itemName = entry.getKey();
            Integer[] itemData = entry.getValue();
            int itemId = itemData[0];

            // Add to model and current preset
            String itemInfo = String.format("%s (ID: %d)", itemName, itemId);
            gearListModel.addElement(itemInfo);
            currentGearPreset.put(itemName, new Integer[]{itemId, 1});
        }

        // Update button colors to indicate successful load
        loadInventoryButton.setBackground(SUCCESS_COLOR);
        loadGearButton.setBackground(SUCCESS_COLOR);

        // Optional: Add feedback
        Logger.info("Loaded preset: " + selectedPreset.getName());
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBackground(BACKGROUND_COLOR);

        // Inventory section
        JPanel inventoryPanel = createSectionPanel("Inventory Items", inventoryList);
        panel.add(inventoryPanel);

        // Gear section
        JPanel gearPanel = createSectionPanel("Equipped Items", gearList);
        panel.add(gearPanel);

        return panel;
    }

    private JPanel createSectionPanel(String title, JList<?> list) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(createStyledTitledBorder(title));

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setPreferredSize(new Dimension(250, 0));

        // Top controls panel
        JPanel controlsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        controlsPanel.setBackground(BACKGROUND_COLOR);
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        loadInventoryButton.addActionListener(e -> loadInventory());
        loadGearButton.addActionListener(e -> loadGear());

        // Preset name field
        JPanel namePanel = new JPanel(new BorderLayout(5, 5));
        namePanel.setBackground(BACKGROUND_COLOR);
        namePanel.add(createStyledLabel("Preset Name:"), BorderLayout.NORTH);
        namePanel.add(presetNameField, BorderLayout.CENTER);

        JButton saveButton = createStyledButton("Save Preset");
        saveButton.addActionListener(e -> savePreset());

        controlsPanel.add(loadInventoryButton);
        controlsPanel.add(loadGearButton);
        controlsPanel.add(namePanel);
        controlsPanel.add(saveButton);

        // Preset list section
        JPanel presetPanel = new JPanel(new BorderLayout(5, 5));
        presetPanel.setBackground(BACKGROUND_COLOR);
        presetPanel.setBorder(createStyledTitledBorder("Saved Presets"));

        JScrollPane scrollPane = new JScrollPane(presetList);
        styleScrollPane(scrollPane);
        presetPanel.add(scrollPane, BorderLayout.CENTER);

        JButton deleteButton = createStyledButton("Delete Selected");
        deleteButton.addActionListener(e -> deleteSelectedPreset());
        presetPanel.add(deleteButton, BorderLayout.SOUTH);

        // Start button
        JButton startButton = createStyledButton("Start Script");
        startButton.setPreferredSize(new Dimension(startButton.getPreferredSize().width, 40));
        startButton.addActionListener(e -> startScript());

        // Add all components
        panel.add(controlsPanel, BorderLayout.NORTH);
        panel.add(presetPanel, BorderLayout.CENTER);
        panel.add(startButton, BorderLayout.SOUTH);

        return panel;
    }

    private void loadInventory() {
        try {
            inventoryListModel.clear();
            currentInventoryPreset = new HashMap<>();

            // Get all items and count their occurrences
            List<Item> items = Inventory.all();
            Map<Integer, Integer> itemCounts = new HashMap<>();
            Map<Integer, String> itemNames = new HashMap<>();

            // First pass: count items by ID
            for (Item item : items) {
                if (item != null) {
                    int id = item.getID();
                    itemCounts.put(id, itemCounts.getOrDefault(id, 0) + item.getAmount());
                    itemNames.put(id, item.getName());
                }
            }

            // Add items to the list with their total counts
            for (Map.Entry<Integer, Integer> entry : itemCounts.entrySet()) {
                int itemId = entry.getKey();
                int totalAmount = entry.getValue();
                String itemName = itemNames.get(itemId);

                String itemInfo = String.format("%s (ID: %d, Amount: %d)",
                        itemName, itemId, totalAmount);
                inventoryListModel.addElement(itemInfo);
                currentInventoryPreset.put(itemName, new Integer[]{itemId, totalAmount});
            }

            loadInventoryButton.setBackground(SUCCESS_COLOR);
            Logger.info("Inventory loaded successfully");
        } catch (Exception e) {
            showError("Failed to load inventory: " + e.getMessage());
        }
    }

    private void loadGear() {
        try {
            gearListModel.clear();
            currentGearPreset = new HashMap<>();

            // Get all equipped items and count their occurrences
            List<Item> equipment = Equipment.all();
            Map<Integer, Integer> itemCounts = new HashMap<>();
            Map<Integer, String> itemNames = new HashMap<>();

            // First pass: count items by ID
            for (Item item : equipment) {
                if (item != null) {
                    int id = item.getID();
                    itemCounts.put(id, itemCounts.getOrDefault(id, 0) + item.getAmount());
                    itemNames.put(id, item.getName());
                }
            }

            // Add items to the list with their total counts
            for (Map.Entry<Integer, Integer> entry : itemCounts.entrySet()) {
                int itemId = entry.getKey();
                int totalAmount = entry.getValue();
                String itemName = itemNames.get(itemId);

                String itemInfo = String.format("%s (ID: %d, Amount: %d)",
                        itemName, itemId, totalAmount);
                gearListModel.addElement(itemInfo);
                currentGearPreset.put(itemName, new Integer[]{itemId, totalAmount});
            }

            loadGearButton.setBackground(SUCCESS_COLOR);
            Logger.info("Equipment loaded successfully");
        } catch (Exception e) {
            showError("Failed to load equipment: " + e.getMessage());
        }
    }

    private void savePreset() {
        String name = presetNameField.getText().trim();
        if (name.isEmpty()) {
            showError("Please enter a preset name");
            return;
        }

        if (currentInventoryPreset.isEmpty() && currentGearPreset.isEmpty()) {
            showError("Please load inventory or gear before saving preset");
            return;
        }

        try {
            // Create directory if it doesn't exist
            File directory = new File(PRESET_DIRECTORY);
            if (!directory.exists() && !directory.mkdirs()) {
                showError("Failed to create preset directory");
                return;
            }

            // Create and save preset
            Preset preset = new Preset(name, currentInventoryPreset, currentGearPreset);
            String fileName = PRESET_DIRECTORY + name + ".preset";

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
                oos.writeObject(preset);
                presetListModel.addElement(preset);
                resetAfterSave();
                Logger.info("Preset saved successfully: " + name);
            }
        } catch (Exception e) {
            showError("Failed to save preset: " + e.getMessage());
        }
    }

    private void deleteSelectedPreset() {
        int selectedIndex = presetList.getSelectedIndex();
        if (selectedIndex != -1) {
            Preset selectedPreset = presetListModel.getElementAt(selectedIndex);
            File presetFile = new File(PRESET_DIRECTORY + selectedPreset.getName() + ".preset");

            if (presetFile.exists() && presetFile.delete()) {
                presetListModel.remove(selectedIndex);
            } else {
                showError("Failed to delete preset file");
            }
        }
    }

    private void resetAfterSave() {
        loadInventoryButton.setBackground(ACCENT_COLOR);
        loadGearButton.setBackground(ACCENT_COLOR);
        inventoryListModel.clear();
        gearListModel.clear();
        currentInventoryPreset = new HashMap<>();
        currentGearPreset = new HashMap<>();
        presetNameField.setText("");
    }

    // In PresetGUI.java, modify the startScript method:
    private void startScript() {
        Preset selectedPreset = presetList.getSelectedValue();
        if (selectedPreset == null) {
            showError("Please select a preset to start");
            return;
        }

        // Store the selected preset
        PresetStorage.storePreset(selectedPreset);

        // Set the script state to BANKING through a static method
        ScriptStateManager.setState(ScriptState.BANKING);

        dispose();
    }


    private void setupFrame() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
    }

    private <T> JList<T> createStyledList(ListModel<T> model) {
        JList<T> list = new JList<>(model);
        list.setBackground(SECONDARY_COLOR);
        list.setForeground(TEXT_COLOR);
        list.setSelectionBackground(ACCENT_COLOR);
        list.setSelectionForeground(Color.WHITE);
        list.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        list.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return list;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setBackground(SECONDARY_COLOR);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return field;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(ACCENT_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return label;
    }

    private TitledBorder createStyledTitledBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                TEXT_COLOR
        );
        border.setTitlePosition(TitledBorder.ABOVE_TOP);
        return border;
    }

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBackground(BACKGROUND_COLOR);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR));
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public Preset getSelectedPreset() {
        return presetList.getSelectedValue();
    }
}
