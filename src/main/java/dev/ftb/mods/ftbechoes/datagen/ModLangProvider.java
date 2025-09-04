package dev.ftb.mods.ftbechoes.datagen;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLangProvider extends LanguageProvider {
    public ModLangProvider(PackOutput output) {
        super(output, FTBEchoes.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(ModBlocks.ECHO_PROJECTOR.get(), "Echo Projector");

        add("ftbechoes.gui.page.lore", "Lore");
        add("ftbechoes.gui.page.tasks", "Tasks");
        add("ftbechoes.gui.page.shop", "Shop");
        add("ftbechoes.gui.place_order", "Place Order");
        add("ftbechoes.gui.wallet", "Wallet: %s");
        add("ftbechoes.gui.complete_stage", "Complete Stage");
        add("ftbechoes.gui.shopping_basket", "Shopping Basket");

        add("ftbechoes.commands.invalid_echo", "Invalid Echo id: %s");
        add("ftbechoes.commands.unknown_echo", "Unknown Echo id: %s");
        add("ftbechoes.message.complete_stage", "Complete Stage");
        add("ftbechoes.message.all_complete", "All Stages Complete!");
        add("ftbechoes.message.purchase_success", "Purchase Completed!");
        add("ftbechoes.message.purchase_success.2", "Payment Taken: %s");
        add("ftbechoes.message.purchase_failed", "Purchase Failed!");
        add("ftbechoes.message.no_echo", "No Echo Configured");

        add("ftbechoes.tooltip.locked", "Not available yet!");
        add("ftbechoes.tooltip.unlocked_by", "Unlocked by: %s");
        add("ftbechoes.tooltip.total_cost", "Total Cost: %s");
        add("ftbechoes.tooltip.too_expensive", "Too Expensive! Can't place order");

        add("ftbechoes.jei.shop.title", "Echo Shop");
        add("ftbechoes.jei.echo_title", "Sold by Echo: %s");
        add("ftbechoes.jei.stage_title", "Unlocked by: %s");
    }
}
