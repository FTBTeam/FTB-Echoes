package dev.ftb.mods.ftbechoes.client.gui;

import dev.ftb.mods.ftbechoes.GameStageHelper;
import dev.ftb.mods.ftbechoes.client.ClientProgress;
import dev.ftb.mods.ftbechoes.client.StageEntryRenderers;
import dev.ftb.mods.ftbechoes.client.gui.widget.AudioButton;
import dev.ftb.mods.ftbechoes.client.gui.widget.CompleteStageButton;
import dev.ftb.mods.ftbechoes.client.gui.widget.HorizontalLineWidget;
import dev.ftb.mods.ftbechoes.client.gui.widget.ImageButton;
import dev.ftb.mods.ftbechoes.echo.BaseStageEntry;
import dev.ftb.mods.ftbechoes.echo.EchoStage;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.TextField;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;

class LorePanel extends EchoScreen.PagePanel implements AudioButtonHolder {
    public LorePanel(Panel parent, EchoScreen echoScreen) {
        super(parent, echoScreen, EchoScreen.Page.LORE);
    }

    @Override
    public void addWidgets() {
        getEcho().ifPresent(echo -> {
            List<EchoStage> stages = echo.stages();
            int current = ClientProgress.get().getCurrentStage(echo.id());
            boolean allCompleted = current >= stages.size();
            int limit = Math.min(stages.size(), current + 1);

            vSpace(5);

            for (int stageIdx = 0; stageIdx < limit; stageIdx++) {
                EchoStage stage = stages.get(stageIdx);

                stage.title().ifPresent(title ->
                        add(new TextField(this).setText(Component.empty().withColor(Color4I.LIGHT_GREEN.rgb()).append(title)))
                );

                addTextLines(stage.lore());

                vSpace(5);

                if (stageIdx == limit - 1 && stageIdx < stages.size() && !allCompleted) {
                    if (GameStageHelper.hasStage(Minecraft.getInstance().player, stage.requiredGameStage())) {
                        add(new TextField(this).setText(stage.ready()));
                        add(new CompleteStageButton(this, Component.translatable("ftbechoes.message.complete_stage"), echo.id()));
                    } else {
                        add(new TextField(this).setText(stage.notReady()));
                    }
                }
                if (stageIdx < limit - 1) {
                    add(new HorizontalLineWidget(this, 0.75f));
                }
            }

            if (allCompleted) {
                add(new HorizontalLineWidget(this, 0.75f));
                Component msg = echo.allComplete().orElse(Component.translatable("ftbechoes.message.all_complete").withStyle(ChatFormatting.LIGHT_PURPLE));
                add(new TextField(this).setText(msg));
            }

            vSpace(10);
        });
    }

    private void addTextLines(List<BaseStageEntry> entries) {
        entries.forEach(entry ->
                StageEntryRenderers.get(entry).ifPresent(r -> r.addWidgets(this::add, entry, this))
        );
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
}
