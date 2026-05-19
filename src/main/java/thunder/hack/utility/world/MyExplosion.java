package thunder.hack.utility.world;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import thunder.hack.injection.accesors.IExplosion;

public class MyExplosion implements Explosion, IExplosion {
    private World world;
    private Entity entity;
    private double x;
    private double y;
    private double z;
    private float power;

    public MyExplosion(World world, Entity entity, double x, double y, double z, float power) {
        this.world = world;
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.power = power;
    }

    @Override
    public void setX(double x) {
        this.x = x;
    }

    @Override
    public void setY(double y) {
        this.y = y;
    }

    @Override
    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void setWorld(World world) {
        this.world = world;
    }

    @Override
    public World getExplosionWorld() {
        return this.world;
    }

    @Override
    public DamageSource getDamageSource() {
        return Explosion.createDamageSource(this.world, this.entity);
    }

    @Override
    public ServerWorld getWorld() {
        return world instanceof ServerWorld ? (ServerWorld) world : null;
    }

    @Override
    public Explosion.DestructionType getDestructionType() {
        return Explosion.DestructionType.DESTROY;
    }

    @Override
    public LivingEntity getCausingEntity() {
        return entity instanceof LivingEntity ? (LivingEntity) entity : null;
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    @Override
    public float getPower() {
        return power;
    }

    @Override
    public Vec3d getPosition() {
        return new Vec3d(x, y, z);
    }

    @Override
    public boolean canTriggerBlocks() {
        return false;
    }

    @Override
    public boolean preservesDecorativeEntities() {
        return false;
    }
}
