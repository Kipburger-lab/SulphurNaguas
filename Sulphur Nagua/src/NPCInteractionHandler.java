import org.dreambot.api.methods.MethodProvider;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.dreambot.api.methods.Calculations;

public class NPCInteractionHandler extends MethodProvider {
    public boolean interactWithRenu() {
        NPC renu = NPCs.closest(npc -> {
            if (npc == null || npc.getName() == null) return false;
            return npc.getName().equals("Renu");
        });

        if (renu != null && renu.exists()) {
            return handleRenuInteraction(renu);
        }

        if (!Players.getLocal().isMoving()) {
            Walking.walk(Constants.CHECKPOINT_AREA);
        }
        return false;
    }

    private boolean handleRenuInteraction(NPC renu) {
        if (!renu.isOnScreen()) {
            Walking.walk(renu.getTile());
            super.sleep(Calculations.random(800, 1200));
            return false;
        }
        return renu.interact("Travel");
    }

    public boolean selectTravelDestination() {
        WidgetChild travelDialog = Widgets.getWidgetChild(874, 15);
        if (travelDialog != null && travelDialog.isVisible()) {
            WidgetChild option = Widgets.getWidgetChild(874, 15, 4);
            if (option != null && option.isVisible()) {
                option.interact();
                super.sleep(Calculations.random(600, 1000));
                return true;
            }
        }
        return false;
    }
}
