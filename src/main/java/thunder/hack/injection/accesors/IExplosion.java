package thunder.hack.injection.accesors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;

public interface IExplosion {
    void setX(double x);
    void setY(double y);
    void setZ(double z);
    void setEntity(Entity entity);
    void setWorld(World world);
    World getExplosionWorld();
    DamageSource getDamageSource();
}
