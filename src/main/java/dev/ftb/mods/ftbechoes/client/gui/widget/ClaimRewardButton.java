package dev.ftb.mods.ftbechoes.client.gui.widget;

import dev.ftb.mods.ftbechoes.echo.Echo;
import dev.ftb.mods.ftbechoes.echo.StageCompletionReward;
import dev.ftb.mods.ftbechoes.net.ClaimRewardMessage;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClaimRewardButton extends SimpleTextButton {
    private final Echo echo;
    private final int stageIdx;
    private final StageCompletionReward reward;

    public ClaimRewardButton(Panel parent, Echo echo, int stageIdx, StageCompletionReward reward) {
        super(parent, Component.translatable("ftbechoes.gui.claim_reward"), Icons.MONEY_BAG);
        this.echo = echo;
        this.stageIdx = stageIdx;
        this.reward = reward;
    }

    @Override
    public void onClicked(MouseButton mouseButton) {
        PacketDistributor.sendToServer(new ClaimRewardMessage(echo.id(), stageIdx));
    }

    @Override
    public void addMouseOverText(TooltipList list) {
        super.addMouseOverText(list);

        list.add(Component.translatable("ftbechoes.tooltip.reward_header"));
        reward.addTooltip(list::add);
    }
}
