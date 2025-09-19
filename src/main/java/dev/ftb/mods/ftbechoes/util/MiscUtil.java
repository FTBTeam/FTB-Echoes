package dev.ftb.mods.ftbechoes.util;

import com.mojang.datafixers.util.Function9;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class MiscUtil {
    public static @NotNull Component formatCost(int cost) {
        return Component.empty().append(Component.literal("⬤ ").withStyle(ChatFormatting.YELLOW)).append(String.valueOf(cost)).withStyle(ChatFormatting.DARK_GREEN);
    }

    // TODO move to ftb lib NetworkHelper...
    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> codec1, final Function<C, T1> getter1, final StreamCodec<? super B, T2> codec2, final Function<C, T2> getter2, final StreamCodec<? super B, T3> codec3, final Function<C, T3> getter3, final StreamCodec<? super B, T4> codec4, final Function<C, T4> getter4, final StreamCodec<? super B, T5> codec5, final Function<C, T5> getter5, final StreamCodec<? super B, T6> codec6, final Function<C, T6> getter6, final StreamCodec<? super B, T7> codec7, final Function<C, T7> getter7, final StreamCodec<? super B, T8> codec8, final Function<C, T8> getter8, final StreamCodec<? super B, T9> codec9, final Function<C, T9> getter9, final Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, C> factory) {
        return new StreamCodec<B, C>() {
            public C decode(B buf) {
                T1 t1 = codec1.decode(buf);
                T2 t2 = codec2.decode(buf);
                T3 t3 = codec3.decode(buf);
                T4 t4 = codec4.decode(buf);
                T5 t5 = codec5.decode(buf);
                T6 t6 = codec6.decode(buf);
                T7 t7 = codec7.decode(buf);
                T8 t8 = codec8.decode(buf);
                T9 t9 = codec9.decode(buf);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9);
            }

            public void encode(B buf, C object) {
                codec1.encode(buf, getter1.apply(object));
                codec2.encode(buf, getter2.apply(object));
                codec3.encode(buf, getter3.apply(object));
                codec4.encode(buf, getter4.apply(object));
                codec5.encode(buf, getter5.apply(object));
                codec6.encode(buf, getter6.apply(object));
                codec7.encode(buf, getter7.apply(object));
                codec8.encode(buf, getter8.apply(object));
                codec9.encode(buf, getter9.apply(object));
            }
        };
    }
}
