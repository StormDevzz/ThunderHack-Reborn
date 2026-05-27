package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public final class Weather extends Module {
    public final Setting<WeatherMode> weatherMode = new Setting<>("Weather", WeatherMode.Clear);
    public final Setting<Integer> time = new Setting<>("Time", 12, 0, 23);
    public final Setting<Integer> fogStart = new Setting<>("FogStart", 0, 0, 256, v -> weatherMode.is(WeatherMode.Fog));
    public final Setting<Integer> fogEnd = new Setting<>("FogEnd", 32, 10, 256, v -> weatherMode.is(WeatherMode.Fog));

    public Weather() {
        super("Weather", Category.MISC);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket)
            event.cancel();
    }

    @Override
    public void onUpdate() {
        if (mc.world == null) return;

        mc.world.setTime(mc.world.getTime(), time.getValue() * 1000L, false);

        switch (weatherMode.getValue()) {
            case Clear -> {
                mc.world.setRainGradient(0f);
                mc.world.setThunderGradient(0f);
            }
            case Rain -> {
                mc.world.setRainGradient(1f);
                mc.world.setThunderGradient(0f);
            }
            case Thunder -> {
                mc.world.setRainGradient(1f);
                mc.world.setThunderGradient(1f);
            }
            case Fog -> {
                mc.world.setRainGradient(0.3f);
                mc.world.setThunderGradient(0f);
            }
        }
    }

    public enum WeatherMode {
        Clear, Rain, Thunder, Fog
    }
}
