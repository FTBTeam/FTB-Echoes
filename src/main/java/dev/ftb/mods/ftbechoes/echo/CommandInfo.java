package dev.ftb.mods.ftbechoes.echo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbechoes.util.EchoCodecs;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

import java.util.List;

public record CommandInfo(String cmd, int permission, boolean silent, List<Component> description) {
    public static final Codec<CommandInfo> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf("run").forGetter(CommandInfo::cmd),
            ExtraCodecs.intRange(0, 4).optionalFieldOf("permission", 0).forGetter(CommandInfo::permission),
            Codec.BOOL.optionalFieldOf("silent", true).forGetter(CommandInfo::silent),
            EchoCodecs.COMPONENT_OR_LIST.optionalFieldOf("description", List.of()).forGetter(CommandInfo::description)
    ).apply(builder, CommandInfo::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, CommandInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CommandInfo::cmd,
            ByteBufCodecs.VAR_INT, CommandInfo::permission,
            ByteBufCodecs.BOOL, CommandInfo::silent,
            ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list()), CommandInfo::description,
            CommandInfo::new
    );

    public void runForPlayer(ServerPlayer sp) {
        if (sp.getServer() != null) {
            CommandSourceStack stack = sp.createCommandSourceStack().withPermission(permission);
            sp.getServer().getCommands().performPrefixedCommand(silent ? stack.withSuppressedOutput() : stack, cmd);
        }
    }
}
