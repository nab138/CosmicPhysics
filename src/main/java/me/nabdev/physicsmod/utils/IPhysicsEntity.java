package me.nabdev.physicsmod.utils;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.jme3.bullet.objects.PhysicsRigidBody;

import java.util.ArrayList;

public interface IPhysicsEntity {
    PhysicsRigidBody getBody();
    BoundingBox getBoundingBox();
    default void solidify(){}
    void setMagneted(boolean magneted);
    void forceActivate();
    void setMass(float mass);
    default void linkWith(IPhysicsEntity entity){}
    default ArrayList<IPhysicsEntity> getLinkedEntities(){return new ArrayList<>();}
}
