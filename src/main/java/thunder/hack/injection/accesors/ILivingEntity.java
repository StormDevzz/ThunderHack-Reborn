package thunder.hack.injection.accesors;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface ILivingEntity {
    @Accessor("lastAttackedTicks")
    int getLastAttackedTicks();

    @Accessor("jumpingCooldown")
    int getLastJumpCooldown();

    @Accessor("jumpingCooldown")
    void setLastJumpCooldown(int val);

    @org.spongepowered.asm.mixin.gen.Invoker("tryUseDeathProtector")
    boolean invokeTryUseDeathProtector(net.minecraft.entity.damage.DamageSource source);
}
