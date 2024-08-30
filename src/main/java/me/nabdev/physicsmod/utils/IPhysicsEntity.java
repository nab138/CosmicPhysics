package me.nabdev.physicsmod.utils;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.jme3.bullet.objects.PhysicsRigidBody;

public interface IPhysicsEntity {
    PhysicsRigidBody getBody();
    BoundingBox getBoundingBox();
    void solidify();
    void setMagneted(boolean magneted);
    void forceActivate();
    void setMass(float mass);
}
