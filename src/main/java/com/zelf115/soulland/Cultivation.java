package com.zelf115.soulland;

import com.zelf115.soulland.commands.CultivationCommands;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

public final class Cultivation {

    private Cultivation() {
    }

    public static void register(IEventBus modEventBus) {
        CultivationAttachment.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(CultivationCommands::register);
    }
}
