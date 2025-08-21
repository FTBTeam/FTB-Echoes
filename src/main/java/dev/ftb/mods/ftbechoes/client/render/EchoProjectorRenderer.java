package dev.ftb.mods.ftbechoes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.ftb.mods.ftbechoes.block.entity.EchoProjectorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;

public class EchoProjectorRenderer implements BlockEntityRenderer<EchoProjectorBlockEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/villager/villager.png");
    private final VillagerModel<Villager> model;

    public EchoProjectorRenderer(BlockEntityRendererProvider.Context ctx) {
        model = new VillagerModel<>(ctx.bakeLayer(ModelLayers.VILLAGER));
    }

    @Override
    public void render(EchoProjectorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        // necessary transforms to make models render in the right place
        poseStack.translate(0.5, 2.02, 0.5);
        poseStack.scale(1f, -1f, -1f);

        // face the player
        poseStack.mulPose(Axis.YP.rotationDegrees(180 + Minecraft.getInstance().gameRenderer.getMainCamera().getYRot()));

        // actual model rendering work
        renderModel(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private void renderModel(EchoProjectorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        VertexConsumer builder = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        model.renderToBuffer(poseStack, builder, packedLight, packedOverlay, 0x9090FFFF);
    }
}
