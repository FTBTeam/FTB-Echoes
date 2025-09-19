package dev.ftb.mods.ftbechoes.client.gui;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.client.ClientProgress;
import dev.ftb.mods.ftbechoes.client.StageEntryRenderers;
import dev.ftb.mods.ftbechoes.client.gui.widget.*;
import dev.ftb.mods.ftbechoes.echo.EchoStage;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

class LorePanel extends EchoScreen.PagePanel implements AudioButtonHolder {
    private final List<Widget> jumpPointWidgets = new ArrayList<>();

    public LorePanel(Panel parent, EchoScreen echoScreen) {
        super(parent, echoScreen, EchoScreen.Page.LORE);
    }

    @Override
    public void addWidgets() {
        getEcho().ifPresent(echo -> {
            jumpPointWidgets.clear();
            List<EchoStage> stages = echo.stages();
            int currentStage = ClientProgress.get().getCurrentStage(echo.id());
            boolean allCompleted = currentStage >= stages.size();
            int limit = Math.min(stages.size() - 1, currentStage);
            Player player = Minecraft.getInstance().player;

            vSpace(5);

            for (int stageIdx = 0; stageIdx <= limit; stageIdx++) {
                EchoStage stage = stages.get(stageIdx);

                if (stage.showTitleInLore()) {
                    TextField titleWidget = new TextField(this).setText(Component.empty().withColor(Color4I.LIGHT_GREEN.rgb()).append(stage.title()));
                    add(titleWidget);
                    jumpPointWidgets.add(titleWidget);
                }

                List<Widget> loreLines = new ArrayList<>();
                stage.lore().forEach(entry ->
                        StageEntryRenderers.get(entry).ifPresent(r -> r.addWidgets(loreLines::add, entry, this))
                );
                if (!stage.showTitleInLore()) {
                    // if title is hidden, stage scroll point is the first line of lore
                    jumpPointWidgets.add(loreLines.getFirst());
                }
                addAll(loreLines);

                vSpace(5);

                if (stageIdx == limit && stageIdx < stages.size() && !allCompleted) {
                    if (FTBEchoes.stageProvider().has(player, stage.requiredGameStage())) {
                        add(new TextField(this).setText(stage.ready()));
                        add(new CompleteStageButton(this, echo.id()));
                    } else {
                        add(new TextField(this).setText(stage.notReady()));
                    }
                }
                if (stageIdx < currentStage) {
                    Component txt = stage.completed().orElse(Component.translatable("ftbechoes.gui.stage_completed"));
                    add(new TextField(this).setText(Component.empty().withStyle(ChatFormatting.GRAY).append(txt)));
                    if (stage.completionReward().isPresent() && !ClientProgress.get().isRewardClaimed(echo.id(), player, stageIdx)) {
                        add(new ClaimRewardButton(this, echo, stageIdx, stage.completionReward().get()));
                    }
                }
                if (stageIdx < limit) {
                    add(new HorizontalLineWidget(this, 0.75f));
                }
            }

            if (allCompleted) {
                add(new HorizontalLineWidget(this, 0.75f));
                Component msg = echo.allComplete().orElse(Component.translatable("ftbechoes.message.all_complete").withStyle(ChatFormatting.LIGHT_PURPLE));
                add(new TextField(this).setText(msg).setScale(1.25f).addFlags(Theme.CENTERED));
            }

            vSpace(10);
        });
    }

    @Override
    public void alignWidgets() {
        widgets.forEach(w -> {
            w.setX(5);
            if (w instanceof ImageButton im) {
                int xPos = switch (im.getAlignment()) {
                    case LEFT -> 5;
                    case CENTER -> (width - im.getWidth()) / 2;
                    case RIGHT -> width - im.getWidth() - 5;
                };
                im.setX(xPos);
            } else if (w instanceof TextField t) {
                t.setWidth(width - 10);
                t.setMinWidth(width - 10);
                t.setMaxWidth(width - 10);
                t.reflow();
            }
        });

        setHeight(align(new WidgetLayout.Vertical(0, 2, 0)) + 12);
    }

    @Override
    public void onAudioStart(AudioButton button) {
        widgets.forEach(w -> {
            if (w != button && w instanceof AudioButton ab) {
                ab.stopAudio();
            }
        });
    }

    @Override
    public void onSwitchAway() {
        widgets.forEach(w -> {
            if (w instanceof AudioButton ab) {
                ab.stopAudio();
            }
        });
    }

    public double getScrollPos(int stageIdx) {
        if (stageIdx >= 0 && stageIdx < jumpPointWidgets.size()) {
            return jumpPointWidgets.get(stageIdx).getPosY() - 2;
        }
        return 0;
    }
}
