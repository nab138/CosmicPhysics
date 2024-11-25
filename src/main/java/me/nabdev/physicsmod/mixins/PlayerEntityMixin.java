package me.nabdev.physicsmod.mixins;

import com.badlogic.gdx.math.Vector3;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.player.PlayerEntity;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.physicsmod.entities.Cube;
import me.nabdev.physicsmod.utils.PhysicsPlayer;
import me.nabdev.physicsmod.utils.PhysicsUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin extends Entity implements PhysicsPlayer {
    public PlayerEntityMixin(String entityTypeId) {
        super(entityTypeId);
    }
    @Unique
    public Cube physicsMod$cube = new Cube();

    @Override
    public void update(Zone z, double dt){
        physicsMod$cube.setPhysicsLocation(new Vector3f(0, 100, 0));
        setPosition(physicsMod$cube.getPosition());
        super.update(z, dt);
    }

    @Override
    public void cosmicPhysics$setPosition(Vector3 pos){
        physicsMod$cube.setPhysicsLocation(PhysicsUtils.v3ToV3f(pos));
    }
}
