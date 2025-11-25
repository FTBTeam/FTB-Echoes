package dev.ftb.mods.ftbechoes.client.gui.widget;

import dev.ftb.mods.ftbechoes.client.gui.EchoSoundClipHandler;
import dev.ftb.mods.ftbechoes.client.gui.Textures;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class AudioButton extends SimpleTextButton {
    private static final Icon INACTIVE = Icon.getIcon(Textures.SPEAKER);
    private static final Icon INACTIVE_MISSING = Icon.getIcon(Textures.SPEAKER).combineWith(Icons.CANCEL.withColor(Color4I.WHITE.withAlpha(128)));
    private static final Icon ACTIVE =  Icon.getIcon(Textures.SPEAKER_ACTIVE);

    private final SoundEvent sound;
    private final ResourceLocation location;

    public AudioButton(Panel panel, Component text, ResourceLocation location) {
        super(panel, text, INACTIVE);

        this.location = location;
        this.sound = findSound(location);
    }

    private SoundEvent findSound(ResourceLocation location) {
        String locale = Minecraft.getInstance().getLanguageManager().getSelected();
        ResourceLocation loc1 = ResourceLocation.fromNamespaceAndPath(location.getNamespace(), locale + "/" + location.getPath());
        ResourceLocation loc2 = ResourceLocation.fromNamespaceAndPath(location.getNamespace(), "en_us/" + location.getPath());

        return BuiltInRegistries.SOUND_EVENT.getOptional(loc1)
                .or(() -> BuiltInRegistries.SOUND_EVENT.getOptional(loc2))
                .or(() -> BuiltInRegistries.SOUND_EVENT.getOptional(location))
                .orElse(SoundEvents.EMPTY);
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        theme.drawPanelBackground(graphics, x + 2, y + 3, w - 4, h - 6);
        GuiHelper.drawHollowRect(graphics, x + 1, y + 2, w - 2, h - 4, theme.getContentColor(WidgetType.DISABLED), true);
    }

    @Override
    public void onClicked(MouseButton mouseButton) {
        if (sound != SoundEvents.EMPTY) {
            if (EchoSoundClipHandler.INSTANCE.isPlayingSound(sound)) {
                EchoSoundClipHandler.INSTANCE.stopPlayingSound();
            } else {
                EchoSoundClipHandler.INSTANCE.startPlayingSound(sound);
            }
        }
    }

    @Override
    public void tick() {
        setIcon(sound == SoundEvents.EMPTY ? INACTIVE_MISSING : (EchoSoundClipHandler.INSTANCE.isPlayingSound(sound) ? ACTIVE : INACTIVE));
    }

    @Override
    public void addMouseOverText(TooltipList list) {
        if (sound == SoundEvents.EMPTY) {
            list.add(Component.literal("missing sound event: " + location));
        }
    }
}
