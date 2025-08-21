package dev.ftb.mods.ftbechoes.entity;

import dev.ftb.mods.ftbechoes.block.entity.EchoProjectorBlockEntity;
import dev.ftb.mods.ftbechoes.client.FTBEchoesClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class EchoEntity extends Entity {
    private static final EntityDataAccessor<String> ECHO_ID
            = SynchedEntityData.defineId(EchoEntity.class, EntityDataSerializers.STRING);

    public EchoEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ECHO_ID, "ftbechoes:_none_");
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        getEntityData().set(ECHO_ID, compound.getString("echo_id"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putString("echo_id", getEntityData().get(ECHO_ID));
    }

    public void setEchoId(@NotNull ResourceLocation echoId) {
        getEntityData().set(ECHO_ID, echoId.toString());
    }

    @NotNull
    public ResourceLocation getEchoId() {
        return ResourceLocation.parse(getEntityData().get(ECHO_ID));
    }

    @Override
    public void tick() {
        if (!(level().getBlockEntity(blockPosition()) instanceof EchoProjectorBlockEntity projector) || !getEchoId().equals(projector.getEchoId())) {
            discard();
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.level().isClientSide && level().getBlockEntity(blockPosition()) instanceof EchoProjectorBlockEntity projector) {
            FTBEchoesClient.openEchoScreen(projector);
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }
}
