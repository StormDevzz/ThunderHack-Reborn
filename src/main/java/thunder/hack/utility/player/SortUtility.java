package thunder.hack.utility.player;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.*;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;

import java.util.*;

import static thunder.hack.features.modules.Module.mc;

public class SortUtility {

    public enum SortBy {
        Name, Count, Category, Randomize
    }

    public enum SortArea {
        Container, Player, Hotbar, All
    }

    public static class SortSession {
        public final int slotStart;
        public final int slotEnd;
        public final int length;
        public int position;
        public int swapFrom;
        public int swapTo;
        public int swapPhase;

        public SortSession(int slotStart, int slotEnd) {
            this.slotStart = slotStart;
            this.slotEnd = slotEnd;
            this.length = slotEnd - slotStart;
            this.position = 0;
            this.swapFrom = -1;
            this.swapTo = -1;
            this.swapPhase = 0;
        }
    }

    public static int getChestSize(ScreenHandler handler) {
        if (handler instanceof GenericContainerScreenHandler gch)
            return gch.slots.size() - 36;
        return 0;
    }

    public static SortSession createSession(GenericContainerScreenHandler chest, SortArea area) {
        int chestSize = getChestSize(chest);
        int slotStart = switch (area) {
            case Container -> 0;
            case Player -> chestSize;
            case Hotbar -> chestSize + 27;
            case All -> 0;
        };
        int slotEnd = switch (area) {
            case Container -> chestSize;
            case Player -> chestSize + 36;
            case Hotbar -> chestSize + 36;
            case All -> chestSize + 36;
        };
        return new SortSession(slotStart, slotEnd);
    }

    public static int nonEmptyCount(GenericContainerScreenHandler chest, SortSession session) {
        int count = 0;
        for (int i = session.slotStart; i < session.slotEnd; i++)
            if (chest.getSlot(i).hasStack()) count++;
        return count;
    }

    public static boolean isAlreadySorted(GenericContainerScreenHandler chest, SortSession session, SortBy sortBy, boolean reverse) {
        int last = -1;
        for (int i = 0; i < session.length; i++) {
            ItemStack s = chest.getSlot(session.slotStart + i).getStack();
            if (s.isEmpty()) continue;
            if (last >= 0) {
                ItemStack p = chest.getSlot(session.slotStart + last).getStack();
                if (compare(p, s, sortBy, reverse) > 0) return false;
            }
            last = i;
        }
        return true;
    }

    public static boolean scanForSwap(GenericContainerScreenHandler chest, SortSession session, SortBy sortBy, boolean reverse) {
        if (session.position >= session.length) return false;

        int curSlot = session.slotStart + session.position;
        ItemStack curStack = chest.getSlot(curSlot).getStack();

        if (curStack.isEmpty()) {
            session.position++;
            return true;
        }

        int bestSlot = curSlot;
        ItemStack bestStack = curStack;
        for (int j = session.position + 1; j < session.length; j++) {
            int checkSlot = session.slotStart + j;
            ItemStack checkStack = chest.getSlot(checkSlot).getStack();
            if (checkStack.isEmpty()) continue;
            if (compare(checkStack, bestStack, sortBy, reverse) < 0) {
                bestSlot = checkSlot;
                bestStack = checkStack;
            }
        }

        session.position++;

        if (bestSlot == curSlot) return true;

        session.swapFrom = curSlot;
        session.swapTo = bestSlot;
        session.swapPhase = 1;
        return true;
    }

    public static int compare(ItemStack a, ItemStack b, SortBy sortBy, boolean reverse) {
        if (a.isEmpty() && b.isEmpty()) return 0;
        if (a.isEmpty()) return 1;
        if (b.isEmpty()) return -1;

        int cmp = switch (sortBy) {
            case Name -> compareName(a, b);
            case Count -> Integer.compare(b.getCount(), a.getCount());
            case Category -> compareCategory(a, b);
            case Randomize -> (a.hashCode() % 3) - 1;
        };
        return reverse ? -cmp : cmp;
    }

    private static int compareName(ItemStack a, ItemStack b) {
        int cmp = a.getItem().getName().getString().compareToIgnoreCase(b.getItem().getName().getString());
        if (cmp != 0) return cmp;
        return a.getItem().getTranslationKey().compareTo(b.getItem().getTranslationKey());
    }

    private static int compareCategory(ItemStack a, ItemStack b) {
        int ca = itemCategory(a).priority;
        int cb = itemCategory(b).priority;
        if (ca != cb) return Integer.compare(ca, cb);
        return compareName(a, b);
    }

    public enum ItemCategory {
        TOOL(0),
        WEAPON(1),
        RANGED(2),
        ARMOR(3),
        FOOD(4),
        DYE(5),
        MECHANISM(6),
        BLOCK(7),
        DECORATION(8),
        MISC(9);

        public final int priority;
        ItemCategory(int priority) { this.priority = priority; }
    }

    public static ItemCategory itemCategory(ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof PickaxeItem || item instanceof AxeItem || item instanceof ShovelItem || item instanceof HoeItem)
            return ItemCategory.TOOL;

        if (item instanceof SwordItem) return ItemCategory.WEAPON;

        if (item instanceof BowItem || item instanceof CrossbowItem || item instanceof TridentItem)
            return ItemCategory.RANGED;

        if (item instanceof ArmorItem) return ItemCategory.ARMOR;

        if (stack.getComponents().contains(DataComponentTypes.FOOD)) return ItemCategory.FOOD;

        Identifier id = item.getRegistryEntry().registryKey().getValue();
        String path = id.getPath();

        if (path.endsWith("_dye") || path.equals("bone_meal")) return ItemCategory.DYE;

        if (isMechanism(path)) return ItemCategory.MECHANISM;

        if (item instanceof BlockItem) {
            if (isDecoration(path)) return ItemCategory.DECORATION;
            return ItemCategory.BLOCK;
        }

        return ItemCategory.MISC;
    }

    private static boolean isMechanism(String path) {
        return path.contains("piston")
                || path.contains("redstone")
                || path.contains("repeater")
                || path.contains("comparator")
                || path.contains("lamp")
                || path.contains("observer")
                || path.contains("dropper")
                || path.contains("dispenser")
                || path.contains("hopper")
                || path.contains("rail")
                || path.contains("button")
                || path.contains("pressure_plate")
                || path.contains("lever")
                || path.contains("trapdoor")
                || path.contains("door")
                || path.contains("fence_gate")
                || path.equals("tnt")
                || path.equals("target")
                || path.equals("daylight_detector")
                || path.equals("lectern")
                || path.equals("note_block")
                || path.equals("bell")
                || path.equals("beacon");
    }

    private static boolean isDecoration(String path) {
        return path.contains("glass")
                || path.contains("fence")
                || path.contains("wall")
                || path.contains("stair")
                || path.contains("slab")
                || path.contains("carpet")
                || path.contains("wool")
                || path.contains("terracotta")
                || path.contains("concrete")
                || path.contains("glazed")
                || path.contains("banner")
                || path.contains("flower_pot")
                || path.contains("lantern")
                || path.contains("chain")
                || path.contains("anvil")
                || path.contains("grindstone")
                || path.contains("stonecutter")
                || path.contains("campfire")
                || path.contains("torch")
                || path.contains("ladder")
                || path.contains("scaffolding")
                || path.contains("bed")
                || path.contains("shulker_box")
                || path.contains("chest")
                || path.contains("barrel")
                || path.contains("bookshelf")
                || path.contains("painting")
                || path.contains("item_frame");
    }
}
