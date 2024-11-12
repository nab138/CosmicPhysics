package me.nabdev.physicsmod.items;

import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import me.nabdev.physicsmod.utils.PhysicsWorld;

public class DropMagnetPacket extends GamePacket {
    public String playerID;

    public void setPlayer(String playerID){
        this.playerID = playerID;
    }

    @Override
    public void receive(ByteBuf in) {
        playerID = readString(in);
    }

    @Override
    public void write() {
        writeString(playerID);
    }

    @Override
    public void handle(NetworkIdentity networkIdentity, ChannelHandlerContext channelHandlerContext) {
        if(networkIdentity.isClient()) return;
        PhysicsWorld.dropMagnet(ServerSingletons.getAccountByUniqueId(playerID).getPlayer());
    }
}
