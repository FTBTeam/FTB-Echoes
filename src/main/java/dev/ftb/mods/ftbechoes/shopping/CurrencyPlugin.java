package dev.ftb.mods.ftbechoes.shopping;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import net.minecraft.world.entity.player.Player;

public interface CurrencyPlugin {
    int getTotalCurrency(Player player);

    boolean takeCurrency(Player player, int amount);

    class DummyCurrencySystem implements CurrencyPlugin {
        // just for testing!

        @Override
        public int getTotalCurrency(Player player) {
            return 100;
        }

        @Override
        public boolean takeCurrency(Player player, int amount) {
            FTBEchoes.LOGGER.debug("would take ⬤{}", amount);
            return amount < 100;
        }
    }
}
