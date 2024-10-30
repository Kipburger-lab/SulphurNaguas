import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.MethodProvider;
import javax.swing.SwingUtilities;
import java.awt.*;

@ScriptManifest(
        name = "KLUS Naguas",
        description = "Kills Sulphur Naguas",
        author = "KLUS",
        version = 1.0,
        category = Category.MISC,
        image = ""
)
public class MainScript extends AbstractScript {
    private ScriptState currentState;
    private GUI gui;
    private NavigationHandler navigationHandler;
    private NPCInteractionHandler npcHandler;
    private LocationChecker locationChecker;
    private BankingHandler bankingHandler;
    private boolean guiShown = false;
    private boolean waitingForPresetSelection = false;

    @Override
    public void onStart() {
        Logger.info("Starting script!");
        initializeComponents();
        determineStartingState();
    }

    private void initializeComponents() {
        gui = new GUI();
        navigationHandler = new NavigationHandler();
        npcHandler = new NPCInteractionHandler();
        locationChecker = new LocationChecker();
        bankingHandler = new BankingHandler();

        if (!Walking.isRunEnabled() && Walking.getRunEnergy() > 20) {
            Walking.toggleRun();
        }
    }

    private void determineStartingState() {
        Tile playerTile = Players.getLocal().getTile();
        if (playerTile.getZ() == 1) {
            currentState = ScriptState.WALKING_TO_BANK;
            Logger.info("Starting inside Cam Torum - walking to bank");
        } else if (locationChecker.isNearAlternateCheckpoint()) {
            currentState = ScriptState.WALKING_TO_ENTRANCE;
            Logger.info("Starting near Cam Torum entrance");
        } else if (locationChecker.isNearCheckpointArea()) {
            currentState = ScriptState.WALKING;
            Logger.info("Starting from checkpoint area");
        } else {
            currentState = ScriptState.WALKING;
            Logger.info("Starting from unknown location - walking to checkpoint");
        }
    }

    @Override
    public int onLoop() {
        if (ScriptStateManager.getState() == ScriptState.BANKING && currentState != ScriptState.BANKING) {
            currentState = ScriptState.BANKING;
            waitingForPresetSelection = false;
            bankingHandler = new BankingHandler();
        }

        switch (currentState) {
            case WALKING:
                return handleWalkingState();
            case INTERACTING_RENU:
                return handleInteractingRenuState();
            case SELECTING_DESTINATION:
                return handleSelectingDestinationState();
            case WALKING_TO_ENTRANCE:
                return handleWalkingToEntranceState();
            case ENTERING_CAM_TORUM:
                return handleEnteringCamTorumState();
            case WAITING_INSIDE:
                return handleWaitingInsideState();
            case WALKING_TO_BANK:
                return handleWalkingToBankState();
            case BANKING:
                return handleBankingState();
            case IDLE:
                Logger.info("Task completed!");
                break;
        }
        return Calculations.random(200, 400);
    }

    private int handleWalkingState() {
        if (locationChecker.isNearCheckpointArea()) {
            Logger.info("Near checkpoint area - looking for Renu!");
            currentState = ScriptState.INTERACTING_RENU;
            return Calculations.random(600, 1200);
        }

        if (locationChecker.isNearAlternateCheckpoint()) {
            Logger.info("Near alternate checkpoint - moving to entrance!");
            currentState = ScriptState.WALKING_TO_ENTRANCE;
            return Calculations.random(600, 1200);
        }

        if (navigationHandler.walkToCheckpoint()) {
            return Calculations.random(Constants.MIN_WAIT_BETWEEN_CLICKS,
                    Constants.MAX_WAIT_BETWEEN_CLICKS);
        }
        return Calculations.random(600, 1200);
    }

    private int handleInteractingRenuState() {
        if (npcHandler.interactWithRenu()) {
            Logger.info("Interacting with Renu!");
            currentState = ScriptState.SELECTING_DESTINATION;
            return Calculations.random(1200, 2000);
        }
        if (!locationChecker.isNearCheckpointArea()) {
            currentState = ScriptState.WALKING;
        }
        return Calculations.random(600, 1200);
    }

    private int handleSelectingDestinationState() {
        if (npcHandler.selectTravelDestination()) {
            Logger.info("Selected Cam Torum Entrance!");
            currentState = ScriptState.WALKING_TO_ENTRANCE;
            return Calculations.random(2000, 3000);
        }
        return Calculations.random(600, 1200);
    }

    private int handleWalkingToEntranceState() {
        Tile targetTile = navigationHandler.getRandomEntranceTile();
        if (Players.getLocal().distance(targetTile) <= 5) {
            Logger.info("Reached Cam Torum entrance!");
            currentState = ScriptState.ENTERING_CAM_TORUM;
            return Calculations.random(600, 1200);
        }

        if (!Players.getLocal().isMoving() || navigationHandler.shouldClick()) {
            Walking.walk(targetTile);
            return Calculations.random(Constants.MIN_WAIT_BETWEEN_CLICKS,
                    Constants.MAX_WAIT_BETWEEN_CLICKS);
        }
        return Calculations.random(600, 1200);
    }

    private int handleEnteringCamTorumState() {
        if (navigationHandler.enterCamTorum()) {
            Logger.info("Entering Cam Torum!");
            currentState = ScriptState.WAITING_INSIDE;
            return Calculations.random(1200, 2000);
        }
        return Calculations.random(600, 1200);
    }

    private int handleWaitingInsideState() {
        if (locationChecker.isNearInsideCheckpoint()) {
            Logger.info("Inside checkpoint reached - moving to bank!");
            sleep(Calculations.random(600, 1200));
            currentState = ScriptState.WALKING_TO_BANK;
            navigationHandler.resetFailedWalkAttempts();
            return Calculations.random(600, 1200);
        }
        return Calculations.random(600, 1000);
    }

    private int handleWalkingToBankState() {
        Tile playerTile = Players.getLocal().getTile();
        double distanceToBank = playerTile.distance(Constants.BANK_DESTINATION);

        Logger.info("Player position: " + playerTile);
        Logger.info("Bank position: " + Constants.BANK_DESTINATION);
        Logger.info("Distance to bank: " + distanceToBank);
        Logger.info("Current state: " + currentState);
        Logger.info("GUI shown: " + guiShown);

        if (distanceToBank <= 5) {
            Logger.info("Near bank detected!");
            if (!guiShown && !waitingForPresetSelection) {
                Logger.info("Attempting to show PresetGUI");
                try {
                    SwingUtilities.invokeLater(() -> {
                        PresetGUI presetGui = new PresetGUI();
                        presetGui.setVisible(true);
                        Logger.info("PresetGUI should be visible now");
                    });
                    guiShown = true;
                    waitingForPresetSelection = true;
                    Logger.info("GUI shown flag set to true");
                } catch (Exception e) {
                    Logger.error("Error showing GUI: " + e.getMessage());
                }
            }
            return Calculations.random(600, 1200);
        }

        if (!Players.getLocal().isMoving() || navigationHandler.shouldClick()) {
            if (navigationHandler.walkToBank()) {
                return Calculations.random(Constants.MIN_WAIT_BETWEEN_CLICKS,
                        Constants.MAX_WAIT_BETWEEN_CLICKS);
            } else {
                Logger.info("Failed to walk to bank, retrying...");
                sleep(Calculations.random(800, 1200));
            }
        }
        return Calculations.random(600, 1200);
    }

    private int handleBankingState() {
        if (!locationChecker.isNearBank()) {
            currentState = ScriptState.WALKING_TO_BANK;
            return Calculations.random(600, 1200);
        }

        if (bankingHandler.handleBanking()) {
            currentState = ScriptState.IDLE;
            Logger.info("Banking completed successfully!");
            return Calculations.random(600, 1200);
        }

        return Calculations.random(600, 1200);
    }

    @Override
    public void onPaint(Graphics2D g) {
        if (gui != null) {
            gui.paint(g, currentState, navigationHandler.getFailedWalkAttempts());
        }
    }
}
