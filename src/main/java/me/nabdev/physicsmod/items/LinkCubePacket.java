package me.nabdev.physicsmod.items;

import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.world.Zone;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import me.nabdev.physicsmod.utils.IPhysicsEntity;

public class LinkCubePacket extends GamePacket {
    EntityUniqueId entityOne = new EntityUniqueId();
    EntityUniqueId entityTwo = new EntityUniqueId();
    String zoneId;

    public void setLinkData(EntityUniqueId entityOne, EntityUniqueId entityTwo, String zoneId) {
        this.entityOne = entityOne;
        this.entityTwo = entityTwo;
        this.zoneId = zoneId;
    }

    @Override
    public void write() {
        writeEntityUniqueId(entityOne);
        writeEntityUniqueId(entityTwo);
        writeString(zoneId);
    }

    @Override
    public void receive(ByteBuf in) {
        readEntityUniqueId(in, entityOne);
        readEntityUniqueId(in, entityTwo);
        zoneId = readString(in);
    }

    @Override
    public void handle(NetworkIdentity networkIdentity, ChannelHandlerContext channelHandlerContext) {
        if(networkIdentity.isServer()) return;
        Zone zone = GameSingletons.world.getZoneIfExists(zoneId);
        if(zone == null) return;
        Entity eOne = zone.getEntity(entityOne);
        if(eOne instanceof IPhysicsEntity physicsOne) {
            Entity eTwo = zone.getEntity(entityTwo);
            if(eTwo instanceof IPhysicsEntity) {
                physicsOne.linkWith(entityTwo);
            }
        }
    }
}
