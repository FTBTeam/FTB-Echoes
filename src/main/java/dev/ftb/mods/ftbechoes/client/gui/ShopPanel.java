package dev.ftb.mods.ftbechoes.client.gui;

import dev.ftb.mods.ftbechoes.client.ClientProgress;
import dev.ftb.mods.ftbechoes.client.gui.widget.ShopItemWidget;
import dev.ftb.mods.ftbechoes.echo.EchoStage;
import dev.ftb.mods.ftbechoes.echo.progress.TeamProgress;
import dev.ftb.mods.ftbechoes.shopping.ShopData;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;

import java.util.List;

public class ShopPanel extends EchoScreen.PagePanel {

    public static final int GUTTER = 5;

    public ShopPanel(Panel parent, EchoScreen echoScreen) {
        super(parent, echoScreen, EchoScreen.Page.SHOP);
    }

    @Override
    public void addWidgets() {
        getEcho().ifPresent(echo -> {
            List<EchoStage> stages = echo.stages();
            TeamProgress teamProgress = ClientProgress.get();
            int currentStage = teamProgress.getCurrentStage(echo.id());

            for (int stageIdx = 0; stageIdx < stages.size(); stageIdx++) {
                EchoStage stage = stages.get(stageIdx);
                for (ShopData data : stage.shopUnlocked()) {
                    if (currentStage <= data.maxStage()) {
                        add(new ShopItemWidget(this, echo, data, stage, stageIdx < currentStage, teamProgress));
                    }
                }
            }
        });
    }

    @Override
    public void alignWidgets() {
        if (width == 0) {
            return;
        }

        align(WidgetLayout.NONE);

        int paddedSize = ShopItemWidget.WIDGET_SIZE + GUTTER;
        int perRow = (width - GUTTER) / paddedSize;
        int curRow = 0;
        int curCol = 0;

        for (Widget w : widgets) {
            if (w instanceof ShopItemWidget) {
                w.setPosAndSize(
                        GUTTER + curCol * paddedSize, GUTTER + curRow * paddedSize,
                        ShopItemWidget.WIDGET_SIZE, ShopItemWidget.WIDGET_SIZE
                );

                if (++curCol >= perRow) {
                    curRow++;
                    curCol = 0;
                }
            }
        }

        setHeight(GUTTER + paddedSize * (curRow + 1));
    }
}
