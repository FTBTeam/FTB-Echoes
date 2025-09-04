package dev.ftb.mods.ftbechoes.echo.progress;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbechoes.GameStageHelper;
import dev.ftb.mods.ftbechoes.echo.Echo;
import dev.ftb.mods.ftbechoes.echo.EchoManager;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Progress for a given team. The record members are mutable internally, but only modified
 * by mutator methods called via {@link TeamProgressManager}.
 *
 * @param perEcho progress on an echo, by echo ID
 */
public record TeamProgress(Map<ResourceLocation, PerEchoProgress> perEcho) {
    public static final TeamProgress NONE = new TeamProgress(Map.of());

    private static final Codec<Map<ResourceLocation,PerEchoProgress>> ECHO_STAGE
            = Codec.unboundedMap(ResourceLocation.CODEC, PerEchoProgress.CODEC).xmap(HashMap::new, Map::copyOf);

    public static final Codec<TeamProgress> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ECHO_STAGE.fieldOf("per_echo").forGetter(p -> p.perEcho)
    ).apply(builder, TeamProgress::new));
    public static final StreamCodec<FriendlyByteBuf, TeamProgress> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, PerEchoProgress.STREAM_CODEC), TeamProgress::perEcho,
            TeamProgress::new
    );

    public static TeamProgress createNew() {
        return new TeamProgress(new HashMap<>());
    }

    public TeamProgress forSyncTo(ServerPlayer player) {
        UUID playerId = player.getUUID();
        Map<ResourceLocation, PerEchoProgress> map = new HashMap<>();
        perEcho.forEach((id, rec) -> {
            Map<UUID,Set<Integer>> rewards = Map.of(playerId, rec.claimedRewards().getOrDefault(playerId, Set.of()));
            map.put(id, new PerEchoProgress(rec.currentStage(), rewards));
        });

        return new TeamProgress(map);
    }

    @Override
    public Map<ResourceLocation, PerEchoProgress> perEcho() {
        return Collections.unmodifiableMap(perEcho);
    }

    public boolean isEmpty() {
        return perEcho.isEmpty();
    }

    public boolean isRewardClaimed(ResourceLocation echoId, Player player, int stage) throws IllegalArgumentException {
        return getPerEchoProgress(echoId).isRewardClaimed(player, stage);
    }

    public int getCurrentStage(ResourceLocation echoId) throws IllegalArgumentException {
        return getPerEchoProgress(echoId).getCurrentStage();
    }

    public boolean isStageAvailable(ResourceLocation id, int stageIdx) {
        return stageIdx >= getCurrentStage(id);
    }

    boolean claimReward(ResourceLocation echoId, Player player, int stage) {
        return getPerEchoProgress(echoId).setRewardClaimed(player, stage);
    }

    boolean completeStage(ResourceLocation echoId) {
        PerEchoProgress per = getPerEchoProgress(echoId);
        var echo = EchoManager.getServerInstance().getEcho(echoId).orElseThrow();
        if (per.getCurrentStage() < echo.stages().size()) {
            per.completeStage();
            return true;
        }
        return false;
    }

    public void advance(ServerPlayer sp, Team team, Echo echo) {
        final int currentStage = getCurrentStage(echo.id());

        if (currentStage >= 0 && currentStage < echo.stages().size()) {
            var stage = echo.stages().get(currentStage);
            if (GameStageHelper.hasStage(sp, stage.requiredGameStage()) && TeamProgressManager.get().completeStage(team, echo.id())) {
                team.getOnlineMembers().forEach(member -> {
                    Vec3 vec = member.position();
                    if (currentStage == echo.stages().size()) {
                        // TODO completed all stages, some special reward here?
                        member.displayClientMessage(Component.literal("All stages completed!").withStyle(ChatFormatting.LIGHT_PURPLE), false);
                        member.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE), SoundSource.PLAYERS, vec.x, vec.y, vec.z, 1f, 1f, 0L));
                    } else {
                        member.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.PLAYER_LEVELUP), SoundSource.PLAYERS, vec.x, vec.y, vec.z, 1f, 1f, 0L));
                    }
                });
            }
        }
    }

    public Boolean setStage(ResourceLocation echoId, int stage) {
        PerEchoProgress per = getPerEchoProgress(echoId);
        var echo = EchoManager.getServerInstance().getEcho(echoId).orElseThrow();
        per.setStage(Mth.clamp(stage, 0, echo.stages().size() - 1));
        return true;
    }

    @NotNull
    private PerEchoProgress getPerEchoProgress(ResourceLocation echoId) throws IllegalArgumentException {
        return perEcho.computeIfAbsent(echoId, k -> PerEchoProgress.empty());
    }
}
