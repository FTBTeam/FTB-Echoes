package dev.ftb.mods.ftbechoes.client.gui.widget;

import dev.ftb.mods.ftbechoes.net.RequestStageCompletionMessage;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public class CompleteStageButton extends SimpleTextButton {
    private final ResourceLocation echoId;

    public CompleteStageButton(Panel panel, ResourceLocation echoId) {
        super(panel, Component.translatable("ftbechoes.gui.complete_stage").withStyle(ChatFormatting.GREEN), Icons.CHECK);
        this.echoId = echoId;
    }

    @Override
    public void onClicked(MouseButton mouseButton) {
        playClickSound();
        PacketDistributor.sendToServer(new RequestStageCompletionMessage(echoId));
    }
}
