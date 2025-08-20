package dev.ftb.mods.ftbechoes.client;

import dev.ftb.mods.ftbechoes.client.gui.widget.AudioButton;
import dev.ftb.mods.ftbechoes.client.gui.widget.ImageButton;
import dev.ftb.mods.ftbechoes.echo.BaseStageEntry;
import dev.ftb.mods.ftbechoes.echo.entries.AudioEntry;
import dev.ftb.mods.ftbechoes.echo.entries.ImageEntry;
import dev.ftb.mods.ftbechoes.echo.entries.TextEntry;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

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
            return (T) entry;
        }
    }

    public static class TextEntryRenderer implements EntryRenderer<TextEntry> {
        @Override
        public void addWidgets(Consumer<Widget> widgetAdder, BaseStageEntry entry, Panel panel) {
            TextEntry t = cast(entry);
            TextField textWidget = new TextField(panel).setText(t.text());
//            textWidget.setWidth(panel.getWidth() - 4);
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
            var sound = BuiltInRegistries.SOUND_EVENT.getOptional(a.location()).orElse(SoundEvents.EMPTY);
            AudioButton btn = new AudioButton(panel, a.text().orElse(Component.empty()), sound);
            btn.setX(2);
            widgetAdder.accept(btn);
        }
    }
}
