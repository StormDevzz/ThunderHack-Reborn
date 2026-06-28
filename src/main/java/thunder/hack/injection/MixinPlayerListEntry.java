package thunder.hack.injection;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.utility.OptifineCapes;
import thunder.hack.utility.ThunderUtility;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;

@Mixin(PlayerListEntry.class)
public class MixinPlayerListEntry {

    @Shadow
    private GameProfile profile;

    @Unique
    private boolean loadedCapeTexture;

    @Unique
    private Identifier customCapeTexture;

    @Inject(method = "<init>(Lcom/mojang/authlib/GameProfile;Z)V", at = @At("TAIL"))
    private void initHook(GameProfile profile, boolean secureChatEnforced, CallbackInfo ci) {
        getTexture(profile);
    }

    @Inject(method = "getSkinTextures", at = @At("TAIL"), cancellable = true)
    private void getCapeTexture(CallbackInfoReturnable<SkinTextures> cir) {
        if (ModuleManager.optifineCapes.isEnabled() && Objects.equals(profile.name(), MinecraftClient.getInstance().getSession().getUsername())) {
            Identifier cape = Identifier.of("thunderhack", "textures/capes/starcape.png");
            SkinTextures prev = cir.getReturnValue();
            SkinTextures newTextures = new SkinTextures(prev.body(), new AssetInfo.TextureAssetInfo(cape), new AssetInfo.TextureAssetInfo(cape), prev.model(), prev.secure());
            cir.setReturnValue(newTextures);
            return;
        }

        if (customCapeTexture != null) {
            SkinTextures prev = cir.getReturnValue();
            SkinTextures newTextures = new SkinTextures(prev.body(), new AssetInfo.TextureAssetInfo(customCapeTexture), new AssetInfo.TextureAssetInfo(customCapeTexture), prev.model(), prev.secure());
            cir.setReturnValue(newTextures);
        }
    }

    @Unique
    private void getTexture(GameProfile profile) {
        if (loadedCapeTexture) return;
        loadedCapeTexture = true;

        Util.getMainWorkerExecutor().execute(() -> {
            try {
                URL capesList = new URL("https://raw.githubusercontent.com/Pan4ur/THRecodeUtil/main/capes/capeBase.txt");
                BufferedReader in = new BufferedReader(new InputStreamReader(capesList.openStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    String colune = inputLine.trim();
                    String name = colune.split(":")[0];
                    String cape = colune.split(":")[1];
                    if (Objects.equals(profile.name(), name)) {
                        customCapeTexture = Identifier.of("thunderhack", "textures/capes/" + cape + ".png");
                        return;
                    }
                }
            } catch (Exception ignored) {
            }

            for (String str : ThunderUtility.starGazer) {
                if (profile.name().toLowerCase().equals(str.toLowerCase()))
                    customCapeTexture = Identifier.of("thunderhack", "textures/capes/starcape.png");
            }

            if (ModuleManager.optifineCapes.isEnabled())
                OptifineCapes.loadPlayerCape(profile, id -> {
                    customCapeTexture = id;
                });
        });
    }
}