package dev.ftb.mods.ftbechoes.client.gui.widget;

import dev.ftb.mods.ftbechoes.client.PersistedClientData;
import dev.ftb.mods.ftbechoes.echo.Echo;
import dev.ftb.mods.ftbechoes.net.RequestStageCompletionMessage;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class CompleteStageButton extends SimpleTextButton {
    private final int stageIdx;
    private final Echo echo;

    public CompleteStageButton(Panel panel, Echo echo, int stageIdx) {
        super(panel, Component.translatable("ftbechoes.gui.complete_stage").withStyle(ChatFormatting.GREEN), Icons.CHECK);
        this.echo = echo;
        this.stageIdx = stageIdx;
    }

    @Override
    public void onClicked(MouseButton mouseButton) {
        playClickSound();
        PacketDistributor.sendToServer(new RequestStageCompletionMessage(echo.id()));
        // don't refresh widgets just yet - that'll happen anyway when server sends us back a progress sync
        PersistedClientData.get().setStageCollapsed(echo, stageIdx, true);
    }
}
