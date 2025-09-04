package dev.ftb.mods.ftbechoes.integration.jei;

import dev.ftb.mods.ftbechoes.shopping.ShopData;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public record ShopDataSummary(ShopData data, Component echoTitle, Optional<Component> stageTitle) {
}
