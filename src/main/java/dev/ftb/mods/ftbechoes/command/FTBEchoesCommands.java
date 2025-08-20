package dev.ftb.mods.ftbechoes.command;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class FTBEchoesCommands {
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(FTBEchoes.MOD_ID)
                .then(ProgressCommand.register())
                .then(StageCommand.register())
        );
    }
}
