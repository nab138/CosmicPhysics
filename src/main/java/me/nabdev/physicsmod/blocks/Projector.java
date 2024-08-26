package me.nabdev.physicsmod.blocks;

import com.github.puzzle.core.Identifier;
import com.github.puzzle.core.resources.ResourceLocation;
import com.github.puzzle.game.block.IModBlock;
import com.github.puzzle.game.generators.BlockEventGenerator;
import com.github.puzzle.game.generators.BlockGenerator;
import com.github.puzzle.game.generators.BlockModelGenerator;
import me.nabdev.physicsmod.Constants;

import java.util.List;

public class Projector implements IModBlock {

    public static final Identifier BLOCK_ID = new Identifier(Constants.MOD_ID, "projector");
    public static final String BLOCK_NAME = "Projector";

    public static final ResourceLocation ALL_TEXTURE = new ResourceLocation("base", "textures/blocks/lunar_soil.png");

    @Override
    public BlockGenerator getBlockGenerator() {
        BlockGenerator generator = new BlockGenerator(BLOCK_ID, BLOCK_NAME);
        generator.createBlockState("default", "model", true, "events", true);
        return generator;
    }

    @Override
    public List<BlockModelGenerator> getBlockModelGenerators(Identifier blockId) {
        BlockModelGenerator generator = new BlockModelGenerator(blockId, "model");
        generator.createTexture("all", ALL_TEXTURE);
        generator.createCuboid(0, 0, 0, 16, 16, 16, "all");
        return List.of(generator);
    }

    @Override
    public List<BlockEventGenerator> getBlockEventGenerators(Identifier blockId) {
        BlockEventGenerator generator = new BlockEventGenerator(blockId, "events");
        return List.of(generator);
    }
}