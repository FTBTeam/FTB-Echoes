package dev.ftb.mods.ftbechoes.client.gui;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.client.ClientProgress;
import dev.ftb.mods.ftbechoes.client.gui.widget.CompleteStageButton;
import dev.ftb.mods.ftbechoes.client.gui.widget.TaskEntryPanel;
import dev.ftb.mods.ftbechoes.echo.EchoStage;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

class TaskPanel extends EchoScreen.PagePanel {
    public TaskPanel(Panel parent, EchoScreen echoScreen) {
        super(parent, echoScreen, EchoScreen.Page.TASKS);
    }

    @Override
    public void addWidgets() {
        getEcho().ifPresent(echo -> {
            List<EchoStage> stages = echo.stages();
            int current = ClientProgress.get().getCurrentStage(echo.id());
            int limit = Math.min(stages.size(), current + 1);
            Player player = Minecraft.getInstance().player;

            for (int stageIdx = 0; stageIdx < limit; stageIdx++) {
                EchoStage stage = stages.get(stageIdx);

                Icon icon = Color4I.empty();
                Component c = Component.empty();
                if (current > stageIdx) {
                    icon = Icons.CHECK; // stage completed
                    c = stage.completed().orElse(Component.translatable("ftbechoes.gui.stage_completed"));
                } else if (current == stageIdx) {
                    if (FTBEchoes.stageProvider().has(player, stage.requiredGameStage())) {
                        icon = Icons.LOCK_OPEN;  // ready to complete
                        c = stage.ready();
                    } else {
                        icon = Icons.LOCK; // not ready to complete
                        c = stage.notReady();
                    }
                }
                add(new TaskEntryPanel(this, icon, stage.title().orElse(Component.empty()), c));
                if (icon == Icons.LOCK_OPEN) {
                    add(new CompleteStageButton(this, echo.id()));
                }
            }
        });
    }

    @Override
    public void alignWidgets() {
        for (Widget w : widgets) {
            if (w instanceof Panel p) {
                p.alignWidgets();
            } else if (w instanceof CompleteStageButton b) {
                b.setX(18);
            }
        }

        align(new WidgetLayout.Vertical(5, 5, 2));
    }
}
