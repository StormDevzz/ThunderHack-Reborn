package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.world.ClientWorld;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class Weather extends Module {
    
    // 天气模式设置
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Clear);
    
    // 保存原始天气值 (用于恢复)
    private float originalRainGradient = 0f;
    private float originalThunderGradient = 0f;
    private boolean wasEnabled = false;
    
    public enum Mode {
        Clear,      // 晴天 (无降水)
        Rain,       // 雨天
        Thunder,    // 雷雨天 (雨 + 闪电)
        Snow        // 雪天 (视觉效果为雨 + 白色粒子)
    }
    
    public Weather() {
        super("Weather", Category.MISC);
    }
    
    @Override
    public void onEnable() {
        if (mc.world == null) return;
        
        // 保存原始天气 (彩虹渐变值)
        originalRainGradient = mc.world.getRainGradient(1.0f);
        originalThunderGradient = mc.world.getThunderGradient(1.0f);
        wasEnabled = true;
        
        // 立即应用选择的天气
        applyWeather();
    }
    
    @Override
    public void onDisable() {
        if (mc.world == null || !wasEnabled) return;
        
        // 恢复原始天气
        restoreOriginalWeather();
        wasEnabled = false;
    }
    
    @EventHandler
    @SuppressWarnings("unused")
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.world == null) return;
        
        // 每帧强制应用天气 (防止Minecraft重置)
        applyWeather();
    }
    
    // 应用选中的天气效果
    private void applyWeather() {
        ClientWorld world = mc.world;
        if (world == null) return;
        
        switch (mode.getValue()) {
            case Clear:
                // 晴天: 无雨, 无雷
                setClientRainGradient(0f);
                setClientThunderGradient(0f);
                break;
                
            case Rain:
                // 雨天: 下雨, 无雷
                setClientRainGradient(1f);
                setClientThunderGradient(0f);
                break;
                
            case Thunder:
                // 雷雨天: 下雨 + 闪电
                setClientRainGradient(1f);
                setClientThunderGradient(1f);
                break;
                
            case Snow:
                // 雪天: 视觉上下雨 + 白色效果 (Minecraft中雪通过生物群系判断)
                // 强制设置下雨并在寒冷生物群系会显示为雪
                setClientRainGradient(1f);
                setClientThunderGradient(0f);
                break;
        }
    }
    
    // 通过反射设置降雨强度 (ClientWorld的rainGradient字段)
    private void setClientRainGradient(float value) {
        try {
            // 使用反射访问ClientWorld的私有字段rainGradient
            java.lang.reflect.Field rainField = ClientWorld.class.getDeclaredField("rainGradient");
            rainField.setAccessible(true);
            rainField.setFloat(mc.world, value);
            
            // 同时设置上一个值，使渐变平滑
            java.lang.reflect.Field prevRainField = ClientWorld.class.getDeclaredField("prevRainGradient");
            prevRainField.setAccessible(true);
            prevRainField.setFloat(mc.world, value);
        } catch (Exception ignored) {
            // 反射失败时忽略 (某些映射版本字段名不同)
        }
    }
    
    // 通过反射设置雷电强度
    private void setClientThunderGradient(float value) {
        try {
            java.lang.reflect.Field thunderField = ClientWorld.class.getDeclaredField("thunderGradient");
            thunderField.setAccessible(true);
            thunderField.setFloat(mc.world, value);
            
            java.lang.reflect.Field prevThunderField = ClientWorld.class.getDeclaredField("prevThunderGradient");
            prevThunderField.setAccessible(true);
            prevThunderField.setFloat(mc.world, value);
        } catch (Exception ignored) {
            // 反射失败时忽略
        }
    }
    
    // 恢复原始天气
    private void restoreOriginalWeather() {
        try {
            // 恢复降雨
            java.lang.reflect.Field rainField = ClientWorld.class.getDeclaredField("rainGradient");
            rainField.setAccessible(true);
            rainField.setFloat(mc.world, originalRainGradient);
            
            java.lang.reflect.Field prevRainField = ClientWorld.class.getDeclaredField("prevRainGradient");
            prevRainField.setAccessible(true);
            prevRainField.setFloat(mc.world, originalRainGradient);
            
            // 恢复雷电
            java.lang.reflect.Field thunderField = ClientWorld.class.getDeclaredField("thunderGradient");
            thunderField.setAccessible(true);
            thunderField.setFloat(mc.world, originalThunderGradient);
            
            java.lang.reflect.Field prevThunderField = ClientWorld.class.getDeclaredField("prevThunderGradient");
            prevThunderField.setAccessible(true);
            prevThunderField.setFloat(mc.world, originalThunderGradient);
        } catch (Exception ignored) {
            // 反射失败时忽略
        }
    }
    
    @Override
    public String getDisplayInfo() {
        return mode.getValue().name();
    }
}