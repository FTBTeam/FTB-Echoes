package dev.ftb.mods.ftbechoes.block.entity;

import dev.ftb.mods.ftbechoes.client.FTBEchoesClient;
import dev.ftb.mods.ftbechoes.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class EchoProjectorBlockEntity extends BlockEntity {
    @Nullable
    private ResourceLocation echoId;

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
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if (echoId != null) {
            tag.putString("echo_id", echoId.toString());
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
}
