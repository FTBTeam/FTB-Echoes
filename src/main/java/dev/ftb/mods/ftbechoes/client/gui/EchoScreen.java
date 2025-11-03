package dev.ftb.mods.ftbechoes.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.client.ClientProgress;
import dev.ftb.mods.ftbechoes.echo.Echo;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import dev.ftb.mods.ftbechoes.net.PlaceOrderMessage;
import dev.ftb.mods.ftbechoes.net.SelectEchoMessage;
import dev.ftb.mods.ftbechoes.shopping.ShoppingBasket;
import dev.ftb.mods.ftbechoes.util.MiscUtil;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractThreePanelScreen;
import dev.ftb.mods.ftblibrary.ui.misc.NordColors;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;
import java.util.function.BooleanSupplier;

public class EchoScreen extends AbstractThreePanelScreen<EchoScreen.MainPanel> {
    static EchoScreen.Page currentPage = EchoScreen.Page.LORE;

    private final BlockPos projectorPos;
    private Echo echo;
    private boolean pendingScrollToEnd;

    public EchoScreen(BlockPos projectorPos, Echo echo) {
        super();

        this.projectorPos = projectorPos;
        this.echo = echo;

        showCloseButton(false);
    }

    public void setEcho(Echo echo) {
        this.echo = echo;
        refreshWidgets();
    }

    @Override
    public boolean onInit() {
        boolean ok = setSizeProportional(0.85f, 0.8f);
        setWidth(Math.min(getWidth(), 600));
        return ok;
    }

    @Override
    public void onPostInit() {
        if (currentPage == Page.LORE) {
            scrollBar.setValue(scrollBar.getMaxValue());
        }
    }

    @Override
    protected void doCancel() {
    }

    @Override
    protected void doAccept() {
    }

    @Override
    protected int getTopPanelHeight() {
        return 25 + getTheme().getFontHeight() + 9;
    }

    @Override
    protected Panel createTopPanel() {
        return new TopPanel();
    }

    @Override
    protected MainPanel createMainPanel() {
        return new MainPanel();
    }

    @Override
    protected Panel createBottomPanel() {
        return new BottomPanel(this);
    }

    @Override
    public void alignWidgets() {
        super.alignWidgets();

        if (pendingScrollToEnd) {
            scrollBar.setValue(scrollBar.getMaxValue());
            pendingScrollToEnd = false;
        }
    }

    public void onProgressUpdated() {
        refreshWidgets();
        if (currentPage == Page.LORE) {
            pendingScrollToEnd = true;
        }
    }

    public BlockPos getProjectorPos() {
        return projectorPos;
    }

    static class PurchaseButton extends SimpleTextButton {
        public PurchaseButton(Panel parent) {
            super(parent, Component.translatable("ftbechoes.gui.place_order"), Icons.MONEY_BAG);
        }

        @Override
        public void onClicked(MouseButton mouseButton) {
            // TODO send order to server
            playClickSound();

            PacketDistributor.sendToServer(new PlaceOrderMessage(ShoppingBasket.CLIENT_INSTANCE));
        }

        @Override
        public boolean shouldAddMouseOverText() {
            return isMouseOver;
        }

        @Override
        public void addMouseOverText(TooltipList list) {
            super.addMouseOverText(list);

            if (ShoppingBasket.CLIENT_INSTANCE.hasContents()) {
                list.add(Component.translatable("ftbechoes.gui.shopping_basket").withStyle(ChatFormatting.YELLOW));
                ShoppingBasket.CLIENT_INSTANCE.forEach((key, count) -> EchoManager.getClientInstance().getShopData(key).ifPresent(data -> {
                    List<MutableComponent> lines = new ArrayList<>();
                    List<ItemStack> stacks = data.stacks();
                    for (int i = 0; i < stacks.size(); i++) {
                        ItemStack stack = stacks.get(i);
                        lines.add(Component.literal(i == 0 ? "• " : "  ").append(count * stack.getCount() + " x ").append(stack.getHoverName()));
                    }
                    data.command().ifPresent(cmd -> cmd.description().forEach(d -> lines.add(Component.literal("• ").append(d))));
                    if (!lines.isEmpty()) {
                        lines.getLast().append(": ").append(MiscUtil.formatCost(count * data.cost()));
                    }
                    lines.forEach(list::add);
                }));
                list.add(Component.empty());
                list.add(Component.translatable("ftbechoes.tooltip.total_cost", MiscUtil.formatCost(ShoppingBasket.CLIENT_INSTANCE.getTotalCost())));
                if (ShoppingBasket.CLIENT_INSTANCE.getTotalCost() > FTBEchoes.currencyProvider().getTotalCurrency(Minecraft.getInstance().player)) {
                    list.add(Component.translatable("ftbechoes.tooltip.too_expensive").withStyle(ChatFormatting.RED));
                }
            }
        }

        @Override
        public WidgetType getWidgetType() {
            return isEnabled() ? super.getWidgetType() : WidgetType.DISABLED;
        }

        @Override
        public boolean isEnabled() {
            return ShoppingBasket.CLIENT_INSTANCE.hasContents()
                    && currentPage == Page.SHOP
                    && ShoppingBasket.CLIENT_INSTANCE.getTotalCost() <= FTBEchoes.currencyProvider().getTotalCurrency(Minecraft.getInstance().player);
        }

        @Override
        public boolean shouldDraw() {
            return currentPage == Page.SHOP;
        }
    }

    private class TopPanel extends Panel {
        private final TextField label;
        private final Button settingsButton;
        private final Map<EchoScreen.Page, EchoScreen.PageButton> buttons = new EnumMap<>(EchoScreen.Page.class);

        public TopPanel() {
            super(EchoScreen.this);

            label = new TextField(this);
            settingsButton = SimpleTextButton.create(this, Component.empty(), Icons.SETTINGS, this::showEchoSelector);
            buttons.put(EchoScreen.Page.LORE, new EchoScreen.PageButton(EchoScreen.Page.LORE, this, Icons.BOOK));
            buttons.put(EchoScreen.Page.SHOP, new EchoScreen.PageButton(EchoScreen.Page.SHOP, this, Icons.MONEY_BAG));

            // only show selector drop-down after at least one stage has been completed
            buttons.get(Page.LORE).setDropdownAction(
                    this::showStageSelector,
                    () -> echo != null && ClientProgress.get().isStageCompleted(echo.id(), 0)
            );
        }

        @Override
        public void addWidgets() {
            var player = Objects.requireNonNull(Minecraft.getInstance().player);
            if (player.hasPermissions(Commands.LEVEL_GAMEMASTERS) && player.isCreative()) {
                add(settingsButton);
            }
            if (echo == null) {
                add(label.setText(Component.translatable("ftbechoes.message.no_echo").withStyle(ChatFormatting.ITALIC, ChatFormatting.GOLD)));
            } else {
                add(label.setText(Component.empty().withStyle(ChatFormatting.YELLOW).append(echo.title())));
            }
            for (EchoScreen.Page p : buttons.keySet()) {
                add(buttons.get(p));
            }
        }

        @Override
        public void alignWidgets() {
            settingsButton.setPosAndSize(width - 18, 2, 16, 16);

            label.setPos(4, 5);
            label.setWidth(width);

            int bw = width / (buttons.size() + 1) - 4;
            int by = getTheme().getFontHeight() + 8;
            for (PageButton w : buttons.values()) {
                w.setPosAndSize(4 + w.page.ordinal() * (bw + 2), by, bw, height - by);
            }
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            super.drawBackground(graphics, theme, x, y, w, h);
            int col = 0xff585d66;  // blends best with tabbed outline
            graphics.hLine(x, x + buttons.get(Page.LORE).posX - 1, y + height - 2, col);
            graphics.hLine(x + buttons.get(Page.LORE).posX + buttons.get(Page.LORE).width, x + buttons.get(Page.SHOP).posX - 1, y + height - 2, col);
            graphics.hLine(x + buttons.get(Page.SHOP).posX + buttons.get(Page.SHOP).width, x + width - 1, y + height - 2, col);
            graphics.hLine(x, x + width - 1, y + height - 1, NordColors.POLAR_NIGHT_1.rgba());
        }

        @Override
        public boolean keyPressed(Key key) {
            if (key.is(InputConstants.KEY_ADD) || key.is(InputConstants.KEY_EQUALS)) {
                collapseAll(false);
                return true;
            } else if (key.is(InputConstants.KEY_MINUS) || key.is(333) /* KP_SUBTRACT */) {
                collapseAll(true);
                return true;
            } else {
                return super.keyPressed(key);
            }
        }

        private void showEchoSelector(MouseButton mb) {
            openContextMenu(Util.make(new ArrayList<>(), list ->
                    EchoManager.getClientInstance().getEchoes().forEach(echo -> {
                        Icon icon = isCurrentEcho(echo) ? Icons.CHECK : Icon.empty();
                        list.add(new ContextMenuItem(echo.title(), icon, btn -> selectEcho(echo)));
                    })
            ));
        }

        private void showStageSelector() {
            openContextMenu(Util.make(new ArrayList<>(), list -> {
                        list.add(ContextMenuItem.title(Component.translatable("ftbechoes.gui.stages")));
                        list.add(ContextMenuItem.separator());
                        for (int i = 0; i < echo.stages().size(); i++) {
                            final int stageIdx = i;
                            if (ClientProgress.get().isStageCompleted(echo.id(), i)) {
                                list.add(new ContextMenuItem(echo.stages().get(stageIdx).title(), Icons.BLUE_BUTTON, btn -> scrollToStage(stageIdx)));
                            }
                        }
                        list.add(ContextMenuItem.separator());
                        list.add(new ContextMenuItem(Component.translatable("ftbechoes.gui.expand_all")
                                .append(Component.literal(" [+]").withStyle(ChatFormatting.GRAY)),
                                Icons.EXPAND, btn -> collapseAll(false)));
                        list.add(new ContextMenuItem(Component.translatable("ftbechoes.gui.collapse_all")
                                .append(Component.literal(" [-]").withStyle(ChatFormatting.GRAY)),
                                Icons.COLLAPSE, btn -> collapseAll(true)));
                    }
            ));
        }

        private void selectEcho(Echo echo) {
            if (!isCurrentEcho(echo)) {
                PacketDistributor.sendToServer(new SelectEchoMessage(EchoScreen.this.projectorPos, echo.id()));
            }
        }

        private void scrollToStage(int stageIdx) {
            if (EchoScreen.this.mainPanel.pages.get(Page.LORE) instanceof LorePanel lorePanel) {
                if (lorePanel.isCollapsed(stageIdx)) {
                    lorePanel.setCollapsed(stageIdx, false);
                }
                EchoScreen.this.scrollBar.setValue(lorePanel.getScrollPos(stageIdx));
            }
        }

        private void collapseAll(boolean collapse) {
            playClickSound();
            if (EchoScreen.this.mainPanel.pages.get(Page.LORE) instanceof LorePanel lorePanel) {
                lorePanel.setAllCollapsed(collapse);
            }
        }

        private boolean isCurrentEcho(Echo e) {
            return EchoScreen.this.echo != null && e.id().equals(EchoScreen.this.echo.id());
        }
    }

    public class MainPanel extends Panel {
        private final Map<Page, PagePanel> pages = new EnumMap<>(Page.class);

        public MainPanel() {
            super(EchoScreen.this);

            pages.put(Page.LORE, new LorePanel(this, EchoScreen.this));
            pages.put(Page.SHOP, new ShopPanel(this, EchoScreen.this));
        }

        @Override
        public void addWidgets() {
            addAll(pages.values());
        }

        @Override
        public void alignWidgets() {
            pages.values().forEach(p -> {
                p.setPosAndSize(0, 0, width, height);
                p.alignWidgets();
            });
            if (getContentHeight() <= getHeight()) {
                EchoScreen.this.scrollBar.setValue(0.0);
            }
        }

        @Override
        public int getContentHeight() {
            return pages.get(currentPage).getContentHeight();
        }

        public void storeScrollPos() {
            pages.get(currentPage).storeScrollPos();
        }

        public double fetchScrollPos() {
            return pages.get(currentPage).fetchScrollPos();
        }

        public void onSwitchAway() {
            pages.get(currentPage).onSwitchAway();
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            NordColors.POLAR_NIGHT_0.draw(graphics, x, y, w, h);
        }
    }

    private static class BottomPanel extends Panel {
        private final PurchaseButton purchaseButton;

        public BottomPanel(Panel panel) {
            super(panel);

            purchaseButton = new PurchaseButton(this);
        }

        @Override
        public void addWidgets() {
            add(purchaseButton);
        }

        @Override
        public void alignWidgets() {
            purchaseButton.setPos(width - purchaseButton.width - 5, 2);
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            theme.drawPanelBackground(graphics, x, y, w, h);
            Color4I.GRAY.withAlpha(64).draw(graphics, x, y, w, 1);
        }

        @Override
        public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            super.draw(graphics, theme, x, y, w, h);

            var c = MiscUtil.formatCost(FTBEchoes.currencyProvider().getTotalCurrency(Minecraft.getInstance().player));

            theme.drawString(graphics, Component.translatable("ftbechoes.gui.wallet", c), x + 5, y + 8, Color4I.GREEN.rgba());
        }
    }

    abstract static class PagePanel extends Panel {
        private final EchoScreen echoScreen;
        private final EchoScreen.Page page;
        private double lastScrollPos = 0.0;

        public PagePanel(Panel parent, EchoScreen echoScreen, EchoScreen.Page page) {
            super(parent);

            this.echoScreen = echoScreen;
            this.page = page;
        }

        @Override
        public boolean shouldDraw() {
            return page == currentPage;
        }

        @Override
        public boolean isEnabled() {
            return page == currentPage;
        }

        protected Optional<Echo> getEcho() {
            return Optional.ofNullable(echoScreen.echo);
        }

        protected final void vSpace(int space) {
            add(new VerticalSpaceWidget(this, space));
        }

        public void storeScrollPos() {
            lastScrollPos = echoScreen.scrollBar.getValue();
        }

        public double fetchScrollPos() {
            return lastScrollPos;
        }

        public void scrollToTop() {
            echoScreen.scrollBar.setValue(0.0);
        }

        public void onSwitchAway() {
        }
    }

    public enum Page {
        LORE("lore"),
        SHOP("shop");

        private final String name;

        Page(String name) {
            this.name = name;
        }

        public Component getLabel() {
            return Component.translatable("ftbechoes.gui.page." + name);
        }
    }

    private class PageButton extends SimpleTextButton {
        private final EchoScreen.Page page;
        private Runnable onDropDownClicked = null;
        private BooleanSupplier dropdownPredicate = () -> false;

        public PageButton(EchoScreen.Page page, Panel panel, Icon icon) {
            super(panel, page.getLabel(), icon);

            this.page = page;
        }

        public void setDropdownAction(Runnable onClicked, BooleanSupplier predicate) {
            onDropDownClicked = onClicked;
            dropdownPredicate = predicate;
        }

        @Override
        public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            this.drawBackground(graphics, theme, x, y, w, h);

            var iconSize = 12;

            var off = (h - iconSize) / 2;
            FormattedText title = getTitle();
            var textX = x + 4;
            var textY = y + (h - theme.getFontHeight() + 1) / 2;

            var sw = theme.getStringWidth(title);
            var mw = w - off + iconSize - 6;

            if (sw > mw) {
                title = theme.trimStringToWidth(title, mw);
            }

            drawIcon(graphics, theme, x + off, y + off + 1, iconSize, iconSize);
            textX += off + iconSize;

            theme.drawString(graphics, title, textX, textY + 1, theme.getContentColor(getWidgetType()), 0);

            if (dropdownPredicate.getAsBoolean() && onDropDownClicked != null) {
                int x1 = x + width - 16;
                int x2 = x + width - 6;
                int y0 = y + height / 2;
                graphics.hLine(x1, x2, y0 - 3, theme.getContentColor(WidgetType.NORMAL).rgba());
                graphics.hLine(x1, x2, y0, theme.getContentColor(WidgetType.NORMAL).rgba());
                graphics.hLine(x1, x2, y0 + 3, theme.getContentColor(WidgetType.NORMAL).rgba());
            }
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            theme.drawHorizontalTab(graphics, x, y, w, h, EchoScreen.currentPage == page);
        }

        @Override
        public void onClicked(MouseButton mouseButton) {
            if (dropdownPredicate.getAsBoolean() && EchoScreen.currentPage == page && getMouseX() < getX() + width - 4 && getMouseX() > getX() + width - 18) {
                onDropDownClicked.run();
                return;
            }

            MainPanel mainPanel = EchoScreen.this.mainPanel;
            PanelScrollBar scrollBar = EchoScreen.this.scrollBar;

            mainPanel.onSwitchAway();
            mainPanel.storeScrollPos();
            EchoScreen.currentPage = page;
            mainPanel.refreshWidgets();
            scrollBar.setValue(mainPanel.fetchScrollPos());
        }
    }
}
