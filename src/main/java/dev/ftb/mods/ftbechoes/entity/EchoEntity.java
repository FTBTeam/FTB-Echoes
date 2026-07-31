package dev.ftb.mods.ftbechoes.entity;

import dev.ftb.mods.ftbechoes.block.entity.EchoProjectorBlockEntity;
import dev.ftb.mods.ftbechoes.client.FTBEchoesClient;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class EchoEntity extends Villager {
    private static final EntityDataAccessor<String> ECHO_ID
            = SynchedEntityData.defineId(EchoEntity.class, EntityDataSerializers.STRING);

    public EchoEntity(EntityType<? extends Villager> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(ECHO_ID, "ftbechoes:_none_");
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        getEntityData().set(ECHO_ID, valueInput.getString("echo_id").orElseThrow());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        valueOutput.putString("echo_id", getEntityData().get(ECHO_ID));
    }

    public Identifier getEchoId() {
        return Identifier.parse(getEntityData().get(ECHO_ID));
    }

    public void setEchoId(Identifier echoId) {
        getEntityData().set(ECHO_ID, echoId.toString());
    }

    @Override
    public void tick() {
        if (!level().isClientSide()) {
            if (!(level().getBlockEntity(blockPosition()) instanceof EchoProjectorBlockEntity projector) || !projector.validateEchoEntity(this)) {
                discard();
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float v) {
        return false;
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
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        if (player.level().isClientSide() && level().getBlockEntity(blockPosition()) instanceof EchoProjectorBlockEntity projector) {
            FTBEchoesClient.openEchoScreenForProjector(projector);
        }
        return player.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }
}
