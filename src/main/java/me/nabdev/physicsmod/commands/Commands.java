package me.nabdev.physicsmod.commands;

import com.badlogic.gdx.math.Vector3;
import com.github.puzzle.game.commands.CommandManager;
import com.github.puzzle.game.commands.ServerCommandSource;
import com.github.puzzle.game.util.BlockUtil;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.blocks.MissingBlockStateResult;
import me.nabdev.physicsmod.entities.Cube;
import me.nabdev.physicsmod.utils.PhysicsUtils;
import me.nabdev.physicsmod.utils.PhysicsWorld;

public class Commands {
    public static BlockState cheese = null;
    public static BlockState light = null;

    public static void register() {
        LiteralArgumentBuilder<ServerCommandSource> cmd = CommandManager.literal("physics");
        cmd.then(CommandManager.argument(ServerCommandSource.class, "action", StringArgumentType.word())
                .executes(context -> {

                    String action = StringArgumentType.getString(context, "action");
                    switch (action) {
                        case "reset" -> {
                            PhysicsWorld.reset();
                            return 0;
                        }
                        case "cube" -> {
                            if(cheese == null) {
                                cheese = Block.getInstance("cheese").getDefaultBlockState();
                            }
                            PhysicsUtils.createBlockAt(context.getSource().getAccount().getPlayer().getPosition().add(0, 0.5f, 0), cheese, context.getSource().getAccount().getPlayer().getZone());
                            return 0;
                        }
                        case "ball" -> {
                            if(light == null) {
                                light = BlockState.getInstance("base:light[power=on,lightRed=15,lightGreen=0,lightBlue=0]", MissingBlockStateResult.EXCEPTION);

                            }
                            Cube ball = PhysicsUtils.createBlockAt(context.getSource().getAccount().getPlayer().getPosition(), light, context.getSource().getAccount().getPlayer().getZone());
                            assert ball != null;
                            ball.scale(new Vector3(5, 5, 5));
                            return 0;
                        }
                    }
                    context.getSource().getChat().addMessage(null, "Unknown action!");
                    return 1;
                })
        );
        CommandManager.DISPATCHER.register(cmd);

        LiteralArgumentBuilder<ServerCommandSource> fill = CommandManager.literal("fill");
        fill.then(CommandManager.argument(ServerCommandSource.class, "x1", IntegerArgumentType.integer())
                .then(CommandManager.argument(ServerCommandSource.class, "y1", IntegerArgumentType.integer())
                .then(CommandManager.argument(ServerCommandSource.class, "z1", IntegerArgumentType.integer())
                .then(CommandManager.argument(ServerCommandSource.class, "x2", IntegerArgumentType.integer())
                .then(CommandManager.argument(ServerCommandSource.class, "y2", IntegerArgumentType.integer())
                .then(CommandManager.argument(ServerCommandSource.class, "z2", IntegerArgumentType.integer())
                .then(CommandManager.argument(ServerCommandSource.class, "block", StringArgumentType.greedyString())
                .executes(context -> {
                    int x1 = IntegerArgumentType.getInteger(context, "x1");
                    int y1 = IntegerArgumentType.getInteger(context, "y1");
                    int z1 = IntegerArgumentType.getInteger(context, "z1");
                    int x2 = IntegerArgumentType.getInteger(context, "x2");
                    int y2 = IntegerArgumentType.getInteger(context, "y2");
                    int z2 = IntegerArgumentType.getInteger(context, "z2");
                    for(int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
                        for(int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                            for(int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
                                BlockUtil.setBlockAt(GameSingletons.world.getDefaultZone(), BlockState.getInstance(StringArgumentType.getString(context, "block"), MissingBlockStateResult.EXCEPTION), x, y, z);
                            }
                        }
                    }
                    return 0;
                }))))))));
        CommandManager.DISPATCHER.register(fill);
    }

}