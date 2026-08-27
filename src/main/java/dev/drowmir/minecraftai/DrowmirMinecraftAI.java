package dev.drowmir.minecraftai;

import com.mojang.logging.LogUtils;
import java.util.UUID;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

@Mod(DrowmirMinecraftAI.MOD_ID)
public final class DrowmirMinecraftAI {
    public static final String MOD_ID = "drowmir_ai";
    public static final Logger LOGGER = LogUtils.getLogger();

    private UUID companionId;
    private UUID ownerId;
    private boolean following;

    public DrowmirMinecraftAI(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Drowmir Minecraft AI carregado.");
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("drowmir")
                        .executes(context -> {
                            context.getSource().sendSuccess(
                                    () -> Component.literal("Drowmir: tô vivo, porra. A fundação da IA carregou certinho KKKK"),
                                    false
                            );
                            return 1;
                        })
                        .then(Commands.literal("spawn")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ServerLevel level = player.serverLevel();

                                    WanderingTrader oldCompanion = findCompanion(context.getSource().getServer());
                                    if (oldCompanion != null) {
                                        oldCompanion.discard();
                                    }

                                    WanderingTrader companion = EntityType.WANDERING_TRADER.create(level);
                                    if (companion == null) {
                                        context.getSource().sendFailure(Component.literal("Drowmir: não consegui criar meu corpo. Aí é foda KKKK"));
                                        return 0;
                                    }

                                    companion.setPos(player.getX() + 2.0D, player.getY(), player.getZ() + 1.0D);
                                    companion.setCustomName(Component.literal("Drowmir"));
                                    companion.setCustomNameVisible(true);
                                    companion.setPersistenceRequired();
                                    level.addFreshEntity(companion);

                                    companionId = companion.getUUID();
                                    ownerId = player.getUUID();
                                    following = false;

                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Drowmir: beleza, agora eu tenho um corpo provisório KKKKK"),
                                            false
                                    );
                                    return 1;
                                }))
                        .then(Commands.literal("seguir")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    WanderingTrader companion = findCompanion(context.getSource().getServer());
                                    if (companion == null) {
                                        context.getSource().sendFailure(Component.literal("Drowmir: primeiro usa /drowmir spawn, animal KKKK"));
                                        return 0;
                                    }

                                    ownerId = player.getUUID();
                                    following = true;
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Drowmir: fechou, vou contigo."),
                                            false
                                    );
                                    return 1;
                                }))
                        .then(Commands.literal("parar")
                                .executes(context -> {
                                    WanderingTrader companion = findCompanion(context.getSource().getServer());
                                    following = false;
                                    if (companion != null) {
                                        companion.getNavigation().stop();
                                    }
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Drowmir: parei."),
                                            false
                                    );
                                    return 1;
                                }))
                        .then(Commands.literal("vem")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    WanderingTrader companion = findCompanion(context.getSource().getServer());
                                    if (companion == null) {
                                        context.getSource().sendFailure(Component.literal("Drowmir: eu nem tenho corpo ainda KKKK usa /drowmir spawn"));
                                        return 0;
                                    }

                                    ownerId = player.getUUID();
                                    companion.getNavigation().moveTo(player, 1.15D);
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Drowmir: tô indo."),
                                            false
                                    );
                                    return 1;
                                }))
                        .then(Commands.literal("status")
                                .executes(context -> {
                                    WanderingTrader companion = findCompanion(context.getSource().getServer());
                                    String body = companion == null ? "sem corpo" : "corpo provisório online";
                                    String movement = following ? "seguindo você" : "parado/livre";
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Drowmir v0.0.1-alpha | " + body + " | " + movement),
                                            false
                                    );
                                    return 1;
                                }))
                        .then(Commands.literal("ajuda")
                                .executes(context -> {
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Comandos: /drowmir spawn, /drowmir seguir, /drowmir parar, /drowmir vem, /drowmir status"),
                                            false
                                    );
                                    return 1;
                                }))
        );
    }

    private void onServerTick(ServerTickEvent.Post event) {
        if (!following || companionId == null || ownerId == null) {
            return;
        }

        MinecraftServer server = event.getServer();
        ServerPlayer owner = server.getPlayerList().getPlayer(ownerId);
        WanderingTrader companion = findCompanion(server);

        if (owner == null || companion == null || owner.level() != companion.level()) {
            return;
        }

        double distanceSquared = companion.distanceToSqr(owner);
        if (distanceSquared > 12.25D) {
            companion.getNavigation().moveTo(owner, 1.15D);
        } else if (distanceSquared < 4.0D) {
            companion.getNavigation().stop();
        }
    }

    private WanderingTrader findCompanion(MinecraftServer server) {
        if (companionId == null) {
            return null;
        }

        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(companionId);
            if (entity instanceof WanderingTrader trader) {
                return trader;
            }
        }
        return null;
    }
}
