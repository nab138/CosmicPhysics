package me.nabdev.physicsmod.commands;

import com.github.puzzle.game.commands.ClientCommandManager;
import com.github.puzzle.game.commands.ClientCommandSource;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import finalforeach.cosmicreach.chat.Chat;
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
                        Chat.MAIN_CLIENT_CHAT.addMessage(null, "The cube command is currently disabled.");
                        return 0;
                    }
                    Chat.MAIN_CLIENT_CHAT.addMessage(null, "Unknown action!");
                    return 1;
                })
        );
        ClientCommandManager.DISPATCHER.register(cmd);
    }

}