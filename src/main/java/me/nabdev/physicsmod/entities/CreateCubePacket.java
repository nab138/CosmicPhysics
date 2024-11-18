package me.nabdev.physicsmod.entities;

import com.badlogic.gdx.math.Vector3;
import com.github.puzzle.game.util.BlockUtil;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.blocks.MissingBlockStateResult;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.networking.NetworkSide;
import finalforeach.cosmicreach.world.Zone;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import me.nabdev.physicsmod.utils.PhysicsUtils;

public class CreateCubePacket extends GamePacket {
    BlockPosition blockPos;
    BlockState blockState;
    Zone zone;

    public void setCubeInfo(Zone zone, BlockPosition blockPos, BlockState blockState) {
        this.zone = zone;
        this.blockPos = blockPos;
        this.blockState = blockState;
    }

    @Override
    public void receive(ByteBuf in) {
        this.zone = GameSingletons.world.getZoneCreateIfNull(this.readString(in));
        blockPos = this.readBlockPosition(in, zone);
        blockState = BlockState.getInstance(this.readString(in), MissingBlockStateResult.MISSING_OBJECT);
    }

    @Override
    public void write() {
        this.writeString(this.zone.zoneId);
        this.writeBlockPosition(blockPos);
        this.writeString(this.blockState.getSaveKey());
    }

    @Override
    public void handle(NetworkIdentity identity, ChannelHandlerContext ctx) {
        if (identity.getSide() != NetworkSide.CLIENT) {
            if (this.blockPos.getBlockState() == this.blockState) {
                BlockUtil.setBlockAt(this.zone, Block.AIR.getDefaultBlockState(), this.blockPos);

                PhysicsUtils.createBlockAt(new Vector3(blockPos.getGlobalX(), blockPos.getGlobalY(), blockPos.getGlobalZ()).add(0.5f), blockState, zone);
            }
        }
    }
}
