package com.lx862.takeaseat.commands;

import com.lx862.takeaseat.config.Config;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TakeASeatCommand {

    public static void register(String command, CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal(command)
            .requires(ctx -> ctx.hasPermissionLevel(2))
                .then(CommandManager.literal("reload")
                        .executes(context -> {
                            Config.load();
                            context.getSource().sendFeedback(() -> Text.literal("Config reloaded.").formatted(Formatting.GREEN), false);
                            return 1;
                        })
                )
        );
    }
}