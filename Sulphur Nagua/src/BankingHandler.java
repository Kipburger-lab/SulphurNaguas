import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.utilities.Logger;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class BankingHandler {
    private final Map<String, Integer[]> gearItems;
    private final Map<String, Integer[]> inventoryItems;
    private boolean isGearHandled = false;
    private boolean hasWithdrawnGear = false;
    private boolean bankJustOpened = false;

    public BankingHandler() {
        this.gearItems = PresetStorage.getGearItems();
        this.inventoryItems = PresetStorage.getInventoryItems();
    }

    public boolean handleBanking() {
        if (!bankJustOpened && !Bank.isOpen()) {
            return Bank.open();
        }

        // First time opening bank
        if (!bankJustOpened && Bank.isOpen()) {
            bankJustOpened = true;
            if (shouldDepositItems()) {
                Bank.depositAllItems();
                Bank.depositAllEquipment();
                return false;
            }
        }

        // Handle gear first if needed
        if (!isGearHandled) {
            return handleGearWithdrawal();
        }

        // Handle inventory items
        return handleInventoryWithdrawal();
    }

    private boolean handleGearWithdrawal() {
        // If no gear items to handle, skip to inventory
        if (gearItems.isEmpty() || allGearEquipped()) {
            isGearHandled = true;
            return false;
        }

        // If gear is withdrawn but not equipped
        if (hasWithdrawnGear && Bank.isOpen()) {
            Logger.info("Closing bank to equip gear");
            Bank.close();
            sleep(300, 500);
            return false;
        }

        // If bank is closed and we have items to equip
        if (!Bank.isOpen() && hasWithdrawnGear) {
            Logger.info("Attempting to equip gear");
            if (equipWithdrawnGear()) {
                Logger.info("All gear equipped successfully");
                isGearHandled = true;
                hasWithdrawnGear = false;
                return false;
            }
            return false;
        }

        // Withdraw all missing gear at once
        if (Bank.isOpen() && !hasWithdrawnGear) {
            List<Integer> missingGear = getMissingGear();
            if (!missingGear.isEmpty()) {
                Logger.info("Withdrawing missing gear: " + missingGear.size() + " items");
                for (int itemId : missingGear) {
                    if (Bank.contains(itemId)) {
                        Bank.withdraw(itemId, 1);
                        sleep(100, 200);
                    } else {
                        Logger.error("Missing gear item: " + itemId);
                    }
                }
                hasWithdrawnGear = true;
                return false;
            } else {
                isGearHandled = true;
                return false;
            }
        }

        return false;
    }

    private boolean equipWithdrawnGear() {
        boolean allEquipped = true;
        boolean equippedSomething = false;

        for (Map.Entry<String, Integer[]> entry : gearItems.entrySet()) {
            int itemId = entry.getValue()[0];
            if (!Equipment.contains(itemId) && Inventory.contains(itemId)) {
                Item item = Inventory.get(itemId);
                if (item != null) {
                    Logger.info("Attempting to equip item: " + item.getName());
                    if (item.interact("Wield") || item.interact("Wear") || item.interact("Equip")) {
                        equippedSomething = true;
                        sleep(300, 500);
                    } else {
                        Logger.error("Failed to equip item: " + item.getName());
                        allEquipped = false;
                    }
                }
            }
        }

        if (equippedSomething) {
            sleep(300, 500); // Give time for equipment to update
        }

        return allEquipped;
    }

    private boolean handleInventoryWithdrawal() {
        if (inventoryItems.isEmpty()) {
            return true;
        }

        if (!Bank.isOpen()) {
            return Bank.open();
        }

        boolean allItemsWithdrawn = true;
        boolean withdrawnSomething = false;

        for (Map.Entry<String, Integer[]> entry : inventoryItems.entrySet()) {
            int itemId = entry.getValue()[0];
            int requiredAmount = entry.getValue()[1];

            int currentAmount = Inventory.count(itemId);
            if (currentAmount < requiredAmount) {
                int amountToWithdraw = requiredAmount - currentAmount;
                if (Bank.contains(itemId)) {
                    Bank.withdraw(itemId, amountToWithdraw);
                    withdrawnSomething = true;
                    sleep(100, 200);
                } else {
                    Logger.error("Missing inventory item: " + entry.getKey());
                }
                allItemsWithdrawn = false;
            }
        }

        if (allItemsWithdrawn) {
            Bank.close();
            return true;
        }

        return false;
    }

    private List<Integer> getMissingGear() {
        List<Integer> missingGear = new ArrayList<>();
        for (Map.Entry<String, Integer[]> entry : gearItems.entrySet()) {
            int itemId = entry.getValue()[0];
            if (!Equipment.contains(itemId) && !Inventory.contains(itemId)) {
                missingGear.add(itemId);
            }
        }
        return missingGear;
    }

    private boolean allGearEquipped() {
        return gearItems.values().stream()
                .allMatch(data -> Equipment.contains(data[0]));
    }

    private boolean shouldDepositItems() {
        for (Item item : Inventory.all()) {
            if (item != null && !isItemInPreset(item.getID())) {
                return true;
            }
        }

        for (Item item : Equipment.all()) {
            if (item != null && !isItemInGearPreset(item.getID())) {
                return true;
            }
        }

        return false;
    }

    private boolean isItemInPreset(int itemId) {
        return inventoryItems.values().stream()
                .anyMatch(data -> data[0] == itemId) ||
                gearItems.values().stream()
                        .anyMatch(data -> data[0] == itemId);
    }

    private boolean isItemInGearPreset(int itemId) {
        return gearItems.values().stream()
                .anyMatch(data -> data[0] == itemId);
    }

    private void sleep(int min, int max) {
        try {
            Thread.sleep(org.dreambot.api.methods.Calculations.random(min, max));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
