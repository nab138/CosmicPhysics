package me.nabdev.physicsmod.utils;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.jme3.bullet.objects.PhysicsRigidBody;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.world.Zone;

public interface IPhysicsEntity {
    PhysicsRigidBody getBody();

    Quaternion getRotation();

    void setRenderRotation(Quaternion rotation);

    default void solidify() {
    }

    void setMagnetised(Player magnetPlayer);

    void forceActivate();

    default void linkWith(EntityUniqueId entity) {
    }

    default Array<IPhysicsEntity> getLinkedEntities() {
        return new Array<>();
    }

    void kill();

    EntityUniqueId getID();

    Zone getZone();

    Vector3 getPosition();

    BoundingBox getBoundingBox();

    Vector3 getScale();
    void scale(Vector3 scale);
}
