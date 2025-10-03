package dev.ftb.mods.ftbechoes.datagen;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.registry.ModBlocks;
import dev.ftb.mods.ftbechoes.registry.ModEntityTypes;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLangProvider extends LanguageProvider {
    public ModLangProvider(PackOutput output) {
        super(output, FTBEchoes.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(ModBlocks.ECHO_PROJECTOR.get(), "Echo Projector");

        add(ModEntityTypes.ECHO.get(), "Echo Projection");

        add("ftbechoes.gui.page.lore", "Lore");
        add("ftbechoes.gui.page.tasks", "Tasks");
        add("ftbechoes.gui.page.shop", "Shop");
        add("ftbechoes.gui.place_order", "Place Order");
        add("ftbechoes.gui.wallet", "Wallet: %s");
        add("ftbechoes.gui.complete_stage", "Complete Stage");
        add("ftbechoes.gui.stage_completed", "Stage Completed!");
        add("ftbechoes.gui.shopping_basket", "Shopping Basket");
        add("ftbechoes.gui.claim_reward", "Claim Reward");
        add("ftbechoes.gui.stage", "Stage %s");
        add("ftbechoes.gui.stages", "Stages");
        add("ftbechoes.gui.collapse_all", "Collapse All");
        add("ftbechoes.gui.expand_all", "Expand All");
        add("ftbechoes.gui.all_complete", "All Stages Complete!");

        add("ftbechoes.commands.invalid_echo", "Invalid Echo id: %s");
        add("ftbechoes.commands.unknown_echo", "Unknown Echo id: %s");
        add("ftbechoes.commands.added_stage", "Added game stage: %s");
        add("ftbechoes.commands.removed_stage", "Removed game stage: %s");
        add("ftbechoes.commands.progress_changed", "Set progress for '%s' on echo '%s' to stage %s");
        add("ftbechoes.commands.progress_changed.failed", "Progress change failed");
        add("ftbechoes.commands.reward_reset", "Reset reward for '%s' on echo '%s', stage %s");
        add("ftbechoes.commands.reward_reset_all", "All rewards reset for '%s' on echo '%s'");
        add("ftbechoes.commands.reward_reset.failed", "Could not reset reward - player has not claimed it?");
        add("ftbechoes.commands.all_progress_reset", "All echo progress & rewards reset for %s");

        add("ftbechoes.message.complete_stage", "Complete Stage");
        add("ftbechoes.message.purchase_success", "Purchase Completed!");
        add("ftbechoes.message.purchase_success.2", "Payment Taken: %s");
        add("ftbechoes.message.purchase_failed", "Purchase Failed!");
        add("ftbechoes.message.no_echo", "No Echo Configured");
        add("ftbechoes.message.reward_claimed", "Reward Claimed!");
        add("ftbechoes.message.reward_not_claimed", "Reward could not be claimed!");
        add("ftbechoes.message.echo_complete", "Echo %s: All stages complete!");
        add("ftbechoes.message.echo_stage_complete", "Echo %s: stage %s complete!");

        add("ftbechoes.tooltip.locked", "Not unlocked yet!");
        add("ftbechoes.tooltip.unlocked_by", "Unlocked by: %s");
        add("ftbechoes.tooltip.claimed", "Already claimed allowed amount.");
        add("ftbechoes.tooltip.total_cost", "Total Cost: %s");
        add("ftbechoes.tooltip.too_expensive", "Too Expensive! Can't place order");
        add("ftbechoes.tooltip.reward_header", "Reward");

        add("ftbechoes.jei.shop.title", "Echo Shop");
        add("ftbechoes.jei.echo_title", "Sold by Echo: %s");
        add("ftbechoes.jei.stage_title", "Unlocked by: %s");

        add("ftbechoes.subtitle.coins_clink", "Coins clink");
    }
}
