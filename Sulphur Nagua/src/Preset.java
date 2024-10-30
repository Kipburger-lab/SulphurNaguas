import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Preset implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final Map<String, Integer[]> inventoryItems;
    private final Map<String, Integer[]> gearItems;

    public Preset(String name, Map<String, Integer[]> inventoryItems,
                  Map<String, Integer[]> gearItems) {
        this.name = name;
        this.inventoryItems = new HashMap<>(inventoryItems);
        this.gearItems = new HashMap<>(gearItems);
    }

    public String getName() {
        return name;
    }

    public Map<String, Integer[]> getInventoryItems() {
        return new HashMap<>(inventoryItems);
    }

    public Map<String, Integer[]> getGearItems() {
        return new HashMap<>(gearItems);
    }

    @Override
    public String toString() {
        return name;
    }
}
