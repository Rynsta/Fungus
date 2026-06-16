package io.github.ryn.fungus;

import io.github.ryn.fungus.config.ConfigManager;
import io.github.ryn.fungus.gui.BlockHighlightScreen;
import io.github.ryn.fungus.gui.FungusScreen;
import io.github.ryn.fungus.gui.ViewmodelScreen;
import io.github.ryn.fungus.renderer.BlockHighlightRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.concurrent.atomic.AtomicReference;

public class FungusClient implements ClientModInitializer {
    private static final AtomicReference<java.util.function.Function<Screen, Screen>> PENDING_SCREEN =
            new AtomicReference<>(null);

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        BlockHighlightRenderer.register();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("fungus")
                    .executes(ctx -> {
                        PENDING_SCREEN.set(parent -> new FungusScreen(parent));
                        return 1;
                    }));
            dispatcher.register(ClientCommands.literal("viewmodel")
                    .executes(ctx -> {
                        PENDING_SCREEN.set(parent -> new ViewmodelScreen(parent));
                        return 1;
                    }));
            dispatcher.register(ClientCommands.literal("blockhighlight")
                    .executes(ctx -> {
                        PENDING_SCREEN.set(parent -> new BlockHighlightScreen(parent));
                        return 1;
                    }));
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            java.util.function.Function<Screen, Screen> factory = PENDING_SCREEN.getAndSet(null);
            if (factory != null) {
                Minecraft.getInstance().setScreen(factory.apply(null));
            }
        });

        Fungus.LOG.info("[Fungus] client init — /fungus, /viewmodel, /blockhighlight registered");
    }
}
