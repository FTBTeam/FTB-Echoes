package dev.ftb.mods.ftbechoes.integration.magic_coins;

import dev.ftb.mods.ftbechoes.shopping.CurrencyPlugin;
import net.minecraft.world.entity.player.Player;
import net.sirgrantd.magic_coins.api.MagicCoinsApi;

public enum MagicCoinsCurrency implements CurrencyPlugin {
    INSTANCE;

    @Override
    public int getTotalCurrency(Player player) {
        return MagicCoinsApi.getTotalCoins(player);
    }

    @Override
    public boolean takeCurrency(Player player, int amount) {
        if (getTotalCurrency(player) >= amount) {
            MagicCoinsApi.removeCoins(player, amount);
            return true;
        }
        return false;
    }
}
