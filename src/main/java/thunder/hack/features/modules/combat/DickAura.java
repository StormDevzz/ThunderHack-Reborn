package thunder.hack.features.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ItemSelectSetting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;

import java.util.*;

public class DickAura extends Module {
    public DickAura() {
        super("DickAura", Category.COMBAT);
    }

    // Основные настройки
    private final Setting<Float> range = new Setting<>("Range", 5f, 1f, 10f);
    private final Setting<Integer> buildDelay = new Setting<>("Delay", 50, 0, 500);
    private final Setting<Boolean> switchBack = new Setting<>("SwitchBack", true);
    private final Setting<Boolean> autoDisable = new Setting<>("AutoDisable", true);
    private final Setting<Boolean> ignoreFriends = new Setting<>("IgnoreFriends", true);
    private final Setting<Boolean> packetPlace = new Setting<>("PacketPlace", false);
    private final Setting<Integer> shaftLength = new Setting<>("ShaftLength", 4, 2, 12);
    private final Setting<Integer> distance = new Setting<>("Distance", 2, 1, 5);

    // Мемные настройки
    private final Setting<Boolean> peeMode = new Setting<>("PeeMode", false);
    private final Setting<Boolean> announce = new Setting<>("Announce", true);
    private final Setting<Mode> announceMode = new Setting<>("AnnounceMode", Mode.Public, v -> announce.getValue());
    private final Setting<Boolean> danceMode = new Setting<>("DanceMode", false);
    private final Setting<Boolean> curseTarget = new Setting<>("CurseTarget", false);

    private final Setting<ItemSelectSetting> blocks = new Setting<>("Blocks", new ItemSelectSetting(
        new ArrayList<>(List.of(
            "pink_concrete", "red_concrete", "magenta_concrete", "white_concrete",
            "pink_wool", "red_wool", "white_wool",
            "pink_terracotta", "white_terracotta", "red_terracotta"
        ))
    ));

    private enum Mode { Public, Private, Whisper }

    private final Timer timer = new Timer();
    private final Timer danceTimer = new Timer();
    private final List<BlockPos> currentBuild = new ArrayList<>();
    private PlayerEntity target;
    private int buildIndex;
    private int failedAttempts;
    private int dancePhase;
    private BlockPos danceCenter;
    private int danceDir = 1;

    @Override
    public void onEnable() {
        target = null;
        currentBuild.clear();
        buildIndex = 0;
        failedAttempts = 0;
        dancePhase = 0;
        danceCenter = null;
        timer.reset();
        danceTimer.reset();
    }

    @Override
    public void onDisable() {
        target = null;
        currentBuild.clear();
        danceCenter = null;
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent e) {
        if (mc.player == null || mc.world == null) return;

        // Dance mode - движение члена влево-вправо
        if (danceMode.getValue() && danceCenter != null && danceTimer.passedMs(500)) {
            danceDir *= -1;
            danceTimer.reset();
            // Перестраиваем член на новой позиции
            currentBuild.clear();
            buildIndex = 0;
            failedAttempts = 0;
            generateDickAt(danceCenter.add(danceDir * 2, 0, 0));
        }

        if (target == null || !isValidTarget(target)) {
            target = findTarget();
            if (target == null) return;
            currentBuild.clear();
            buildIndex = 0;
            failedAttempts = 0;
            
            if (announce.getValue()) {
                announceTarget(target);
            }
            
            if (curseTarget.getValue()) {
                curse(target);
            }
            
            generateDick(target);
        }

        if (buildIndex >= currentBuild.size()) {
            if (danceMode.getValue() && danceCenter != null) {
                // Не отключаем в режиме танца
                buildIndex = 0;
                currentBuild.clear();
                generateDickAt(danceCenter);
                return;
            }
            
            if (autoDisable.getValue()) {
                disable();
            } else {
                target = null;
            }
            return;
        }

        if (!timer.passedMs(buildDelay.getValue())) return;

        SearchInvResult blockResult = findBlockFromSelection();
        if (!blockResult.found()) {
            sendMessage("No blocks found!");
            disable();
            return;
        }

        BlockPos pos = currentBuild.get(buildIndex);

        // Pee mode - желтые блоки внизу
        if (peeMode.getValue() && buildIndex == 0) {
            // Ставим желтый блок под основанием
            BlockPos peePos = pos.down();
            if (mc.world.getBlockState(peePos).isReplaceable()) {
                SearchInvResult yellowBlock = findYellowBlock();
                if (yellowBlock.found()) {
                    int prevSlot = mc.player.getInventory().selectedSlot;
                    yellowBlock.switchTo();
                    InteractionUtility.placeBlock(peePos,
                        InteractionUtility.Rotate.None,
                        InteractionUtility.Interact.AirPlace,
                        InteractionUtility.PlaceMode.Normal,
                        true);
                    if (switchBack.getValue()) InventoryUtility.switchTo(prevSlot);
                }
            }
        }

        int prevSlot = mc.player.getInventory().selectedSlot;
        blockResult.switchTo();

        boolean placed = InteractionUtility.placeBlock(pos,
            InteractionUtility.Rotate.None,
            InteractionUtility.Interact.AirPlace,
            packetPlace.getValue() ? InteractionUtility.PlaceMode.Packet : InteractionUtility.PlaceMode.Normal,
            true);

        if (switchBack.getValue()) {
            InventoryUtility.switchTo(prevSlot);
        }

        if (placed) {
            buildIndex++;
            failedAttempts = 0;
        } else {
            failedAttempts++;
            if (failedAttempts > 20) {
                sendMessage("Failed to build!");
                disable();
            }
        }

        timer.reset();
    }

    private void announceTarget(PlayerEntity target) {
        String msg = switch (announceMode.getValue()) {
            case Public -> target.getName().getString() + " has a small dick!";
            case Private -> "/msg " + target.getName().getString() + " Nice cock bro!";
            case Whisper -> "/w " + target.getName().getString() + " I built a monument for you!";
        };
        mc.player.networkHandler.sendChatMessage(msg);
    }

    private void curse(PlayerEntity target) {
        mc.player.networkHandler.sendChatMessage("/curse " + target.getName().getString() + " dick_aura");
    }

    private SearchInvResult findYellowBlock() {
        String[] yellowBlocks = {"yellow_concrete", "yellow_wool", "yellow_terracotta", "yellow_stained_glass"};
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            String itemId = stack.getItem().getTranslationKey()
                .replace("item.minecraft.", "")
                .replace("block.minecraft.", "");
            for (String yellow : yellowBlocks) {
                if (itemId.equals(yellow)) {
                    return new SearchInvResult(i, true, stack);
                }
            }
        }
        return SearchInvResult.notFound();
    }

    private SearchInvResult findBlockFromSelection() {
        List<String> selectedItems = blocks.getValue().getItemsById();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            String itemId = stack.getItem().getTranslationKey()
                .replace("item.minecraft.", "")
                .replace("block.minecraft.", "");
            if (selectedItems.contains(itemId)) {
                return new SearchInvResult(i, true, stack);
            }
        }
        return SearchInvResult.notFound();
    }

    private boolean isValidTarget(PlayerEntity player) {
        if (player == null || player == mc.player) return false;
        if (!player.isAlive()) return false;
        if (ModuleManager.antiBot.isEnabled() && AntiBot.bots.contains(player)) return false;
        if (ignoreFriends.getValue() && Managers.FRIEND.isFriend(player)) return false;
        return mc.player.distanceTo(player) <= range.getValue();
    }

    private PlayerEntity findTarget() {
        return mc.world.getPlayers().stream()
            .filter(this::isValidTarget)
            .min(Comparator.comparingDouble(p -> mc.player.distanceTo(p)))
            .orElse(null);
    }

    private void generateDick(PlayerEntity target) {
        BlockPos targetPos = target.getBlockPos();
        Vec3d playerPos = mc.player.getPos();
        Vec3d targetVec = target.getPos();

        double dx = targetVec.x - playerPos.x;
        double dz = targetVec.z - playerPos.z;
        int dist = distance.getValue();

        boolean buildAlongX = Math.abs(dx) > Math.abs(dz);

        int centerX = targetPos.getX();
        int centerZ = targetPos.getZ();

        if (buildAlongX) {
            centerZ = targetPos.getZ() + (dz > 0 ? dist : -dist);
        } else {
            centerX = targetPos.getX() + (dx > 0 ? dist : -dist);
        }

        danceCenter = new BlockPos(centerX, targetPos.getY(), centerZ);
        generateDickAt(danceCenter);
    }

    private void generateDickAt(BlockPos center) {
        currentBuild.clear();
        int shaft = shaftLength.getValue();
        int startY = center.getY() + 1;
        int centerX = center.getX();
        int centerZ = center.getZ();

        // Ствол (shaft блоков вертикально)
        for (int y = 0; y < shaft; y++) {
            currentBuild.add(new BlockPos(centerX, startY + y, centerZ));
        }

        // Головка (горизонтальная линия из 3 блоков над стволом)
        int headY = startY + shaft;
        currentBuild.add(new BlockPos(centerX - 1, headY, centerZ));
        currentBuild.add(new BlockPos(centerX, headY, centerZ));
        currentBuild.add(new BlockPos(centerX + 1, headY, centerZ));

        // Сортируем снизу вверх
        currentBuild.sort(Comparator.comparingInt(BlockPos::getY));
    }

    @Override
    public String getDisplayInfo() {
        if (target == null) return "";
        return target.getName().getString() + " " + buildIndex + "/" + currentBuild.size();
    }
}
//VFedTerV :3