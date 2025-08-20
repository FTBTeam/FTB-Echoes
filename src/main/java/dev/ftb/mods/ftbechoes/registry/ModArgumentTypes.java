package dev.ftb.mods.ftbechoes.registry;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.command.EchoArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModArgumentTypes {
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES
            = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, FTBEchoes.MOD_ID);

    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<EchoArgumentType>> ECHO_ARGUMENT_TYPE
            = COMMAND_ARGUMENT_TYPES.register("echo",
            () -> ArgumentTypeInfos.registerByClass(EchoArgumentType.class, SingletonArgumentInfo.contextFree(EchoArgumentType::echo)));
}
