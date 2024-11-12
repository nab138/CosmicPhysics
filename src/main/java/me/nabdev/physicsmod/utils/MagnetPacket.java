package me.nabdev.physicsmod.utils;

import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import me.nabdev.physicsmod.items.GravityGun;

public class MagnetPacket extends GamePacket {
    public String playerID;
    boolean isMagnet;

    public void setPlayer(Player player, boolean isMagnet){
        playerID = player.getAccount().getUniqueId();
        this.isMagnet = isMagnet;
    }

    @Override
    public void receive(ByteBuf in) {
        playerID = readString(in);
        isMagnet = readBoolean(in);
    }

    @Override
    public void write() {
        writeString(playerID);
        writeBoolean(isMagnet);
    }

    @Override
    public void handle(NetworkIdentity networkIdentity, ChannelHandlerContext channelHandlerContext) {
        if(networkIdentity.isServer()) return;
        if(isMagnet){
            GravityGun.isPlayerMag.put(playerID, true);
        } else {
            GravityGun.isPlayerMag.remove(playerID);
        }
    }
}
