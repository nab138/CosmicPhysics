package me.nabdev.physicsmod.entities;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.world.Zone;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import me.nabdev.physicsmod.utils.IPhysicsEntity;

public class EntityRenderRotationScalePacket extends GamePacket {
    EntityUniqueId entityId = new EntityUniqueId();
    public Quaternion rotation = new Quaternion();
    public Vector3 scale = new Vector3();

    public void setEntity(IPhysicsEntity entity) {
        this.entityId.set(entity.getID());
        rotation.set(entity.getRotation());
        scale.set(entity.getScale());
    }

    @Override
    public void receive(ByteBuf in) {
        this.readEntityUniqueId(in, this.entityId);
        readQuaternion(in, rotation);
        readVector3(in, scale);
    }

    @Override
    public void write() {
        this.writeEntityUniqueId(this.entityId);
        writeQuaternion(rotation);
        writeVector3(scale);
    }

    public void handle(NetworkIdentity identity, ChannelHandlerContext ctx) {
        if (!identity.isServer()) {
            Zone zone = identity.getZone();
            if (zone != null) {
                Entity e = zone.getEntity(this.entityId);
                if (e != null && e != GameSingletons.client().getLocalPlayer().getEntity() && e instanceof IPhysicsEntity physicsEntity) {
                    physicsEntity.setRenderRotation(rotation);
                    physicsEntity.scale(scale);
                }

            }
        }
    }

    private void readQuaternion(ByteBuf in, Quaternion q) {
        q.x = in.readFloat();
        q.y = in.readFloat();
        q.z = in.readFloat();
        q.w = in.readFloat();
    }

    private void writeQuaternion(Quaternion q) {
        this.writeFloat(q.x);
        this.writeFloat(q.y);
        this.writeFloat(q.z);
        this.writeFloat(q.w);
    }

}
