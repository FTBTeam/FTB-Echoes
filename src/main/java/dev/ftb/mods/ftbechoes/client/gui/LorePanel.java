package dev.ftb.mods.ftbechoes.client.gui;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.client.ClientProgress;
import dev.ftb.mods.ftbechoes.client.PersistedClientData;
import dev.ftb.mods.ftbechoes.client.gui.widget.*;
import dev.ftb.mods.ftbechoes.echo.Echo;
import dev.ftb.mods.ftbechoes.echo.EchoStage;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
                Component title = Component.empty().withColor(Color4I.LIGHT_GREEN.rgb()).append(stage.title());
                Widget titleWidget = new CollapsibleSectionTitle(title, stageIdx);
                add(titleWidget);
                jumpPointWidgets.add(titleWidget);

                boolean collapsed = PersistedClientData.get().isStageCollapsed(echo, stageIdx);
                if (!collapsed) {
                    List<Widget> loreLines = new ArrayList<>();
                    stage.lore().forEach(entry ->
                            StageEntryRenderers.get(entry).ifPresent(r -> r.addWidgets(loreLines::add, entry, this))
                    );
                    addAll(loreLines);

                    vSpace(5);
                }
                if (stageIdx == limit && stageIdx < stages.size() && !allCompleted) {
                    if (FTBEchoes.stageProvider().has(player, stage.requiredGameStage())) {
                        if (!collapsed) {
                            add(new TextField(this).setText(stage.ready()));
                        }
                        add(new CompleteStageButton(this, echo, stageIdx));
                    } else {
                        add(new TextField(this).setText(stage.notReady()));
                    }
                }
                if (stageIdx < currentStage) {
                    Component txt = stage.completed().orElse(Component.translatable("ftbechoes.gui.stage_completed"));
                    add(new TextField(this).setText(Component.empty().withStyle(ChatFormatting.GREEN).append(txt)));
                    if (stage.completionReward().isPresent() && !ClientProgress.get().isRewardClaimed(echo.id(), player, stageIdx)) {
                        add(new ClaimRewardButton(this, echo, stageIdx, stage.completionReward().get()));
                    }
                }

                if (stageIdx < limit) {
                    add(new HorizontalLineWidget(this, 0.85f));
                }
            }

            if (allCompleted) {
                add(new HorizontalLineWidget(this, 0.85f));
                Component msg = echo.allComplete().orElse(Component.translatable("ftbechoes.gui.all_complete").withStyle(ChatFormatting.LIGHT_PURPLE));
                add(new TextField(this).setText(msg).setScale(1.25f).addFlags(Theme.CENTERED));
            }

            vSpace(10);
        });
    }

    @Override
    public void alignWidgets() {
        widgets.forEach(w -> {
            int xPos = w instanceof CollapsibleSectionTitle ? 5 : 15;
            int xWid = width - (w instanceof CollapsibleSectionTitle ? 10 : 20);
            w.setX(w instanceof CollapsibleSectionTitle ? 5 : 15);
            if (w instanceof ImageButton im) {
                int imgX = switch (im.getAlignment()) {
                    case LEFT -> xPos;
                    case CENTER -> xPos + (xWid - im.getWidth()) / 2;
                    case RIGHT -> xPos + xWid - im.getWidth() - 5;
                };
                im.setX(imgX);
            } else if (w instanceof TextField t) {
                t.setWidth(xWid);
                t.setMinWidth(xWid);
                t.setMaxWidth(xWid);
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

    public boolean isCollapsed(int stageIdx) {
        return getEcho().map(e -> PersistedClientData.get().isStageCollapsed(e, stageIdx)).orElse(false);
    }

    public void setCollapsed(int stageIdx, boolean collapsed) {
        getEcho().ifPresent(echo -> {
            if (PersistedClientData.get().setStageCollapsed(echo, stageIdx, collapsed)) {
                refreshWidgets();
            }
        });
    }

    public void setAllCollapsed(boolean collapse) {
        getEcho().ifPresent(echo -> {
            for (int i = 0; i < echo.stages().size(); i++) {
                PersistedClientData.get().setStageCollapsed(echo, i, collapse);
            }
            refreshWidgets();
        });
    }

    private class CollapsibleSectionTitle extends SimpleTextButton {
        private final int stageIdx;

        public CollapsibleSectionTitle(Component title, int stageIdx) {
            super(LorePanel.this, title, Icon.empty());

            setSize(getGui().getTheme().getStringWidth(title) + 25, getGui().getTheme().getFontHeight() + 7);
            this.stageIdx = stageIdx;
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        }

        @Override
        public void onClicked(MouseButton mouseButton) {
            playClickSound();
            Echo echo = getEcho().orElseThrow();
            PersistedClientData pcd = PersistedClientData.get();
            pcd.setStageCollapsed(echo, stageIdx, !pcd.isStageCollapsed(echo, stageIdx));
            LorePanel.this.refreshWidgets();
        }

        @Override
        public Component getTitle() {
            MutableComponent arrow = Component.literal(PersistedClientData.get().isStageCollapsed(getEcho().orElseThrow(), stageIdx) ? "▶ " : "▼ ");
            return arrow.append(super.getTitle());
        }
    }
}
