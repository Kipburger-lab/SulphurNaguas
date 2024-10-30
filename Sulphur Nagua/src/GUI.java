import java.awt.*;

public class GUI {
    // Chat dimensions and positioning
    private static final int CHAT_START_X = 7;
    private static final int CHAT_START_Y = 345;
    private static final int CHAT_WIDTH = 506;
    private static final int CHAT_HEIGHT = 130;

    // Text positioning
    private static final int TEXT_START_X = CHAT_START_X + 5;
    private static final int FIRST_LINE_Y = CHAT_START_Y + 15;
    private static final int LINE_SPACING = 15;

    // Colors matching OSRS chat
    private static final Color BACKGROUND_COLOR = new Color(0, 0, 0, 150);
    private static final Color BORDER_COLOR = new Color(119, 81, 63);
    private static final Color TEXT_COLOR = new Color(255, 152, 31);
    private static final Color STATUS_COLOR = Color.WHITE;

    public void paint(Graphics2D g, ScriptState currentState, int failedAttempts) {
        // Enable antialiasing for smoother text
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw chat background
        g.setColor(BACKGROUND_COLOR);
        g.fillRect(CHAT_START_X, CHAT_START_Y, CHAT_WIDTH, CHAT_HEIGHT);

        // Draw chat border
        g.setColor(BORDER_COLOR);
        g.drawRect(CHAT_START_X, CHAT_START_Y, CHAT_WIDTH, CHAT_HEIGHT);

        // Set font to match OSRS chat
        g.setFont(new Font("RuneScape Chat", Font.PLAIN, 12));

        // Draw script information
        g.setColor(TEXT_COLOR);
        g.drawString("KLUS Naguas Status", TEXT_START_X, FIRST_LINE_Y);

        g.setColor(STATUS_COLOR);
        g.drawString("Current State: " + formatState(currentState),
                TEXT_START_X, FIRST_LINE_Y + LINE_SPACING);
        g.drawString("Failed Walk Attempts: " + failedAttempts,
                TEXT_START_X, FIRST_LINE_Y + (LINE_SPACING * 2));
    }

    private String formatState(ScriptState state) {
        // Convert WALKING_TO_BANK to "Walking to Bank" for better readability
        String[] words = state.toString().split("_");
        StringBuilder formatted = new StringBuilder();

        for (String word : words) {
            if (formatted.length() > 0) {
                formatted.append(" ");
            }
            formatted.append(word.charAt(0))
                    .append(word.substring(1).toLowerCase());
        }

        return formatted.toString();
    }
}
