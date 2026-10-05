package dev.ftb.mods.ftbechoes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.VillagerLikeModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.VillagerDataHolderRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.VillagerMetadataSection;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;

// very much like VillagerProfession Layer, but renders layers translucent
public class EchoProfessionLayer<S extends LivingEntityRenderState & VillagerDataHolderRenderState, M extends EntityModel<S> & VillagerLikeModel<S>> extends VillagerProfessionLayer<S, M> {
    private static final Int2ObjectMap<Identifier> LEVEL_LOCATIONS = Util.make(new Int2ObjectOpenHashMap<>(), map -> {
        map.put(1, Identifier.withDefaultNamespace("stone"));
        map.put(2, Identifier.withDefaultNamespace("iron"));
        map.put(3, Identifier.withDefaultNamespace("gold"));
        map.put(4, Identifier.withDefaultNamespace("emerald"));
        map.put(5, Identifier.withDefaultNamespace("diamond"));
    });
    private final Object2ObjectMap<ResourceKey<VillagerType>, VillagerMetadataSection.Hat> typeHatCache = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<ResourceKey<VillagerProfession>, VillagerMetadataSection.Hat> professionHatCache = new Object2ObjectOpenHashMap<>();
    private final String path;
    private final M noHatModel;

    public EchoProfessionLayer(RenderLayerParent<S, M> renderer, ResourceManager resourceManager, String path, M noHatModel, M noHatBabyModel) {
        super(renderer, resourceManager, path, noHatModel, noHatBabyModel);
        this.path = path;
        this.noHatModel = noHatModel;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, S state, float yRot, float xRot) {
        if (!state.isInvisible) {
            VillagerData villagerData = state.getVillagerData();
            if (villagerData != null) {
                Holder<VillagerType> type = villagerData.type();
                Holder<VillagerProfession> profession = villagerData.profession();
                VillagerMetadataSection.Hat typeHat = this.getHatData(this.typeHatCache, "type", type);
                VillagerMetadataSection.Hat professionHat = this.getHatData(this.professionHatCache, "profession", profession);
                M model = this.getParentModel();
                Identifier typeTexture = this.getIdentifier(state.isBaby ? "baby" : "type", type);
                boolean typeHatVisible = professionHat == VillagerMetadataSection.Hat.NONE
                        || professionHat == VillagerMetadataSection.Hat.PARTIAL && typeHat != VillagerMetadataSection.Hat.FULL;
                renderTranslucentColoredCutoutModel(typeHatVisible ? model : noHatModel, typeTexture, poseStack, submitNodeCollector, lightCoords, state, 0xC090FFFF, 1);
                if (!profession.is(VillagerProfession.NONE) && !state.isBaby) {
                    Identifier professionTexture = this.getIdentifier("profession", profession);
                    renderTranslucentColoredCutoutModel(model, professionTexture, poseStack, submitNodeCollector, lightCoords, state, 0x8090FFFF, 2);
                    if (!profession.is(VillagerProfession.NITWIT)) {
                        Identifier professionLevelTexture = this.getIdentifier(
                                "profession_level", LEVEL_LOCATIONS.get(Mth.clamp(villagerData.level(), 1, LEVEL_LOCATIONS.size()))
                        );
                        renderTranslucentColoredCutoutModel(model, professionLevelTexture, poseStack, submitNodeCollector, lightCoords, state, 0x8090FFFF, 3);
                    }
                }
            }
        }
    }

    private Identifier getIdentifier(String type, Identifier key) {
        return key.withPath(keyPath -> "textures/entity/" + this.path + "/" + type + "/" + keyPath + ".png");
    }

    private Identifier getIdentifier(String type, Holder<?> holder) {
        return holder.unwrapKey().map(k -> this.getIdentifier(type, k.identifier())).orElse(MissingTextureAtlasSprite.getLocation());
    }

    private static <S extends LivingEntityRenderState> void renderTranslucentColoredCutoutModel(
            Model<? super S> model,
            Identifier texture,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            S state,
            int color,
            int order
    ) {
        submitNodeCollector.order(order)
                .submitModel(
                        model,
                        state,
                        poseStack,
                        RenderTypes.entityTranslucentCullItemTarget(texture),
                        lightCoords,
                        LivingEntityRenderer.getOverlayCoords(state, 0.0F),
                        color,
                        null,
                        state.outlineColor,
                        null
                );
    }
}
