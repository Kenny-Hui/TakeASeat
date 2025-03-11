package com.lx862.takeaseat.commands;

import com.lx862.takeaseat.TakeASeat;
import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TakeASeatCommand {
    public static void register(String command, CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal(command)
            .requires(Permissions.require("takeaseat.reload", 2))
                .then(CommandManager.literal("reload")
                        .executes(context -> {
                            TakeASeat.getConfig().load();
                            context.getSource().sendFeedback(() -> Text.literal("Config reloaded.").formatted(Formatting.GREEN), false);
                            return 1;
                        })
                )
        );
    }
}