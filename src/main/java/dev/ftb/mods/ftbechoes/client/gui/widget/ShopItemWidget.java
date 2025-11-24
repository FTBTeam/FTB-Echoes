package dev.ftb.mods.ftbechoes.client.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbechoes.echo.Echo;
import dev.ftb.mods.ftbechoes.echo.EchoStage;
import dev.ftb.mods.ftbechoes.echo.progress.TeamProgress;
import dev.ftb.mods.ftbechoes.integration.quests.FTBQuestsIntegration;
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
import dev.ftb.mods.ftblibrary.util.ModUtils;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopItemWidget extends Panel {
    public static final int WIDGET_SIZE = 64;
    public static final int INC_BTN_SIZE = 14;
    public static final int ICON_BTN_SIZE = 24;

    private final ShopData data;
    private final boolean unlocked;
    private final Button incrButton, decrButton;
    private final Button iconButton;
    private final ShoppingKey key;
    private final Component costStr;
    private final Component tooltip;
    private final boolean isCommand;
    private final TeamProgress teamProgress;
    private final Map<String,List<ItemStack>> byMod;  // organising for tooltip purposes

    @Nullable
    private List<Component> extraInfo = null;

    public ShopItemWidget(Panel parent, Echo echo, ShopData data, EchoStage stage, boolean unlocked, TeamProgress teamProgress) {
        super(parent);

        this.data = data;
        this.unlocked = unlocked;
        this.teamProgress = teamProgress;

        key = ShoppingKey.of(echo, data);
        costStr = MiscUtil.formatCost(data.cost());

        byMod = new HashMap<>();
        for (ItemStack stack : data.stacks()) {
            byMod.computeIfAbsent(getModForItem(stack), k -> new ArrayList<>()).add(stack);
        }

        if (data.maxClaims().isPresent() && getRemainingLimit() <= 0) {
            tooltip = Component.translatable("ftbechoes.tooltip.claimed");
        } else {
            tooltip = unlocked ?
                    Component.translatable("ftbechoes.tooltip.unlocked_by", stage.title().copy().withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY) :
                    Component.translatable("ftbechoes.tooltip.locked");
        }

        setSize(WIDGET_SIZE, WIDGET_SIZE);

        decrButton = new AdjustButton(this, false);
        incrButton = new AdjustButton(this, true);
        iconButton = new IconButton(getActualIcon());

        isCommand = data.command().isPresent();

        if (unlocked && ModList.get().isLoaded("ftbquests")) {
            extraInfo = FTBQuestsIntegration.getLootData(this.data);
        }
    }

    private String getModForItem(ItemStack stack) {
        if (BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals("ftbquests:lootcrate")) {
            return "";
        }
        return ModUtils.getModName(stack.getItem()).orElse("");
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
        iconButton.setPosAndSize((width - ICON_BTN_SIZE) / 2, 16, ICON_BTN_SIZE, ICON_BTN_SIZE);
    }

    private void adjustAmount(int adjustment) {
        if (unlocked) {
            ShoppingBasket.CLIENT_INSTANCE.adjust(
                    key,
                    adjustment * (ScreenWrapper.hasShiftDown() ? 10 : 1),
                    isCommand ? 1 : data.maxClaims().map((max) -> getRemainingLimit()).orElse(Integer.MAX_VALUE));
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
        if (data.maxClaims().isPresent()) {
            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.translate(x + 4, y + 4, 0);
            pose.scale(0.75F, 0.75F, 1F);
            theme.drawString(graphics, Component.translatable("ftbechoes.tooltip.stock"), 0, 0, theme.getContentColor(WidgetType.NORMAL).withAlpha(200), 0);
            pose.popPose();
            theme.drawString(graphics, Component.literal(String.valueOf(getRemainingLimit())), x + 4, y + 12, theme.getContentColor(WidgetType.NORMAL), 0);
        }

        if (!unlocked || (data.maxClaims().isPresent() && getRemainingLimit() <= 0)) {
            graphics.pose().translate(0, 0, 300);
            Color4I.DARK_GRAY.withAlpha(160).draw(graphics, x, y, w, h);
            graphics.pose().translate(0, 0, -300);
        }
    }

    @Override
    public void addMouseOverText(TooltipList list) {
        if (getMouseY() < getY() + 16 && getMouseX() < getX() + width / 2 && data.maxClaims().isPresent()) {
            list.add(Component.translatable("ftbechoes.gui.stock_remaining", getRemainingLimit(), data.maxClaims().get()));
            list.add(Component.translatable("ftbechoes.gui.stock_limit." + (data.perPlayerMax() ? "player": "team")).withStyle(ChatFormatting.GRAY));
        } else {
            super.addMouseOverText(list);
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

    int getRemainingLimit() {
        return data.maxClaims()
                .map((max) -> teamProgress.getRemainingShopStock(Minecraft.getInstance().player, key, data))
                .orElse(Integer.MAX_VALUE);
    }

    private class AdjustButton extends SimpleTextButton {
        private final boolean forward;

        public AdjustButton(ShopItemWidget panel, boolean forward) {
            super(panel, Component.literal(forward ? "+": "-"), Icon.empty());
            this.forward = forward;
        }

        @Override
        public void onClicked(MouseButton mouseButton) {
            adjustAmount(forward ? 1 : -1);
        }

        @Override
        public boolean isEnabled() {
            return unlocked && ((ShopItemWidget)this.parent).getRemainingLimit() > 0;
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
            byMod.forEach((modName, stacks) -> {
                stacks.forEach(s -> list.add(IconButton.stackDesc(s)));
                if (!modName.isEmpty()) {
                    list.add(Component.literal(modName).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                }
            });
            data.command().ifPresent(cmd -> cmd.description().forEach(list::add));
            data.description().forEach(list::add);
            list.add(Component.empty());
            list.add(tooltip);

            if (extraInfo != null) {
                list.add(Component.empty());
                if (!ScreenWrapper.hasShiftDown()) {
                    list.add(Component.translatable("ftbechoes.tooltip.hold_shift_for_more").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                } else {
                    extraInfo.forEach(list::add);
                }
            }
        }

        private static Component stackDesc(ItemStack stack) {
            return stack.getCount() == 1 ?
                    stack.getHoverName() :
                    Component.literal(stack.getCount() + " x ").append(stack.getHoverName());
        }
    }
}
