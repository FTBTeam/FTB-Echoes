package dev.ftb.mods.ftbechoes.integration.jei;

import dev.ftb.mods.ftbechoes.shopping.ShopData;
import net.minecraft.network.chat.Component;

public record ShopDataSummary(ShopData data, Component echoTitle, Component stageTitle) {
}
