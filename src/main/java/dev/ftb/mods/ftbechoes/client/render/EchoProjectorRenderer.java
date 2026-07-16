package dev.ftb.mods.ftbechoes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.ftb.mods.ftbechoes.block.entity.EchoProjectorBlockEntity;
import dev.ftb.mods.ftbechoes.echo.Echo;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class EchoProjectorRenderer implements BlockEntityRenderer<EchoProjectorBlockEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/villager/villager.png");
    private final VillagerModel<Villager> model;

    private static final List<ResourceLocation> LEVELS = List.of(
        ResourceLocation.withDefaultNamespace("none"),
        ResourceLocation.withDefaultNamespace("stone"),
        ResourceLocation.withDefaultNamespace("iron"),
        ResourceLocation.withDefaultNamespace("gold"),
        ResourceLocation.withDefaultNamespace("emerald"),
        ResourceLocation.withDefaultNamespace("diamond")
    );

    public EchoProjectorRenderer(BlockEntityRendererProvider.Context ctx) {
        model = new VillagerModel<>(ctx.bakeLayer(ModelLayers.VILLAGER));
    }

    @Override
    public void render(EchoProjectorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (blockEntity.getEchoId() == null) {
            return;
        }

        poseStack.pushPose();

        // necessary transforms to make models render in the right place
        poseStack.translate(0.5, 2.02, 0.5);
        poseStack.scale(1f, -1f, -1f);

        // face the player
        poseStack.mulPose(Axis.YP.rotationDegrees(180 + Minecraft.getInstance().gameRenderer.getMainCamera().getYRot()));

        // actual model rendering work
        VertexConsumer builder = bufferSource.getBuffer(RenderType.entityTranslucentCull(TEXTURE));
        model.renderToBuffer(poseStack, builder, LightTexture.FULL_BRIGHT, packedOverlay, 0x9090FFFF);

        EchoManager.getClientInstance().getEcho(blockEntity.getEchoId()).flatMap(Echo::model).ifPresent(modelInfo -> {
            if (modelInfo.show()) {
                model.hatVisible(modelInfo.showHat());
                ResourceLocation typeTex = textureLoc("type", BuiltInRegistries.VILLAGER_TYPE.getKey(modelInfo.data().getType()));
                renderColoredCutoutModel(model, typeTex, poseStack, bufferSource, LightTexture.FULL_BRIGHT, 0x8090FFFF, true);
                ResourceLocation profTex = textureLoc("profession", BuiltInRegistries.VILLAGER_PROFESSION.getKey(modelInfo.data().getProfession()));
                renderColoredCutoutModel(model, profTex, poseStack, bufferSource, LightTexture.FULL_BRIGHT, 0x9090FFFF, false);
                int level = modelInfo.data().getLevel();
                if (level >= 1 && level <= 5) {
                    ResourceLocation levelTex = this.textureLoc("profession_level", LEVELS.get(level));
                    renderColoredCutoutModel(model, levelTex, poseStack, bufferSource, LightTexture.FULL_BRIGHT, 0x8090FFFF, true);
                }
            }
        });

        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(EchoProjectorBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).setMaxY(blockEntity.getBlockPos().getY() + 3);
    }

    private ResourceLocation textureLoc(String folder, ResourceLocation location) {
        return location.withPath(s -> "textures/entity/villager/" + folder + "/" + s + ".png");
    }

    private static <T extends LivingEntity> void renderColoredCutoutModel(EntityModel<T> model, ResourceLocation textureLocation, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int color, boolean translucent) {
        VertexConsumer vc = buffer.getBuffer(translucent ? RenderType.entityTranslucent(textureLocation) : RenderType.entityCutoutNoCull(textureLocation));
        model.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY, color);
    }
}
