package thunder.hack.features.modules.player;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.SortUtility;

public class AutoSorter extends Module {
    public AutoSorter() {
        super("AutoSorter", Category.PLAYER);
    }

    private final Setting<Page> page = new Setting<>("Page", Page.Main);

    public final Setting<SortUtility.SortBy> sortBy = new Setting<>("SortBy", SortUtility.SortBy.Category, v -> page.is(Page.Main));
    public final Setting<SortUtility.SortArea> sortArea = new Setting<>("SortArea", SortUtility.SortArea.Container, v -> page.is(Page.Main));
    public final Setting<Integer> clickDelay = new Setting<>("ClickDelay", 100, 0, 1000, v -> page.is(Page.Main));
    public final Setting<Boolean> reverse = new Setting<>("Reverse", false, v -> page.is(Page.Main));
    public final Setting<Boolean> showButton = new Setting<>("ShowButton", true, v -> page.is(Page.Main));

    public final Setting<Boolean> autoClose = new Setting<>("AutoClose", false, v -> page.is(Page.Misc));

    private final Timer clickTimer = new Timer();
    private SortUtility.SortSession session;
    private boolean sorting;

    public boolean sorterHovered;

    public enum Page { Main, Misc }

    @Override
    public void onEnable() {
        sorting = false;
        session = null;
    }

    @Override
    public void onDisable() {
        sorting = false;
        session = null;
    }

    public void startSortingTask(ScreenHandler handler) {
        if (!(handler instanceof GenericContainerScreenHandler chest)) return;
        initSort(chest);
    }

    private void initSort(GenericContainerScreenHandler chest) {
        session = SortUtility.createSession(chest, sortArea.getValue());

        if (SortUtility.nonEmptyCount(chest, session) < 2
                || SortUtility.isAlreadySorted(chest, session, sortBy.getValue(), reverse.getValue())) {
            session = null;
            return;
        }

        sorting = true;
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent e) {
        if (mc.player == null || mc.world == null) return;
        ScreenHandler handler = mc.player.currentScreenHandler;
        if (!(handler instanceof GenericContainerScreenHandler chest)) {
            sorting = false;
            session = null;
            return;
        }

        if (!sorting || session == null) return;

        if (!clickTimer.passedMs(clickDelay.getValue())) return;

        if (!handler.getCursorStack().isEmpty()) {
            if (session.swapFrom >= 0) {
                clickSlot(session.swapFrom, SlotActionType.PICKUP);
                session.swapPhase = 0;
            } else {
                clickSlot(0, SlotActionType.PICKUP);
            }
            clickTimer.reset();
            return;
        }

        if (session.swapPhase == 0) {
            if (!SortUtility.scanForSwap(chest, session, sortBy.getValue(), reverse.getValue())) {
                sorting = false;
                session = null;
                if (autoClose.getValue()) mc.player.closeHandledScreen();
                return;
            }
            if (session.swapPhase == 0) {
                clickTimer.reset();
                return;
            }
        }

        if (session.swapPhase == 1) {
            clickSlot(session.swapFrom, SlotActionType.PICKUP);
            session.swapPhase = 2;
        } else if (session.swapPhase == 2) {
            clickSlot(session.swapTo, SlotActionType.PICKUP);
            session.swapPhase = 3;
        } else if (session.swapPhase == 3) {
            clickSlot(session.swapFrom, SlotActionType.PICKUP);
            session.swapPhase = 0;
        }

        clickTimer.reset();
    }
}
