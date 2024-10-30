import org.dreambot.api.methods.map.Tile;

public class Constants {
    // Location Constants
    public static final Tile VARROCK_TELE_AREA = new Tile(3212, 3424, 0);
    public static final Tile CHECKPOINT_AREA = new Tile(1700, 3141, 0);
    public static final Tile ALTERNATE_CHECKPOINT = new Tile(1449, 3106, 0);
    public static final Tile INSIDE_CHECKPOINT = new Tile(1439, 9509, 1);
    public static final Tile BANK_DESTINATION = new Tile(1453, 9567, 1);
    public static final Tile[] ENTRANCE_TILES = {
            new Tile(1435, 3128, 0),
            new Tile(1436, 3128, 0)
    };

    // Game Object Constants
    public static final int ENTRANCE_ID = 51375;
    public static final String ENTRANCE_ACTION = "Pass-through";

    // Timing Constants
    public static final int MIN_WAIT_BETWEEN_CLICKS = 2000;
    public static final int MAX_WAIT_BETWEEN_CLICKS = 4000;
    public static final int STUCK_THRESHOLD = 5000;
    public static final int NEXT_CLICK_THRESHOLD = 400;

    // Distance Constants
    public static final int CHECKPOINT_RADIUS = 50;
    public static final int ALTERNATE_CHECKPOINT_RADIUS = 30;
    public static final int INSIDE_CHECKPOINT_RADIUS = 10;
    public static final int BANK_RADIUS = 5;  // Increased for testing

    // Navigation Constants
    public static final int MAX_FAILED_ATTEMPTS = 3;
    public static final int MIN_STEP_SIZE = 20;
    public static final int MAX_STEP_SIZE = 25;
}
