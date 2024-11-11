package me.nabdev.physicsmod.entities;

import com.badlogic.gdx.math.Quaternion;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.world.Zone;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import me.nabdev.physicsmod.utils.IPhysicsEntity;

public class EntityRenderRotationPacket extends GamePacket {
    EntityUniqueId entityId = new EntityUniqueId();
    public Quaternion rotation = new Quaternion();

    public EntityRenderRotationPacket(){}

    public void setEntity(IPhysicsEntity entity) {
        this.entityId.set(entity.getID());
        rotation.set(entity.getRotation());
    }

    @Override
    public void receive(ByteBuf in) {
        this.readEntityUniqueId(in, this.entityId);
        readQuaternion(in, rotation);
    }

    @Override
    public void write() {
        this.writeEntityUniqueId(this.entityId);
        writeQuaternion(rotation);
    }

    public void handle(NetworkIdentity identity, ChannelHandlerContext ctx) {
        if (!identity.isServer()) {
            Zone zone = identity.getZone();
            if (zone != null) {
                Entity e = zone.getEntity(this.entityId);
                if (e != null && e != GameSingletons.client().getLocalPlayer().getEntity() && e instanceof IPhysicsEntity physicsEntity) {
                    physicsEntity.setRenderRotation(rotation);
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
