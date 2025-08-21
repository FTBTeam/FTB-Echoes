package dev.ftb.mods.ftbechoes.block.entity;

import dev.ftb.mods.ftbechoes.client.FTBEchoesClient;
import dev.ftb.mods.ftbechoes.entity.EchoEntity;
import dev.ftb.mods.ftbechoes.registry.ModBlockEntityTypes;
import dev.ftb.mods.ftbechoes.registry.ModEntityTypes;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public class EchoProjectorBlockEntity extends BlockEntity {
    @Nullable
    private ResourceLocation echoId;
    private UUID workerID = Util.NIL_UUID;

    public EchoProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.ECHO_PROJECTOR.get(), pos, state);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        // server side, chunk sending
        CompoundTag compound = super.getUpdateTag(provider);
        if (echoId != null) compound.putString("echo_id", echoId.toString());
        return compound;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        // server side, block update (calls getUpdateTag())
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        // client side, chunk sending
        super.handleUpdateTag(tag, provider);

        FTBEchoesClient.onProjectorUpdated(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        // client side, block update
        handleUpdateTag(pkt.getTag(), lookupProvider);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        echoId = tag.contains("echo_id", CompoundTag.TAG_STRING) ?
                ResourceLocation.parse(tag.getString("echo_id")) :
                null;

        workerID = tag.contains("worker_id") ? tag.getUUID("worker_id") : Util.NIL_UUID;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if (echoId != null) {
            tag.putString("echo_id", echoId.toString());
        }
        if (workerID != Util.NIL_UUID) {
            tag.putUUID("worker_id", workerID);
        }
    }

    @Override
    public void setRemoved() {
        if (!workerID.equals(Util.NIL_UUID) && level instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(workerID);
            if (entity != null) {
                entity.discard();
            }
        }
    }

    @Nullable
    public ResourceLocation getEchoId() {
        return echoId;
    }

    public void setEchoId(@Nullable ResourceLocation echoId) {
        this.echoId = echoId;
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public void tickServer(ServerLevel serverLevel) {
        checkForEntity(serverLevel);
    }

    private void checkForEntity(ServerLevel level) {
        Entity currentEcho = level.getEntity(workerID);

        if (currentEcho == null && getEchoId() != null) {
            EchoEntity newEcho = new EchoEntity(ModEntityTypes.ECHO.get(), level);
            newEcho.setPos(Vec3.atCenterOf(getBlockPos()));
            newEcho.setEchoId(getEchoId());
            level.addFreshEntity(newEcho);
            workerID = newEcho.getUUID();
            setChanged();
        }
    }
}
