package dev.ftb.mods.ftbechoes.client;

import dev.ftb.mods.ftbechoes.echo.progress.TeamProgress;

public enum ClientProgress {
    INSTANCE;

    private TeamProgress progress = TeamProgress.createNew();

    public void receiveProgressFromServer(TeamProgress synced) {
        progress = synced;
    }

    public static TeamProgress get() {
        return INSTANCE.progress;
    }
}
