import java.util.HashMap;
import java.util.Map;

public class PresetStorage {
    private static final Map<String, Integer[]> inventoryItems = new HashMap<>();
    private static final Map<String, Integer[]> gearItems = new HashMap<>();

    public static void storePreset(Preset preset) {
        inventoryItems.clear();
        gearItems.clear();
        inventoryItems.putAll(preset.getInventoryItems());
        gearItems.putAll(preset.getGearItems());
    }

    public static Map<String, Integer[]> getInventoryItems() {
        return new HashMap<>(inventoryItems);
    }

    public static Map<String, Integer[]> getGearItems() {
        return new HashMap<>(gearItems);
    }

    public static void clear() {
        inventoryItems.clear();
        gearItems.clear();
    }
}
