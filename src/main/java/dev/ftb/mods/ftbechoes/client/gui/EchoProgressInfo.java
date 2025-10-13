package dev.ftb.mods.ftbechoes.client.gui;

import dev.ftb.mods.ftbechoes.echo.Echo;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import dev.ftb.mods.ftbechoes.echo.EchoStage;
import dev.ftb.mods.ftbechoes.echo.progress.PerEchoProgress;
import dev.ftb.mods.ftbechoes.echo.progress.TeamProgress;
import dev.ftb.mods.ftbechoes.net.RequestTeamProgressMessage;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.property.TeamProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class EchoProgressInfo extends BaseScreen {
    private final TeamSelectionPanel teamSelectionPanel;
    private final TeamProgressInfoPanel teamProgressInfoPanel;

    private final PanelScrollBar teamSelectScrollBar;
    private final PanelScrollBar teamProgressScrollBar;

    public EchoProgressInfo() {
        teamProgressInfoPanel = new TeamProgressInfoPanel(this);
        teamSelectionPanel = new TeamSelectionPanel(this);

        teamSelectScrollBar = new PanelScrollBar(this, ScrollBar.Plane.VERTICAL, teamSelectionPanel);
        teamProgressScrollBar = new PanelScrollBar(this, ScrollBar.Plane.VERTICAL, teamProgressInfoPanel);

        Theme.renderDebugBoxes = false;
    }

    @Override
    public boolean onInit() {
        List<Team> teams = FTBTeamsAPI.api().getClientManager().getTeams()
                .stream()
                .filter(Team::isPartyTeam)
                .toList();

        // Select the first team by default
        teams.stream().findFirst().ifPresent(this::selectTeam);
        return true;
    }

    private void selectTeam(Team teamId) {
        teamProgressInfoPanel.setLoading(true);
        PacketDistributor.sendToServer(new RequestTeamProgressMessage(teamId.getId()));
    }

    @Override
    public void addWidgets() {
        add(teamSelectionPanel);
        add(teamProgressInfoPanel);
        add(teamSelectScrollBar);
        add(teamProgressScrollBar);
    }

    @Override
    public void alignWidgets() {
        super.alignWidgets();

        this.setPos(25, 25);
        this.setSize(getScreen().getGuiScaledWidth() - 50, getScreen().getGuiScaledHeight() - 50);

        int teamPanelWidth = 100;
        teamSelectionPanel.setPosAndSize(0, 0, teamPanelWidth, height);
        teamProgressInfoPanel.setPosAndSize(0, 0, width - (teamPanelWidth + 15), height);

        this.align(new WidgetLayout.Horizontal(0, 10, 0));

        teamSelectScrollBar.setPosAndSize(teamSelectionPanel.getPosX() + teamSelectionPanel.getWidth(), teamSelectionPanel.getPosY(), 6, teamSelectionPanel.getHeight());
        teamProgressScrollBar.setPosAndSize(teamProgressInfoPanel.getPosX() + teamProgressInfoPanel.getWidth(), teamProgressInfoPanel.getPosY(), 6, teamProgressInfoPanel.getHeight());
    }

    public void setProgress(UUID teamId, TeamProgress progress, Map<UUID, Component> referencedPlayers) {
        FTBTeamsAPI.api().getClientManager().getTeamByID(teamId).ifPresent(team -> {
            this.teamProgressInfoPanel.setTeamProgress(team, progress, referencedPlayers);
        });
    }

    private static class TeamSelectionPanel extends Panel {
        private final TextField title;

        public TeamSelectionPanel(Panel panel) {
            super(panel);
            this.title = new TextField(this).setText("Teams");
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            super.drawBackground(graphics, theme, x, y, w, h);
            graphics.fill(x, y, x + w, y + h, 0x40000000);
        }

        @Override
        public void addWidgets() {
            add(title);

            List<Team> teams = FTBTeamsAPI.api().getClientManager().getTeams()
                    .stream()
                    .filter(Team::isPartyTeam)
                    .toList();

            for (var team : teams) {
                var color = team.getProperty(TeamProperties.COLOR);

                this.add(SimpleTextButton.create(this, team.getName(), color.withPadding(4), (mouseButton) -> {
                    ((EchoProgressInfo) getGui()).selectTeam(team);
                }));
            }
        }

        @Override
        public void alignWidgets() {
            this.title.setX(5);
            for (var widget : widgets) {
                if (widget instanceof SimpleTextButton) {
                    widget.setX(5);
                    widget.setWidth(100 - 10);
                }
            }

            this.align(new WidgetLayout.Vertical(5, 5, 0));
        }
    }

    private static class TeamProgressInfoPanel extends Panel {
        TeamProgress progress = null;
        Team team = null;
        Map<UUID, Component> playerNames = new HashMap<>();

        boolean loading = false;

        TextField text;

        public TeamProgressInfoPanel(Panel panel) {
            super(panel);
        }

        @Override
        public void addWidgets() {
            this.text = new TextField(this);

            if (progress != null) {
                MutableComponent processText = Component.empty()
                        .append("Progress for\n")
                        .append(team.getName().copy().append("'s").withStyle(ChatFormatting.BOLD))
                        .append("\n");

                for (Map.Entry<ResourceLocation, PerEchoProgress> echoProgress : progress.perEcho().entrySet()) {
                    var echoId = echoProgress.getKey();
                    var perEchoProgress = echoProgress.getValue();

                    EchoManager instance = EchoManager.getInstance();
                    Optional<Echo> echo = instance.getEcho(echoId);
                    if (echo.isEmpty()) {
                        continue;
                    }

                    var echoName = echo.get().title().getString();
                    processText.append("\n\n");
                    var stage = perEchoProgress.getCurrentStage();
                    processText.append(Component.literal(echoName + " (Stage " + stage + ")").withStyle(ChatFormatting.AQUA));
                    processText.append(Component.literal("\n\nStages\n").withStyle(ChatFormatting.YELLOW));
                    Map<UUID, Set<Integer>> playersClaimedRewards = perEchoProgress.claimedRewards();

                    for (int i = 0; i < perEchoProgress.getCurrentStage() + 2; i++) {
                        List<EchoStage> echoStages = echo.map(Echo::stages).orElse(Collections.emptyList());

                        var isCurrentStage = i == perEchoProgress.getCurrentStage();
                        var stageName = echoStages.size() > i ? echoStages.get(i).title().getString() : "Stage " + i;
                        processText.append(Component.literal(stageName + (isCurrentStage ? " (current)\n" : "\n")).withStyle(isCurrentStage ? ChatFormatting.GOLD : ChatFormatting.WHITE));

                        List<UUID> playersThatClaimed = new ArrayList<>();
                        for (var entry : playersClaimedRewards.entrySet()) {
                            if (entry.getValue().contains(i)) {
                                playersThatClaimed.add(entry.getKey());
                            }
                        }

                        if (!playersThatClaimed.isEmpty()) {
                            // This player has claimed the reward for this stage
                            processText.append(Component.literal("Reward claimed by:\n").withStyle(ChatFormatting.GREEN));
                            for (UUID uuid : playersThatClaimed) {
                                var playerName = this.playerNames.getOrDefault(uuid, Component.literal(uuid.toString()));
                                processText.append(Component.literal(playerName.getString()).withStyle(ChatFormatting.GREEN));

                                var isLast = playersThatClaimed.indexOf(uuid) == playersThatClaimed.size() - 1;
                                if (!isLast) {
                                    processText.append(Component.literal(", ").withStyle(ChatFormatting.GREEN));
                                }
                            }

                            processText.append("\n");
                        }

                        processText.append("\n");
                    }
                }

                this.text.setText(processText);
            } else if (loading) {
                this.text.setText("Loading...");
            }

            add(this.text);
        }

        @Override
        public void alignWidgets() {
            this.text.setPosAndSize(0, 5, this.width, this.height);

            if (this.text != null) {
                this.text.reflow();
            }

//            this.scrollBar.setPosAndSize(0, 5, this.width, this.height);
        }

        public void setTeamProgress(Team team, TeamProgress progress, Map<UUID, Component> playerNames) {
            this.progress = progress;
            this.team = team;
            this.loading = false;
            this.playerNames = playerNames;

            refreshWidgets();
        }

        public void setLoading(boolean loading) {
            this.loading = loading;
            refreshWidgets();
        }
    }
}
