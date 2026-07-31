package dev.ftb.mods.ftbechoes.client.gui.widget;

import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.apache.commons.lang3.Validate;

public class HorizontalLineWidget extends Widget {
    private final float lineWidth;

    public HorizontalLineWidget(Panel panel, float lineWidth) {
        super(panel);

        Validate.isTrue(lineWidth > 0F && lineWidth < 1F);
        this.lineWidth = lineWidth;

        setHeight(10);
    }

    @Override
    public void draw(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
        int lw = (int) (parent.width * lineWidth);
        int xs = (parent.width - lw) / 2;
        graphics.horizontalLine(x + xs, x + xs + lw, y + h / 2, Color4I.WHITE.withAlpha(100).rgba());
        graphics.horizontalLine(x + xs, x + xs + lw, y + h / 2 + 1, Color4I.GRAY.withAlpha(60).rgba());
    }
}
