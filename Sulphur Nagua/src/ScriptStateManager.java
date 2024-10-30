public class ScriptStateManager {
    private static ScriptState currentState;

    public static void setState(ScriptState state) {
        currentState = state;
    }

    public static ScriptState getState() {
        return currentState;
    }
}
