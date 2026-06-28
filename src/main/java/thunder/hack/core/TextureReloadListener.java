package thunder.hack.core;

import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.render.Render2DEngine;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class TextureReloadListener implements SimpleResourceReloadListener<Void> {
    @Override
    public CompletableFuture<Void> load(ResourceManager manager, Executor executor) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> apply(Void data, ResourceManager manager, Executor executor) {
        return CompletableFuture.runAsync(this::onReload, executor);
    }

    private void onReload() {
        Render2DEngine.shadowCache.clear();
        Render2DEngine.shadowCache1.clear();
        Core.HEADS.clear();
        // FontRenderers.reloadAll(); // removed in 1.21.11
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of("thunderhack", "texture_reload");
    }
}
