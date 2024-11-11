package me.nabdev.physicsmod.commands;

import com.github.puzzle.game.commands.ClientCommandManager;
import com.github.puzzle.game.commands.ClientCommandSource;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityCreator;
import me.nabdev.physicsmod.entities.Cube;
import me.nabdev.physicsmod.utils.PhysicsWorld;

public class Commands {

    public static void register() {
        LiteralArgumentBuilder<ClientCommandSource> cmd = ClientCommandManager.literal("assets/physics");
        cmd.then(ClientCommandManager.argument("action", StringArgumentType.word())
                .executes(context -> {
                    String action = StringArgumentType.getString(context, "action");
                    if (action.equals("reset")) {
                        PhysicsWorld.reset();
                        return 0;
                    } else if (action.equals("cube")) {
//                        Entity e = EntityCreator.get(Cube.id.toString());
//                        e.setPosition(InGame.getLocalPlayer().getPosition());
//
//                        Zone zone = InGame.getLocalPlayer().getZone();
//                        zone.addEntity(e);
                        Chat.MAIN_CLIENT_CHAT.addMessage(null, "Unsupported Spawned Cube");
                        return 0;
                    }
                    Chat.MAIN_CLIENT_CHAT.addMessage(null, "Unknown action!");
                    return 1;
                })
        );
        ClientCommandManager.DISPATCHER.register(cmd);
    }

}