import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.utilities.Logger;

public class LocationChecker {

    public boolean isNearCheckpointArea() {
        return Players.getLocal().distance(Constants.CHECKPOINT_AREA) <= Constants.CHECKPOINT_RADIUS;
    }

    public boolean isNearAlternateCheckpoint() {
        return Players.getLocal().distance(Constants.ALTERNATE_CHECKPOINT) <= Constants.ALTERNATE_CHECKPOINT_RADIUS;
    }

    public boolean isNearInsideCheckpoint() {
        return Players.getLocal().distance(Constants.INSIDE_CHECKPOINT) <= Constants.INSIDE_CHECKPOINT_RADIUS;
    }

    public boolean isNearBank() {
        return Players.getLocal().distance(Constants.BANK_DESTINATION) <= Constants.BANK_RADIUS;
    }

}
