package dev.ftb.mods.ftbechoes.echo;

import dev.ftb.mods.ftblibrary.util.NameMap;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum EchoPage implements StringRepresentable{
    LORE("lore"),
    SHOP("shop");

    public static final StreamCodec<FriendlyByteBuf, EchoPage> STREAM_CODEC = NetworkHelper.enumStreamCodec(EchoPage.class);

    public static final NameMap<EchoPage> NAME_MAP = NameMap.of(LORE, EchoPage.values())
            .id(EchoPage::getSerializedName)
            .baseNameKey("ftbechoes.gui.page")
            .create();

    public static final StringRepresentableCodec<EchoPage> CODEC = StringRepresentable.fromEnum(EchoPage::values);

    private final String name;

    EchoPage(String name) {
        this.name = name;
    }

    public Component getLabel() {
        return Component.translatable("ftbechoes.gui.page." + name);
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
