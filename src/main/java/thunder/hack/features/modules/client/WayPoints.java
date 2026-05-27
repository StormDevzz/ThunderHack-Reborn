package thunder.hack.features.modules.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4d;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.world.WayPointManager;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.modules.Module;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextureStorage;

public final class WayPoints extends Module {
    public WayPoints() {
        super("WayPoints", Category.CLIENT);
    }

    @Override
    public void onEnable() {
        sendMessage(Managers.COMMAND.getPrefix() + "waypoint add x y z name");
    }

    public void onRender2D(DrawContext context) {
    // stubbed for 1.21.9
}
}
