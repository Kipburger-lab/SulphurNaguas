import org.dreambot.api.methods.MethodProvider;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.utilities.Logger;

public class NavigationHandler extends MethodProvider {
    private static final Tile[] CAM_TORUM_PATH = {
            new Tile(1440, 9528, 1), // Starting point near entrance
            new Tile(1442, 9535, 1), // Safe waypoint 1
            new Tile(1445, 9543, 1), // Safe waypoint 2
            new Tile(1448, 9551, 1), // Safe waypoint 3
            new Tile(1451, 9559, 1), // Safe waypoint 4
            new Tile(1453, 9567, 1)  // Bank destination
    };

    private int currentWaypointIndex = 0;
    private long lastWalkTime;
    private Tile lastPosition;
    private int failedWalkAttempts;

    public NavigationHandler() {
        this.lastPosition = Players.getLocal().getTile();
        this.lastWalkTime = System.currentTimeMillis();
        this.failedWalkAttempts = 0;
    }

    public boolean walkToCheckpoint() {
        if (!Players.getLocal().isMoving() || shouldClick()) {
            return Walking.walk(Constants.CHECKPOINT_AREA);
        }
        return false;
    }

    public boolean walkToBank() {
        Tile playerTile = Players.getLocal().getTile();
        Logger.info("Walking to bank. Current position: " + playerTile);

        // Check if we reached current waypoint
        if (shouldMoveToNextWaypoint(playerTile)) {
            currentWaypointIndex++;
            if (currentWaypointIndex >= CAM_TORUM_PATH.length) {
                return true; // Reached final destination
            }
        }

        // Handle movement
        if (!Players.getLocal().isMoving() || shouldClick()) {
            Tile nextWaypoint = CAM_TORUM_PATH[currentWaypointIndex];
            boolean result = Walking.walk(nextWaypoint);

            if (result) {
                Logger.info("Walking to waypoint: " + nextWaypoint);
                lastWalkTime = System.currentTimeMillis();
                resetFailedWalkAttempts();
                return true;
            } else {
                Logger.info("Failed to walk to waypoint, attempting recovery");
                return handleFailedWalk(playerTile);
            }
        }

        return true;
    }

    private boolean shouldMoveToNextWaypoint(Tile playerTile) {
        if (currentWaypointIndex >= CAM_TORUM_PATH.length) {
            return false;
        }

        Tile currentWaypoint = CAM_TORUM_PATH[currentWaypointIndex];
        return playerTile.distance(currentWaypoint) < 3;
    }

    private boolean handleFailedWalk(Tile playerTile) {
        failedWalkAttempts++;
        Logger.info("Walking attempt failed. Total failed attempts: " + failedWalkAttempts);

        // Try to find nearest waypoint if stuck
        int nearestIndex = findNearestWaypointIndex(playerTile);
        if (nearestIndex != currentWaypointIndex) {
            currentWaypointIndex = nearestIndex;
            return Walking.walk(CAM_TORUM_PATH[currentWaypointIndex]);
        }

        // If too many failures, try to reset path
        if (failedWalkAttempts >= Constants.MAX_FAILED_ATTEMPTS) {
            currentWaypointIndex = findNearestWaypointIndex(playerTile);
            failedWalkAttempts = 0;
        }

        return false;
    }

    private int findNearestWaypointIndex(Tile playerTile) {
        int nearestIndex = 0;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < CAM_TORUM_PATH.length; i++) {
            double distance = playerTile.distance(CAM_TORUM_PATH[i]);
            if (distance < minDistance) {
                minDistance = distance;
                nearestIndex = i;
            }
        }

        return nearestIndex;
    }

    public Tile getRandomEntranceTile() {
        return Constants.ENTRANCE_TILES[Calculations.random(0, Constants.ENTRANCE_TILES.length - 1)];
    }

    public boolean enterCamTorum() {
        GameObject entrance = GameObjects.closest(obj ->
                obj != null &&
                        obj.getID() == Constants.ENTRANCE_ID);

        if (entrance != null) {
            if (!entrance.isOnScreen()) {
                Walking.walk(getRandomEntranceTile());
                super.sleep(Calculations.random(800, 1200));
                return false;
            }
            return entrance.interact(Constants.ENTRANCE_ACTION);
        }

        if (!Players.getLocal().isMoving()) {
            Walking.walk(getRandomEntranceTile());
        }
        return false;
    }

    public boolean shouldClick() {
        if (Players.getLocal().isMoving() && Players.getLocal().getAnimation() != -1) {
            return System.currentTimeMillis() - lastWalkTime > Constants.NEXT_CLICK_THRESHOLD;
        }
        return System.currentTimeMillis() - lastWalkTime >
                Calculations.random(Constants.MIN_WAIT_BETWEEN_CLICKS, Constants.MAX_WAIT_BETWEEN_CLICKS);
    }

    public boolean isStuck() {
        Tile currentPosition = Players.getLocal().getTile();
        if (currentPosition.equals(lastPosition) && !Players.getLocal().isMoving()) {
            return System.currentTimeMillis() - lastWalkTime > Constants.STUCK_THRESHOLD;
        }
        updateLastPosition(currentPosition);
        return false;
    }

    private void updateLastPosition(Tile position) {
        this.lastPosition = position;
        this.lastWalkTime = System.currentTimeMillis();
    }

    public void resetFailedWalkAttempts() {
        this.failedWalkAttempts = 0;
        this.lastWalkTime = System.currentTimeMillis();
    }

    public int getFailedWalkAttempts() {
        return this.failedWalkAttempts;
    }
}
