package dev.ftb.mods.ftbechoes.client.gui.widget;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleButton;
import dev.ftb.mods.ftblibrary.ui.TextField;
import net.minecraft.network.chat.Component;

import java.util.List;

public class TaskEntryPanel extends Panel {
    private final Button iconButton;
    private final TextField titleField;
    private final TextField descField;

    public TaskEntryPanel(Panel panel, Icon icon, Component title, Component desc) {
        super(panel);

        iconButton = new SimpleButton(this, Component.empty(), icon, (b, mb) -> {});

        titleField = new TextField(this).setText(title);
        descField = new TextField(this).setText(desc).setColor(Color4I.GRAY);
    }

    @Override
    public void addWidgets() {
        add(iconButton);
        add(titleField);
        add(descField);
    }

    @Override
    public void alignWidgets() {
        iconButton.setSize(16, 16);

        List.of(titleField, descField).forEach(w -> {
            w.setMinWidth(parent.getWidth() - 20);
            w.setMaxWidth(parent.getWidth() - 20);
            w.setWidth(parent.getWidth() - 20);
            titleField.reflow();
        });

        iconButton.setX(2);
        titleField.setPos(20, 0);
        descField.setPos(20, titleField.height + 2);

        setSize(parent.getWidth(), titleField.height + descField.height + 4);
    }
}
