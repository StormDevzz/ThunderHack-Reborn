package thunder.hack.gui.clickui;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import thunder.hack.ThunderHack;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.BaritoneSettings;
import thunder.hack.features.modules.client.ClickGui;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.clickui.impl.SearchBar;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Category extends AbstractCategory {
    private final Identifier ICON;

    private boolean scrollHover;
    private final List<AbstractButton> buttons;

    public float catHeight;

    public Category(Module.Category category, ArrayList<Module> features, float x, float y, float width, float height) {
        super(category.getName(), x, y, width, height);
        buttons = new ArrayList<>();
        ICON = Identifier.of("thunderhack", "textures/gui/headers/" + (Module.Category.isCustomCategory(category) ? "stock" : category.getName().toLowerCase()) + ".png");

        if (category.getName().equals("Client"))
            buttons.add(new SearchBar());

        features.forEach(feature -> {
            if (!(feature instanceof BaritoneSettings) || ThunderHack.baritone)
                buttons.add(new ModuleButton(feature));
        });
    }

    @Override
    public void init() {
        buttons.forEach(AbstractButton::init);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        setWidth(ModuleManager.clickGui.moduleWidth.getValue());

        scrollHover = Render2DEngine.isHovered(mouseX, mouseY, getX(), getY() + height, width, catHeight + 20);

        context.getMatrices().pushMatrix();

        boolean popStack = false;

        float height1;
        if (ModuleManager.clickGui.scrollMode.getValue() == ClickGui.scrollModeEn.Old || getButtonsHeight() < ModuleManager.clickGui.catHeight.getValue())
            height1 = (float) getButtonsHeight();
        else
            height1 = (float) ((ModuleManager.clickGui.catHeight.getValue()));

        catHeight = AnimationUtility.fast(catHeight, height1, 30f);

        Color m1 = HudEditor.getColor(270);
        Color m2 = HudEditor.getColor(0);
        Color m3 = HudEditor.getColor(180);
        Color m4 = HudEditor.getColor(90);

        if (isOpen()) {
            Render2DEngine.drawHudBase(context.getMatrices(), getX() + 3, getY() + height - 6, width - 6, catHeight, 1, false);

            if (!(ModuleManager.clickGui.scrollMode.getValue() == ClickGui.scrollModeEn.Old || getButtonsHeight() < ModuleManager.clickGui.catHeight.getValue())) {
                Render2DEngine.addWindow(context, new Render2DEngine.Rectangle(getX() + 3, getY() + height - 6, getX() + 3 + width - 6, (getY() + height - 6) + (float) ((ModuleManager.clickGui.catHeight.getValue()))));
                popStack = true;
            }

            Render2DEngine.drawBlurredShadow(context.getMatrices(), (int) getX() + 4, (int) (getY() + height - 6), (int) width - 8, 8, 7, new Color(0, 0, 0, 180));
            for (AbstractButton button : buttons) {
                if (button instanceof ModuleButton mb && SearchBar.listening && !mb.module.getName().toLowerCase().contains(SearchBar.moduleName.toLowerCase()))
                    continue;

                if (popStack && buttons.getFirst().getY() + moduleOffset < getY() + height) {
                    button.setY(getY() + height + moduleOffset);
                } else {
                    button.setY(getY() + height);
                    moduleOffset = 0f;
                }
                button.setX(getX() + 2);
                button.setWidth(width - 4);
                button.setHeight(ModuleManager.clickGui.moduleHeight.getValue());
                button.render(context, mouseX, mouseY, delta);
            }
        }

        if (popStack)
            Render2DEngine.popWindow();

        Render2DEngine.drawHudBase(context.getMatrices(), getX() + 2, getY() - 5, width - 4, height, 1, false);

        int iconSize = 14;
        int iconX = (int) getX() + 5;
        int iconY = (int) getY() - 4;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, Render2DEngine.getCleanedTexture(ICON), iconX, iconY, 0f, 0f, iconSize, iconSize, iconSize, iconSize, 256, 256, -1);

        int textX = iconX + iconSize + 3;
        int textY = (int) getY() + (int) (height / 2f) - 7;
        FontRenderers.categories.drawString(context.getMatrices(), getName(), textX, textY, new Color(-1).getRGB());
        context.getMatrices().popMatrix();
        updatePosition();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {

        if (button == 1 && hovered) {
            setOpen(!isOpen());
        }
        super.mouseClicked(mouseX, mouseY, button);

        if (isOpen() && scrollHover)
            buttons.forEach(b -> b.mouseClicked(mouseX, mouseY, button));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        if (isOpen())
            buttons.forEach(b -> b.mouseReleased(mouseX, mouseY, button));
    }

    @Override
    public boolean keyTyped(int keyCode) {
        if (isOpen()) {
            for (AbstractButton button : buttons) {
                button.keyTyped(keyCode);
            }
        }
        return false;
    }

    @Override
    public void charTyped(char key, int keyCode) {
        if (isOpen()) {
            for (AbstractButton button : buttons) {
                button.charTyped(key, keyCode);
            }
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        buttons.forEach(AbstractButton::onGuiClosed);
    }

    @Override
    public void tick() {
        buttons.forEach(AbstractButton::tick);
    }

    private void updatePosition() {
        float offsetY = 0;
        float openY = 0;
        for (AbstractButton button : buttons) {
            if (button instanceof ModuleButton mb && SearchBar.listening && !mb.module.getName().toLowerCase().contains(SearchBar.moduleName.toLowerCase())) {
                continue;
            }
            button.setTargetOffset(offsetY);
            if (button instanceof ModuleButton mbutton) {
                if (mbutton.isOpen()) {
                    for (AbstractElement element : mbutton.getElements()) {
                        if (element.isVisible())
                            offsetY += element.getHeight();
                    }
                    offsetY += mbutton.getDescriptionOffset();
                    offsetY += 2f;
                }
            }
            offsetY += button.getHeight() + openY;
        }
    }

    @Override
    public void hudClicked(Module module) {
        for (AbstractButton button : buttons) {
            if (button instanceof ModuleButton mbutton && mbutton.module == module)
                mbutton.setOpen(true);
        }
    }

    public double getButtonsHeight() {
        double height = 8;
        for (AbstractButton button : buttons) {
            if (button instanceof ModuleButton mb && SearchBar.listening && !mb.module.getName().toLowerCase().contains(SearchBar.moduleName.toLowerCase()))
                continue;

            if (button instanceof ModuleButton mbutton) {
                if (mbutton.isOpen())
                    height += 2f;
                height += mbutton.getElementsHeight();
            }

            height += button.getHeight();
        }
        return height;
    }
}