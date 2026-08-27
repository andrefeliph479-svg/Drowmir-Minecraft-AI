package dev.drowmir.minecraftai;

import com.mojang.logging.LogUtils;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

@Mod(DrowmirMinecraftAI.MOD_ID)
public final class DrowmirMinecraftAI {
    public static final String MOD_ID = "drowmir_ai";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DrowmirMinecraftAI(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Drowmir Minecraft AI carregado.");
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
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
                        .then(Commands.literal("status")
                                .executes(context -> {
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Drowmir v0.0.1-alpha | cérebro básico: online | movimento: em desenvolvimento"),
                                            false
                                    );
                                    return 1;
                                }))
                        .then(Commands.literal("ajuda")
                                .executes(context -> {
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Comandos atuais: /drowmir, /drowmir status, /drowmir ajuda"),
                                            false
                                    );
                                    return 1;
                                }))
        );
    }
}
