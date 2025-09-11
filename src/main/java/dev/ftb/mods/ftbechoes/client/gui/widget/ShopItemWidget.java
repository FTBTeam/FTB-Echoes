package dev.ftb.mods.ftbechoes.client.gui.widget;

import dev.ftb.mods.ftbechoes.echo.CommandInfo;
import dev.ftb.mods.ftbechoes.echo.Echo;
import dev.ftb.mods.ftbechoes.echo.EchoStage;
import dev.ftb.mods.ftbechoes.shopping.ShopData;
import dev.ftb.mods.ftbechoes.shopping.ShoppingBasket;
import dev.ftb.mods.ftbechoes.shopping.ShoppingKey;
import dev.ftb.mods.ftbechoes.util.MiscUtil;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.IconAnimation;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ShopItemWidget extends Panel {
    public static final int WIDGET_SIZE = 64;
    public static final int INC_BTN_SIZE = 14;

    private final ShopData data;
    private final boolean unlocked;
    private final Button incrButton, decrButton;
    private final Button iconButton;
    private final ShoppingKey key;
    private final Component costStr;
    private final Component tooltip;
    private final boolean isCommand;

    public ShopItemWidget(Panel parent, Echo echo, ShopData data, int stageIdx, EchoStage stage, boolean unlocked) {
        super(parent);

        this.data = data;
        this.unlocked = unlocked;

        key = ShoppingKey.of(echo, data);
        costStr = MiscUtil.formatCost(data.cost());

        Component txt = stage.title().orElse(Component.literal(String.valueOf(stageIdx)));
        tooltip = unlocked ?
                Component.translatable("ftbechoes.tooltip.unlocked_by", txt.copy().withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY) :
                Component.translatable("ftbechoes.tooltip.locked");

        setSize(WIDGET_SIZE, WIDGET_SIZE);

        decrButton = new AdjustButton(this, false);
        incrButton = new AdjustButton(this, true);
        iconButton = new IconButton(getActualIcon());

        isCommand = data.command().isPresent();
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
        if (unlocked) {
            ShoppingBasket.CLIENT_INSTANCE.adjust(key, adjustment * (ScreenWrapper.hasShiftDown() ? 10 : 1), isCommand ? 1 : Integer.MAX_VALUE);
        }
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

        if (!unlocked) {
            graphics.pose().translate(0, 0, 300);
            Color4I.DARK_GRAY.withAlpha(160).draw(graphics, x, y, w, h);
            graphics.pose().translate(0, 0, -300);
        }
    }

    private Icon getActualIcon() {
        if (data.stacks().size() == 1) {
            return data.icon().orElse(ItemIcon.getItemIcon(data.stacks().getFirst()));
        } else if (data.stacks().size() > 1) {
            return data.icon().orElse(IconAnimation.fromList(data.stacks().stream().map(ItemIcon::getItemIcon).toList(), false));
        } else {
            return data.icon().orElse(Icon.empty());
        }
    }

    private class AdjustButton extends SimpleTextButton {
        private final boolean forward;

        public AdjustButton(Panel panel, boolean forward) {
            super(panel, Component.literal(forward ? "+": "-"), Icon.empty());
            this.forward = forward;
        }

        @Override
        public void onClicked(MouseButton mouseButton) {
            adjustAmount(forward ? 1 : -1);
        }

        @Override
        public boolean isEnabled() {
            return unlocked;
        }

        @Override
        public WidgetType getWidgetType() {
            return unlocked ? super.getWidgetType() : WidgetType.DISABLED;
        }
    }

    private class IconButton extends SimpleButton {
        public IconButton(Icon icon) {
            super(ShopItemWidget.this, Component.empty(), icon, (b, mb) -> {});
        }

        @Override
        public void playClickSound() {
        }

        @Override
        public void addMouseOverText(TooltipList list) {
            data.stacks().stream()
                    .map(IconButton::stackDesc)
                    .forEach(list::add);
            data.command().flatMap(CommandInfo::description).ifPresent(list::add);
            data.description().ifPresent(list::add);
            list.add(Component.empty());
            list.add(tooltip);
        }

        private static Component stackDesc(ItemStack stack) {
            return stack.getCount() == 1 ?
                    stack.getHoverName() :
                    Component.literal(stack.getCount() + " x ").append(stack.getHoverName());
        }
    }
}
