package dev.ftb.mods.ftbechoes.block.entity;

import dev.ftb.mods.ftbechoes.client.FTBEchoesClient;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import dev.ftb.mods.ftbechoes.entity.EchoEntity;
import dev.ftb.mods.ftbechoes.registry.ModBlockEntityTypes;
import dev.ftb.mods.ftbechoes.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public class EchoProjectorBlockEntity extends BlockEntity {
    @Nullable private Identifier echoId;
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
    public void handleUpdateTag(ValueInput input) {
        super.handleUpdateTag(input);

        FTBEchoesClient.onProjectorUpdated(this);
    }

    @Override
    public void onDataPacket(Connection net, ValueInput valueInput) {
        handleUpdateTag(valueInput);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        echoId = input.read("echo_id", Identifier.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        if (echoId != null) {
            output.store("echo_id", Identifier.CODEC, echoId);
        }
    }

    @Nullable
    public Identifier getEchoId() {
        return echoId;
    }

    public void setEchoId(@Nullable Identifier echoId) {
        this.echoId = echoId;
        if (level instanceof ServerLevel serverLevel) {
            createNewEchoEntity();
            setChanged();
            serverLevel.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void onLoad() {
        if (!level.isClientSide()) {
            createNewEchoEntity();
        }
    }

    private void createNewEchoEntity() {
        EchoManager.getServerInstance().getEcho(getEchoId()).ifPresent(echo -> {
            EchoEntity newEcho = new EchoEntity(ModEntityTypes.ECHO.get(), level);
            newEcho.setNoAi(true);
            newEcho.setSilent(true);
            newEcho.setPos(Vec3.atCenterOf(getBlockPos()));
            newEcho.setEchoId(echo.id());
            newEcho.aiStep();
            echo.model().ifPresent(model -> newEcho.setVillagerData(model.data()));
            level.addFreshEntity(newEcho);
            workerID = newEcho.getUUID();
        });
    }

    public boolean validateEchoEntity(EchoEntity entity) {
        return entity.isAlive()
                && entity.getEchoId().equals(echoId)
                && entity.getUUID().equals(workerID);
    }
}
