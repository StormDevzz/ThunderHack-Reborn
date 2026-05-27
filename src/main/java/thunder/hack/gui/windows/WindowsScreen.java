package thunder.hack.gui.windows;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.joml.Matrix3x2fStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static thunder.hack.core.manager.IManager.mc;

public class WindowsScreen extends Screen {
    private List<WindowBase> windows = new ArrayList<>();
    public static WindowBase lastClickedWindow;
    public static WindowBase draggingWindow;
    private static final Identifier clickGuiIcon = Identifier.of("thunderhack", "textures/gui/elements/clickgui.png");

    public WindowsScreen(WindowBase... windows) {
        super(Text.of("THWindows"));
        this.windows.clear();
        lastClickedWindow = null;
        this.windows = Arrays.stream(windows).toList();
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        //   super.render(context, mouseX, mouseY, delta);
        if (Module.fullNullCheck())
            renderBackground(context, mouseX, mouseY, delta);

        Matrix3x2fStack matrices = context.getMatrices();
        int i = mc.getWindow().getScaledWidth() / 2;

        float offset = (windows.size() * 20f) / -2f - 23;

        Render2DEngine.drawHudBase(matrices, i + offset - 1.5f, mc.getWindow().getScaledHeight() - 25, windows.size() * 20f + 23f, 19, HudEditor.hudRound.getValue());

        // TODO: 1.21.9 - RenderSystem.enableBlend/defaultBlendFunc/setShaderColor/disableBlend removed
        context.drawTexture(RenderPipelines.GUI_TEXTURED, clickGuiIcon, (int) (i + offset) + 1, mc.getWindow().getScaledHeight() - 23, 0f, 0f, 15, 15, 15, 15, 15, 15);

        Render2DEngine.drawLine(i + offset + 20, mc.getWindow().getScaledHeight() - 23, i + offset + 20, mc.getWindow().getScaledHeight() - 9, Color.GRAY.getRGB());

        offset += 23;
        for (WindowBase w : windows) {
            Color c = Render2DEngine.isHovered(mouseX, mouseY, i + offset, mc.getWindow().getScaledHeight() - 24, 17, 17) ? new Color(0x7C2F2F2F, true) :
                    !w.isVisible() ? new Color(0x7C1E1E1E, true) : new Color(0x7C3B3B3B, true);
            Render2DEngine.drawRect(matrices, i + offset, mc.getWindow().getScaledHeight() - 24, 17, 17, HudEditor.hudRound.getValue(), 0.7f, c, c, c, c);
            // TODO: 1.21.9 - setShaderColor removed, alpha handled via drawTexture
            context.drawTexture(RenderPipelines.GUI_TEXTURED, w.getIcon() != null ? w.getIcon() : TextureStorage.configIcon, (int) (i + offset) + 3, mc.getWindow().getScaledHeight() - 21, 0f, 0f, 11, 11, 11, 11, 11, 11);
            offset += 20f;
        }

        windows.stream().filter(WindowBase::isVisible).forEach(w -> {
            if (w != lastClickedWindow)
                w.render(context, mouseX, mouseY);
        });

        if (lastClickedWindow != null && lastClickedWindow.isVisible())
            lastClickedWindow.render(context, mouseX, mouseY);
    }

    // TODO: 1.21.9 - input methods reworked (Click/KeyInput/CharInput types), reimplement
}
