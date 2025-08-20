package dev.ftb.mods.ftbechoes.datagen;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLangProvider extends LanguageProvider {
    public ModLangProvider(PackOutput output) {
        super(output, FTBEchoes.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("ftbechoes.gui.page.lore", "Lore");
        add("ftbechoes.gui.page.tasks", "Tasks");
        add("ftbechoes.gui.page.shop", "Shop");
        add("ftbechoes.gui.place_order", "Place Order");
        add("ftbechoes.gui.wallet", "Wallet: %s");

        add("ftbechoes.commands.invalid_echo", "Invalid echo id: %s");
        add("ftbechoes.commands.unknown_echo", "Unknown echo id: %s");
        add("ftbechoes.message.complete_stage", "Complete Stage");
        add("ftbechoes.message.all_complete", "All Stages Complete!");
        add("ftbechoes.message.purchase_success", "Purchase Completed!");
        add("ftbechoes.message.purchase_success.2", "Payment Taken: %s");
        add("ftbechoes.message.purchase_failed", "Purchase Failed!");

        add("ftbechoes.tooltip.unlocked_by", "Unlocked by Stage: %s");
        add("ftbechoes.tooltip.total_cost", "Total Cost: %s");
        add("ftbechoes.tooltip.too_expensive", "Too Expensive! Can't place order");
    }
}
