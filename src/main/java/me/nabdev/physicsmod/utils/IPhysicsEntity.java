package me.nabdev.physicsmod.utils;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.bulletphysics.dynamics.RigidBody;

public interface IPhysicsEntity {
    Vector3 getPosition();
    RigidBody getBody();
    BoundingBox getBoundingBox();
    void setMagneted(boolean magneted);
}
