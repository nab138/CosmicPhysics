package me.nabdev.physicsmod.commands;

import com.github.puzzle.game.commands.CommandManager;
import com.github.puzzle.game.commands.PuzzleCommandSource;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityCreator;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.physicsmod.entities.Cube;
import me.nabdev.physicsmod.utils.PhysicsWorld;

import static finalforeach.cosmicreach.gamestates.InGame.world;

public class Commands {

    public static void register() {
        LiteralArgumentBuilder<PuzzleCommandSource> cmd = CommandManager.literal("physics");
        cmd.then(CommandManager.argument("action", StringArgumentType.word())
                .executes(context -> {
                    String action = StringArgumentType.getString(context, "action");
                    if (action.equals("reset")) {
                        PhysicsWorld.reset();
                        return 0;
                    } else if (action.equals("cube")) {
                        Entity e = EntityCreator.get(Cube.id.toString());
                        e.setPosition(InGame.getLocalPlayer().getPosition());

                        Zone zone = InGame.getLocalPlayer().getZone(world);
                        zone.addEntity(e);
                        Chat.MAIN_CHAT.sendMessage(world, null, null, "Spawned Cube");
                        return 0;
                    }
                    Chat.MAIN_CHAT.sendMessage(world, null, null, "Unknown action!");
                    return 1;
                })
        );
        CommandManager.dispatcher.register(cmd);
    }

}