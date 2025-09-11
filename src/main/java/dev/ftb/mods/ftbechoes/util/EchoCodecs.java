package dev.ftb.mods.ftbechoes.util;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class EchoCodecs {
    public static final Codec<List<ItemStack>> ITEM_OR_ITEMS_CODEC = Codec.withAlternative(
            ItemStack.CODEC.listOf(), ItemStack.CODEC, List::of
    );
    public static final Codec<List<Component>> COMPONENT_OR_LIST = Codec.withAlternative(
        ComponentSerialization.CODEC.listOf(), ComponentSerialization.CODEC, List::of
    );
}
