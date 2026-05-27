package thunder.hack.core.manager.player;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static thunder.hack.features.modules.Module.mc;

public class InventoryManager {
    private int previousSlot = -1;

    public int findItem(Predicate<ItemStack> predicate) {
        for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (predicate.test(stack)) return i;
        }
        return -1;
    }

    public int findItem(Item item) {
        return findItem(stack -> stack.getItem() == item);
    }

    public int findItem(Class<? extends Item> itemClass) {
        return findItem(stack -> itemClass.isInstance(stack.getItem()));
    }

    public int findEmptySlot() {
        for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++) {
            if (mc.player.getInventory().getStack(i).isEmpty()) return i;
        }
        return -1;
    }

    public int countItem(Item item) {
        int count = 0;
        for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == item) count += stack.getCount();
        }
        ItemStack offhand = mc.player.getOffHandStack();
        if (offhand.getItem() == item) count += offhand.getCount();
        return count;
    }

    public int countItems(Predicate<ItemStack> predicate) {
        int count = 0;
        for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (predicate.test(stack)) count += stack.getCount();
        }
        ItemStack offhand = mc.player.getOffHandStack();
        if (predicate.test(offhand)) count += offhand.getCount();
        return count;
    }

    public List<Integer> findAllSlots(Predicate<ItemStack> predicate) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++) {
            if (predicate.test(mc.player.getInventory().getStack(i))) slots.add(i);
        }
        return slots;
    }

    public void switchTo(int slot) {
        previousSlot = mc.player.getInventory().getSelectedSlot();
        mc.player.getInventory().setSelectedSlot(slot);
    }

    public void switchBack() {
        if (previousSlot != -1) {
            mc.player.getInventory().setSelectedSlot(previousSlot);
            previousSlot = -1;
        }
    }

    public boolean isInventoryFull() {
        return findEmptySlot() == -1;
    }

    public int getItemEnchantmentLevel(ItemStack stack, RegistryEntry<Enchantment> enchantment) {
        ItemEnchantmentsComponent ench = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (ench == null) return 0;
        return ench.getLevel(enchantment);
    }

    public boolean hasItemInHotbar(Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) return true;
        }
        return false;
    }

    public int getHotbarItemSlot(Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) return i;
        }
        return -1;
    }

    public boolean hasItemInInventory(Item item) {
        return findItem(i -> i.getItem() == item) != -1;
    }

    public boolean isHolding(Item item) {
        return mc.player.getMainHandStack().getItem() == item;
    }

    public boolean isHolding(Predicate<ItemStack> predicate) {
        return predicate.test(mc.player.getMainHandStack());
    }

    public int getTotalSlots() {
        return PlayerInventory.MAIN_SIZE;
    }

    public boolean isInventoryEmpty() {
        for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++) {
            if (!mc.player.getInventory().getStack(i).isEmpty()) return false;
        }
        return true;
    }
}
