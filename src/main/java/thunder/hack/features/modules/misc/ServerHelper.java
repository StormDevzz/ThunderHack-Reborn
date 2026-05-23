package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.StringUtils;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.gui.notification.Notification;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.utility.ThunderUtility;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;
import java.util.*;
import java.util.List;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class ServerHelper extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Jenro);

    public ServerHelper() {
        super("ServerHelper", Category.MISC);
    }

    // Jenro settings
    private final Setting<Boolean> photomath = new Setting<>("PhotoMath", false, v -> mode.is(Mode.Jenro));
    private final Setting<Boolean> antiTpHere = new Setting<>("AntiTpHere", false, v -> mode.is(Mode.Jenro));
    private final Setting<Boolean> clanInvite = new Setting<>("ClanInvite", false, v -> mode.is(Mode.Jenro));
    private final Setting<Integer> clanInviteDelay = new Setting<>("InviteDelay", 10, 1, 30, v -> mode.is(Mode.Jenro) && clanInvite.getValue());
    private final Setting<Boolean> fixAll = new Setting<>("/fix all", true, v -> mode.is(Mode.Jenro));
    private final Setting<Boolean> feed = new Setting<>("/feed", true, v -> mode.is(Mode.Jenro));
    private final Setting<Boolean> near = new Setting<>("/near", true, v -> mode.is(Mode.Jenro));
    private final Setting<Boolean> airDropWay = new Setting<>("AirDropWay", true, v -> mode.is(Mode.Jenro));
    private final Setting<Boolean> farmilka = new Setting<>("Farmilka", true, v -> mode.is(Mode.Jenro));
    public final Setting<Boolean> trueSight = new Setting<>("TrueSight", true, v -> mode.is(Mode.Jenro));
    private final Setting<Boolean> spek = new Setting<>("SpekNotify", true, v -> mode.is(Mode.Jenro));
    private final Setting<Bind> desorient = new Setting<>("Desorient", new Bind(-1, false, false), v -> mode.is(Mode.Jenro));
    private final Setting<Bind> trap = new Setting<>("Trap", new Bind(-1, false, false), v -> mode.is(Mode.Jenro));
    public final Setting<Boolean> aucHelper = new Setting<>("AucHelper", true, v -> mode.is(Mode.Jenro));
    private final Setting<GroupBy> groupBy = new Setting<>("GroupBy", GroupBy.ItemType, v -> mode.is(Mode.Jenro) && aucHelper.getValue());
    private final Setting<Integer> contrast = new Setting<>("Contrast", 4, 1, 15, v -> mode.is(Mode.Jenro) && aucHelper.getValue());

    // Anarchy settings
    private final Setting<Boolean> autoKit = new Setting<>("AutoKit", false, v -> mode.is(Mode.Anarchy));
    private final Setting<String> kitCommand = new Setting<>("KitCommand", "kit starter", v -> mode.is(Mode.Anarchy) && autoKit.getValue());
    private final Setting<Boolean> autoMessage = new Setting<>("AutoMessage", false, v -> mode.is(Mode.Anarchy));
    private final Setting<String> messageText = new Setting<>("Message", "!", v -> mode.is(Mode.Anarchy) && autoMessage.getValue());
    private final Setting<Integer> messageDelay = new Setting<>("MessageDelay", 5, 1, 30, v -> mode.is(Mode.Anarchy) && autoMessage.getValue());
    private final Setting<Boolean> coordLog = new Setting<>("CoordLog", false, v -> mode.is(Mode.Anarchy));
    private final Setting<Integer> coordLogInterval = new Setting<>("CoordLogInterval", 120, 10, 600, v -> mode.is(Mode.Anarchy) && coordLog.getValue());
    private final Setting<Boolean> antiSpawnKill = new Setting<>("AntiSpawnKill", false, v -> mode.is(Mode.Anarchy));
    private final Setting<Boolean> askGapple = new Setting<>("Gapple", true, v -> mode.is(Mode.Anarchy) && antiSpawnKill.getValue());
    private final Setting<Boolean> askSafeMove = new Setting<>("SafeMove", false, v -> mode.is(Mode.Anarchy) && antiSpawnKill.getValue());
    private final Setting<Integer> askSafeTime = new Setting<>("SafeTime", 2, 1, 5, v -> mode.is(Mode.Anarchy) && antiSpawnKill.getValue() && askSafeMove.getValue());
    private final Setting<Boolean> autoRocket = new Setting<>("AutoRocket", false, v -> mode.is(Mode.Anarchy));
    private final Setting<Integer> rocketDelay = new Setting<>("RocketDelay", 300, 100, 2000, v -> mode.is(Mode.Anarchy) && autoRocket.getValue());
    private final Setting<Boolean> rocketOnlyJump = new Setting<>("OnlyOnJump", true, v -> mode.is(Mode.Anarchy) && autoRocket.getValue());
    private final Setting<Boolean> rocketOnlyRockets = new Setting<>("OnlyWithRockets", false, v -> mode.is(Mode.Anarchy) && autoRocket.getValue());

    private enum GroupBy {
        Name, ItemType
    }

    public enum Mode {
        Jenro, Anarchy
    }

    private final Timer pvpTimer = new Timer();
    private final Timer inviteTimer = new Timer();
    private final Timer atphtimer = new Timer();
    private final Timer checktimer = new Timer();
    private List<AucItem> result = new ArrayList<>();

    private final Timer disorientTimer = new Timer();
    private final Timer trapTimer = new Timer();

    // Anarchy timers
    private final Timer messageTimer = new Timer();
    private final Timer coordTimer = new Timer();
    private final Timer spawnTimer = new Timer();
    private final Timer rocketTimer = new Timer();

    private boolean flag = false;
    private boolean wasDead = false;
    private boolean hasSentMessage = false;
    private boolean hasSpawned = false;


    @Override
    public void onEnable() {
        wasDead = mc.player != null && (mc.player.getHealth() <= 0 || mc.player.isDead());
        hasSentMessage = false;
        hasSpawned = false;
        messageTimer.reset();
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (!mode.is(Mode.Jenro)) return;

        if (event.getPacket() instanceof GameMessageS2CPacket pac) {
            if (spek.getValue()) {
                String content = pac.content().getString().toLowerCase();
                if (content.contains("спек") || content.contains("ызус") || content.contains("spec") || content.contains("spek") || content.contains("ызул")) {
                    String name = ThunderUtility.solveName(pac.content().getString());
                    Managers.NOTIFICATION.publicity("SpekNotification", isRu() ? name + " хочет чтобы за ним проследили" : name + " wants to be followed", 3, Notification.Type.WARNING);
                }
            }

            if (photomath.getValue())
                if (pac.content().getString().contains("Решите: ") && Objects.equals(ThunderUtility.solveName(pac.content().getString()), "FATAL ERROR"))
                    try {
                        mc.player.networkHandler.sendChatMessage(String.valueOf(Integer.parseInt(StringUtils.substringBetween(pac.content().getString(), "Решите: ", " + ")) + Integer.parseInt(StringUtils.substringBetween(pac.content().getString(), " + ", " кто первый"))));
                    } catch (Exception ignored) {
                    }

            if (antiTpHere.getValue()) {
                if (pac.content().getString().contains("Телепортирование...") && check(pac.content().getString())) {
                    flag = true;
                    atphtimer.reset();
                }
            }

            if (airDropWay.getValue() && pac.content().getString().contains("Аирдроп")) {
                try {
                    int xCord = Integer.parseInt(StringUtils.substringBetween(pac.content().getString(), "координаты X: ", " Y:"));
                    int yCord = Integer.parseInt(StringUtils.substringBetween(pac.content().getString(), "Y: ", " Z:"));
                    int zCord = Integer.parseInt(StringUtils.substringBetween(pac.content().getString() + "nigga", "Z: ", "nigga"));
                    ThunderHack.gps_position = new BlockPos(xCord, yCord, zCord);
                    Managers.NOTIFICATION.publicity("FGHelper", "Поставлена метка на аирдроп! X: " + xCord + " Y: " + yCord + " Z: " + zCord, 5, Notification.Type.SUCCESS);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) return;

        if (mode.is(Mode.Jenro)) {
            updateJenro();
        } else {
            updateAnarchy();
        }
    }

    private void updateJenro() {
        if (flag && atphtimer.passedMs(100) && antiTpHere.getValue()) {
            StringBuilder log = new StringBuilder("Тебя телепортировали в X: " + (int) mc.player.getX() + " Z: " + (int) mc.player.getZ() + ". Ближайшие игроки : ");

            for (PlayerEntity entity : mc.world.getPlayers()) {
                if (entity == mc.player) continue;
                log.append(entity.getName().getString()).append(" ");
            }
            sendMessage(String.valueOf(log));

            mc.player.networkHandler.sendChatMessage("/back");
            flag = false;
        }

        if (inviteTimer.passedS(clanInviteDelay.getValue()) && clanInvite.getValue()) {
            ArrayList<String> playersNames = new ArrayList<>();
            for (PlayerListEntry player : mc.player.networkHandler.getPlayerList()) {
                playersNames.add(player.getProfile().name());
            }
            if (playersNames.size() > 1) {
                int randomName = (int) Math.floor(Math.random() * playersNames.size());
                mc.player.networkHandler.sendChatMessage("/c invite " + playersNames.get(randomName));
                playersNames.clear();
                inviteTimer.reset();
            }
        }

        if (feed.getValue() && mc.player.getHungerManager().getFoodLevel() < 8 && canSendCommand())
            mc.player.networkHandler.sendChatCommand("feed");

        if (fixAll.getValue() && canSendCommand())
            mc.player.networkHandler.sendChatCommand("fix all");

        if (mc.player.hurtTime > 0)
            pvpTimer.reset();

        if (near.getValue() && mc.player.age % 30 == 0)
            mc.player.networkHandler.sendChatCommand("near");

        if (farmilka.getValue()) {
            for (Entity ent : Managers.ASYNC.getAsyncEntities()) {
                if (ent instanceof PlayerEntity) continue;
                if (ent instanceof LivingEntity) {
                    if (((LivingEntity) ent).isDead())
                        mc.world.removeEntity(ent.getId(), Entity.RemovalReason.KILLED);
                }
            }
        }

        if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler chest && aucHelper.getValue()) {
            if (mc.currentScreen.getTitle().getString().contains("Аукцион") || mc.currentScreen.getTitle().getString().contains("Поиск")) {

                result.clear();
                Map<String, Integer> itemMap = new HashMap<>();

                int slot = 0;

                for (ItemStack itemStack : chest.getStacks()) {
                    if (slot > 44)
                        continue;

                    int price = getPrice(itemStack);

                    if (itemStack.getCount() > 1)
                        price /= itemStack.getCount();

                    itemMap.put(getKey(itemStack), Math.min(itemMap.getOrDefault(getKey(itemStack), 999999999), price));
                    slot++;
                }

                slot = 0;
                for (ItemStack itemStack : chest.getStacks()) {
                    if (itemMap.get(getKey(itemStack)) != null) {

                        int price = getPrice(itemStack);

                        if (itemStack.getCount() > 1)
                            price /= itemStack.getCount();

                        result.add(new AucItem(itemStack.getItem(), price, itemMap.get(getKey(itemStack)), slot));
                    }
                    slot++;
                }
            }
        }
    }

    private void updateAnarchy() {
        boolean isDead = mc.player.getHealth() <= 0 || mc.player.isDead();
        if (!isDead && wasDead) {
            if (autoKit.getValue()) {
                mc.player.networkHandler.sendChatCommand(kitCommand.getValue());
                Managers.NOTIFICATION.publicity("AutoKit", "Kit claimed: " + kitCommand.getValue(), 3, Notification.Type.SUCCESS);
            }
            if (antiSpawnKill.getValue()) {
                hasSpawned = true;
                spawnTimer.reset();
            }
        }
        wasDead = isDead;

        if (hasSpawned && antiSpawnKill.getValue()) {
            if (askGapple.getValue()) {
                SearchInvResult gapple = InventoryUtility.findItemInHotBar(Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE);
                SearchInvResult gappleInv = InventoryUtility.findItemInInventory(Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE);
                if (gapple.found()) {
                    InventoryUtility.saveAndSwitchTo(gapple.slot());
                    sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
                    InventoryUtility.returnSlot();
                } else if (gappleInv.found()) {
                    clickSlot(gappleInv.slot(), mc.player.getInventory().getSelectedSlot(), SlotActionType.SWAP);
                    sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
                    clickSlot(gappleInv.slot(), mc.player.getInventory().getSelectedSlot(), SlotActionType.SWAP);
                    sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                }
            }
            hasSpawned = false;
        }

        if (askSafeMove.getValue() && antiSpawnKill.getValue() && !spawnTimer.passedS(askSafeTime.getValue())) {
            mc.player.input.playerInput = new net.minecraft.util.PlayerInput(false, false, false, false, false, false, false);
        }

        if (autoMessage.getValue() && !hasSentMessage) {
            if (messageTimer.passedS(messageDelay.getValue())) {
                mc.player.networkHandler.sendChatMessage(messageText.getValue());
                hasSentMessage = true;
            }
        }

        if (coordLog.getValue() && coordTimer.every(coordLogInterval.getValue() * 1000L)) {
            String coords = String.format("X: %.1f Y: %.1f Z: %.1f | %s",
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    mc.world.getRegistryKey().getValue().toString());
            sendMessage("[CoordLog] " + coords);
        }

        if (autoRocket.getValue() && mc.player.isGliding() && rocketTimer.every(rocketDelay.getValue().longValue())) {
            if (rocketOnlyJump.getValue() && !mc.options.jumpKey.isPressed())
                return;

            SearchInvResult rocket = InventoryUtility.findItemInHotBar(Items.FIREWORK_ROCKET);
            if (rocket.found()) {
                InventoryUtility.saveAndSwitchTo(rocket.slot());
                sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
                InventoryUtility.returnSlot();
            } else if (!rocketOnlyRockets.getValue()) {
                SearchInvResult rocketInv = InventoryUtility.findItemInInventory(Items.FIREWORK_ROCKET);
                if (rocketInv.found()) {
                    clickSlot(rocketInv.slot(), mc.player.getInventory().getSelectedSlot(), SlotActionType.SWAP);
                    sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
                    clickSlot(rocketInv.slot(), mc.player.getInventory().getSelectedSlot(), SlotActionType.SWAP);
                    sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                }
            }
        }
    }

    private boolean canSendCommand() {
        if (pvpTimer.passedMs(30000)) {
            pvpTimer.reset();
            return true;
        }
        return false;
    }

    public boolean check(String checkstring) {
        return checktimer.passedMs(3000) && (Objects.equals(ThunderUtility.solveName(checkstring), "FATAL ERROR"));
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send e) {
        if (!mode.is(Mode.Jenro)) return;
        if (e.getPacket() instanceof CommandExecutionC2SPacket)
            checktimer.reset();
    }

    @EventHandler
    private void onSync(EventSync event) {
        if (fullNullCheck()) return;
        if (!mode.is(Mode.Jenro)) return;

        if (isKeyPressed(desorient.getValue().getKey()) && disorientTimer.passedMs(3000) && mc.currentScreen == null) {
            use(InventoryUtility.findInHotBar(i -> i.getItem() == Items.ENDER_EYE),
                    InventoryUtility.findInInventory(i -> i.getItem() == Items.ENDER_EYE));
            disorientTimer.reset();
        }

        if (isKeyPressed(trap.getValue().getKey()) && trapTimer.passedMs(3000) && mc.currentScreen == null) {
            use(InventoryUtility.findInHotBar(i -> i.getItem() == Items.NETHERITE_SCRAP),
                    InventoryUtility.findInInventory(i -> i.getItem() == Items.NETHERITE_SCRAP));
            trapTimer.reset();
        }
    }

    private String getKey(ItemStack stack) {
        if (groupBy.is(GroupBy.Name)) {
            return stack.getName().getString();
        } else {
            return stack.getItem().getTranslationKey();
        }
    }

    public void onRenderChest(DrawContext context, Slot slot) {
        if (!mode.is(Mode.Jenro)) return;
        if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler chest)
            if (mc.currentScreen.getTitle().getString().contains("Аукцион") || mc.currentScreen.getTitle().getString().contains("Поиск"))
                for (AucItem item : result)
                    if (item.id == slot.id && slot.id <= 44 && !slot.getStack().isEmpty()) {
                        float ratio = (float) (Math.pow(item.lowestPrice, contrast.getValue()) / Math.pow(item.price, contrast.getValue()));

                        Render2DEngine.drawRect(context.getMatrices(), slot.x, slot.y,
                                16, 16, Render2DEngine.interpolateColorC(new Color(0xFF000000, true), new Color(0x00FF00), ratio));
                        return;
                    }
    }

    public int getPrice(ItemStack stack) {
        if (stack.getComponents().toString() == null) return 999999999;

        String string2 = StringUtils.substringBetween(stack.getComponents().toString(), "\"text\":\" $", "\"}]");

        if (string2 == null) return 999999999;

        string2 = string2.replaceAll(" ", "");

        int price = 999999999;

        try {
            price = Integer.parseInt(string2);
        } catch (NumberFormatException ignored) {
        }

        return price;
    }

    private void use(SearchInvResult result, SearchInvResult invResult) {
        if (result.found()) {
            InventoryUtility.saveAndSwitchTo(result.slot());
            sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
            InventoryUtility.returnSlot();
        } else if (invResult.found()) {
            clickSlot(invResult.slot(), mc.player.getInventory().getSelectedSlot(), SlotActionType.SWAP);
            sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
            clickSlot(invResult.slot(), mc.player.getInventory().getSelectedSlot(), SlotActionType.SWAP);
            sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
        }
        disorientTimer.reset();
    }

    private record AucItem(Item item, int price, int lowestPrice, int id) {
    }
}
