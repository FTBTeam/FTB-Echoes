package dev.ftb.mods.ftbechoes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.npc.BabyVillagerModel;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.villager.Villager;
import org.jspecify.annotations.Nullable;

public class EchoEntityRenderer extends VillagerRenderer {
    public EchoEntityRenderer(EntityRendererProvider.Context context) {
        super(context);

        layers.removeIf(l -> l instanceof VillagerProfessionLayer<VillagerRenderState, VillagerModel>);
        addLayer(new EchoProfessionLayer<>(this,
                context.getResourceManager(),
                "villager",
                new VillagerModel(context.bakeLayer(ModelLayers.VILLAGER_NO_HAT)),
                new BabyVillagerModel(context.bakeLayer(ModelLayers.VILLAGER_BABY_NO_HAT))));
    }

    @Override
    public VillagerRenderState createRenderState() {
        return new EchoRenderState();
    }

    @Override
    public void extractRenderState(Villager entity, VillagerRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
    }

    @Override
    public void submit(VillagerRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();

        // always face the player
        double angle = Mth.atan2(state.x - camera.pos.x, state.z - camera.pos.z);
        poseStack.mulPose(Axis.YP.rotation((float) angle + Mth.PI));

        super.submit(state, poseStack, submitNodeCollector, camera);

        poseStack.popPose();
    }

    @Override
    protected @Nullable RenderType getRenderType(VillagerRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
        return RenderTypes.entityTranslucentCullItemTarget(getTextureLocation(state));
    }

    @Override
    protected int getModelTint(VillagerRenderState state) {
        return 0x8090FFFF;
    }
}
