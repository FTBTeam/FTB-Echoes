package dev.ftb.mods.ftbechoes.registry;

import dev.ftb.mods.ftbechoes.FTBEchoes;
import dev.ftb.mods.ftbechoes.entity.EchoEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES
            = DeferredRegister.create(Registries.ENTITY_TYPE, FTBEchoes.MOD_ID);

    public static final Supplier<EntityType<EchoEntity>> ECHO
            = register("echo", ModEntityTypes::echo);

    private static <E extends Entity> Supplier<EntityType<E>> register(final String name, final Supplier<EntityType.Builder<E>> sup) {
        return ENTITY_TYPES.register(name, () -> sup.get().build(name));
    }

    private static EntityType.Builder<EchoEntity> echo() {
        return EntityType.Builder.of(EchoEntity::new, MobCategory.MISC)
                .sized(0.6F, 2F)
                .eyeHeight(1.62F)
                .fireImmune()
                .noSummon()
                .clientTrackingRange(10);
    }
}
