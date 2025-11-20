package dev.ftb.mods.ftbechoes.client.gui;

import dev.ftb.mods.ftbechoes.client.gui.widget.AudioButton;
import dev.ftb.mods.ftbechoes.client.gui.widget.ImageButton;
import dev.ftb.mods.ftbechoes.echo.BaseStageEntry;
import dev.ftb.mods.ftbechoes.echo.entries.AudioEntry;
import dev.ftb.mods.ftbechoes.echo.entries.ImageEntry;
import dev.ftb.mods.ftbechoes.echo.entries.TextEntry;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.TextField;
import dev.ftb.mods.ftblibrary.ui.Widget;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class StageEntryRenderers {
    private static final Map<String,EntryRenderer<? extends BaseStageEntry>> map = new HashMap<>();

    public static void init() {
        map.put(TextEntry.ID, new TextEntryRenderer());
        map.put(ImageEntry.ID, new ImageRenderer());
        map.put(AudioEntry.ID, new AudioRenderer());
    }

    public static Optional<EntryRenderer<? extends BaseStageEntry>> get(BaseStageEntry entry) {
        return Optional.ofNullable(map.get(entry.getTypeId()));
    }

    @FunctionalInterface
    public interface EntryRenderer<T extends BaseStageEntry> {
        void addWidgets(Consumer<Widget> widgetAdder, BaseStageEntry entry, Panel panel);

        default T cast(BaseStageEntry entry) {
            //noinspection unchecked
            return (T) entry;
        }
    }

    public static class TextEntryRenderer implements EntryRenderer<TextEntry> {
        @Override
        public void addWidgets(Consumer<Widget> widgetAdder, BaseStageEntry entry, Panel panel) {
            TextEntry t = cast(entry);
            TextField textWidget = new TextField(panel).setText(t.text());
            textWidget.setX(2);
            widgetAdder.accept(textWidget);
        }
    }

    public static class ImageRenderer implements EntryRenderer<ImageEntry> {
        @Override
        public void addWidgets(Consumer<Widget> widgetAdder, BaseStageEntry entry, Panel panel) {
            ImageEntry im = cast(entry);
            ImageButton btn = new ImageButton(panel, Icon.getIcon(im.location()), im.alignment());
            btn.setSize(im.width(), im.height());
            widgetAdder.accept(btn);
        }
    }

    public static class AudioRenderer implements EntryRenderer<AudioEntry> {
        @Override
        public void addWidgets(Consumer<Widget> widgetAdder, BaseStageEntry entry, Panel panel) {
            AudioEntry a = cast(entry);
            AudioButton btn = new AudioButton(panel, a.text().orElse(Component.empty()), a.location());
            btn.setX(2);
            widgetAdder.accept(btn);
        }
    }
}
