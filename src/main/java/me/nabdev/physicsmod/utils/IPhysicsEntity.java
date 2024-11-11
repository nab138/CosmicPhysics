package me.nabdev.physicsmod.utils;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.jme3.bullet.objects.PhysicsRigidBody;
import finalforeach.cosmicreach.entities.EntityUniqueId;

public interface IPhysicsEntity {
    PhysicsRigidBody getBody();

    BoundingBox getBoundingBox();

    Quaternion getRotation();

    void setRenderRotation(Quaternion rotation);

    default void solidify() {
    }

    void setMagnetised(boolean magnetised);

    void forceActivate();

    default void linkWith(IPhysicsEntity entity) {
    }

    default Array<IPhysicsEntity> getLinkedEntities() {
        return new Array<>();
    }

    void kill();

    EntityUniqueId getID();
}
