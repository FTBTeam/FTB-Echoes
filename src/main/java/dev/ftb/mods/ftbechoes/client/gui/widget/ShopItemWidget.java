package dev.ftb.mods.ftbechoes.client.gui.widget;

import dev.ftb.mods.ftbechoes.client.FTBEchoesClient;
import dev.ftb.mods.ftbechoes.echo.*;
import dev.ftb.mods.ftbechoes.shopping.ShopData;
import dev.ftb.mods.ftbechoes.shopping.ShoppingBasket;
import dev.ftb.mods.ftbechoes.shopping.ShoppingKey;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.misc.NordColors;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class ShopItemWidget extends Panel {
    public static final int WIDGET_SIZE = 64;
    public static final int INC_BTN_SIZE = 14;

    private final ShopData data;
    private final Button incrButton, decrButton;
    private final Button iconButton;
    private final ShoppingKey key;
    private final Component costStr;
    private final Component tooltip;
    private final boolean isCommand;

    public ShopItemWidget(Panel parent, Echo echo, ShopData data, int stageIdx, EchoStage stage) {
        super(parent);

        this.data = data;

        key = ShoppingKey.of(echo, data);
        costStr = FTBEchoesClient.formatCost(data.cost());

        Component txt = stage.title().orElse(Component.literal(String.valueOf(stageIdx)));
        tooltip = Component.translatable("ftbechoes.tooltip.unlocked_by", txt.copy().withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY);

        setSize(WIDGET_SIZE, WIDGET_SIZE);

        decrButton = SimpleTextButton.create(this, Component.literal("-"), Icon.empty(), mb -> adjustAmount(-1));
        incrButton = SimpleTextButton.create(this, Component.literal("+"), Icon.empty(), mb -> adjustAmount(1));
        iconButton = new IconButton();

        isCommand = !data.command().isEmpty();
    }

    @Override
    public void addWidgets() {
        add(decrButton);
        add(incrButton);
        add(iconButton);
    }

    @Override
    public void alignWidgets() {
        decrButton.setPosAndSize(4, height - (INC_BTN_SIZE + 4), INC_BTN_SIZE, INC_BTN_SIZE);
        incrButton.setPosAndSize(width - (INC_BTN_SIZE + 4), height - (INC_BTN_SIZE + 4), INC_BTN_SIZE, INC_BTN_SIZE);
        iconButton.setPosAndSize(20, 12, 24, 24);
    }

    private void adjustAmount(int adjustment) {
        ShoppingBasket.CLIENT_INSTANCE.adjust(key, adjustment * (ScreenWrapper.hasShiftDown() ? 10 : 1), isCommand ? 1 : Integer.MAX_VALUE);
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        GuiHelper.drawBorderedPanel(graphics, x, y, w, h, Color4I.rgb(0x1C2028), true);
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        super.draw(graphics, theme, x, y, w, h);

        Component amountStr = Component.literal(String.valueOf(ShoppingBasket.CLIENT_INSTANCE.get(key)));
        int sx = 16 + (32 - theme.getStringWidth(amountStr)) / 2;
        theme.drawString(graphics, amountStr, x + sx, y + height - INC_BTN_SIZE, theme.getContentColor(WidgetType.NORMAL), 0);

        theme.drawString(graphics, costStr, x + width - theme.getStringWidth(costStr) - 4, y + 4, theme.getContentColor(WidgetType.NORMAL), 0);
    }

    private class IconButton extends SimpleButton {
        public IconButton() {
            super(ShopItemWidget.this, Component.empty(), data.icon().orElse(ItemIcon.getItemIcon(data.stack())), (b, mb) -> {});
        }

        @Override
        public void playClickSound() {
        }

        @Override
        public void addMouseOverText(TooltipList list) {
            if (!data.stack().isEmpty()) {
                list.add(data.stack().getHoverName());
            }
            data.description().ifPresent(list::add);
            list.add(Component.empty());
            list.add(tooltip);
        }
    }
}
