package com.zelf115.soulland.qi;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;

/** Reports the qi held by the chunk the command source stands in. */
public final class QiCommand {

    private static final String COMMAND_NAME = "qi";
    private static final String RESULT_KEY = "commands.soulland.qi";
    private static final String UNKNOWN_BIOME_KEY = "commands.soulland.qi.unknown_biome";

    private QiCommand() {
    }

    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(COMMAND_NAME).executes(QiCommand::reportQiHere));
    }

    private static int reportQiHere(final CommandContext<CommandSourceStack> context) {
        final CommandSourceStack source = context.getSource();
        final ServerLevel level = source.getLevel();
        final BlockPos position = BlockPos.containing(source.getPosition());
        final Holder<Biome> biome = level.getBiome(position);
        final ResourceKey<Biome> key = biome.unwrapKey().orElse(null);
        if (key == null) {
            source.sendFailure(Component.translatable(UNKNOWN_BIOME_KEY));
            return 0;
        }

        final int qi = QiManager.getQiAt(level, position);
        source.sendSuccess(() -> Component.translatable(RESULT_KEY, biomeName(key.location()), qi), false);
        return qi;
    }

    private static Component biomeName(final ResourceLocation biomeId) {
        return Component.translatable("biome." + biomeId.getNamespace() + "." + biomeId.getPath());
    }
}
