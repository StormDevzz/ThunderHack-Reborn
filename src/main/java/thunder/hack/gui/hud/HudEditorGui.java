package thunder.hack.gui.hud;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClickGui;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.clickui.AbstractCategory;
import thunder.hack.gui.clickui.Category;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;
import java.util.List;

import static thunder.hack.features.modules.client.ClientSettings.isRu;
import static thunder.hack.features.modules.Module.mc;

public class HudEditorGui extends Screen {
    public static HudElement currentlyDragging;
    private final List<AbstractCategory> windows;
    private static HudEditorGui instance = new HudEditorGui();

    private boolean firstOpen;
    private double dWheel;

    public HudEditorGui() {
        super(Text.of("HudEditorGui"));
        windows = Lists.newArrayList();
        firstOpen = true;

        this.setInstance();
    }

    @Override
    protected void init() {
        if (firstOpen) {
            Category window = new Category(Module.Category.HUD, Managers.MODULE.getModulesByCategory(Module.Category.HUD), mc.getWindow().getScaledWidth() / 2f - 50, 20f, 100f, 18f);
            window.setOpen(true);
            windows.add(window);
            firstOpen = false;
        }
        windows.forEach(AbstractCategory::init);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void tick() {
        windows.forEach(AbstractCategory::tick);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ClickGUI.anyHovered = false;

        Render2DEngine.begin(context);
        try {
            Render2DEngine.drawRect(context.getMatrices(), 0, 0, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight(), new Color(0x40000000, true));

            if (ModuleManager.clickGui.scrollMode.getValue() == ClickGui.scrollModeEn.Old) {
                for (AbstractCategory window : windows) {
                    if (InputUtil.isKeyPressed(mc.getWindow(), 264))
                        window.setY(window.getY() + 2);
                    if (InputUtil.isKeyPressed(mc.getWindow(), 265))
                        window.setY(window.getY() - 2);
                    if (InputUtil.isKeyPressed(mc.getWindow(), 262))
                        window.setX(window.getX() + 2);
                    if (InputUtil.isKeyPressed(mc.getWindow(), 263))
                        window.setX(window.getX() - 2);
                    if (dWheel != 0)
                        window.setY((float) (window.getY() + dWheel));
                }
            } else for (AbstractCategory window : windows)
                if (dWheel != 0)
                    window.setModuleOffset((float) dWheel, mouseX, mouseY);

            dWheel = 0;

            for (AbstractCategory window : windows) {
                window.render(context, mouseX, mouseY, delta);
            }

            // Removed title and hint texts as requested
        } finally {
            Render2DEngine.end();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        dWheel = (int) (verticalAmount * 5D);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(Click click, boolean something) {
        windows.forEach(w -> {
            w.mouseClicked((int) click.x(), (int) click.y(), click.button());

            windows.forEach(w1 -> {
                if (w.dragging && w != w1)
                    w1.dragging = false;
            });
        });
        return super.mouseClicked(click, something);
    }

    @Override
    public boolean mouseReleased(Click click) {
        windows.forEach(w -> w.mouseReleased((int) click.x(), (int) click.y(), click.button()));
        return super.mouseReleased(click);
    }


    @Override
    public boolean keyPressed(KeyInput key) {
        windows.forEach(w -> w.keyTyped(key.key()));

        if (key.key() == GLFW.GLFW_KEY_ESCAPE) {
            super.keyPressed(key);
            return true;
        }

        return false;
    }

    @Override
    public void removed() {
        ThunderHack.EVENT_BUS.unsubscribe(this);
    }

    public void hudClicked(Module module) {
        for (AbstractCategory window : windows) {
            window.hudClicked(module);
        }
    }

    public static HudEditorGui getInstance() {
        if (instance == null) {
            instance = new HudEditorGui();
        }
        return instance;
    }

    public static HudEditorGui getHudGui() {
        return HudEditorGui.getInstance();
    }

    private void setInstance() {
        instance = this;
    }
}
