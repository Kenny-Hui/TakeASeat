package com.lx.takeaseat.commands;

import com.lx.takeaseat.config.Config;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class takeaseat {

    public static void register(String command, CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal(command)
            .requires(ctx -> ctx.hasPermissionLevel(2))
                .then(CommandManager.literal("reload")
                        .executes(context -> {
                            Config.load();
                            context.getSource().sendFeedback(Text.literal("Config reloaded.").formatted(Formatting.GREEN), false);
                            return 1;
                        })
                )
        );
    }
}