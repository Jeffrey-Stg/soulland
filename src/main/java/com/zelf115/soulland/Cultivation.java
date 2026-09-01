package com.zelf115.soulland;

import com.zelf115.soulland.commands.CultivationCommands;
import com.zelf115.soulland.cultivation.CultivationAttachment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

public final class Cultivation {
    public static final String LAST_MEDITATION_TICK_KEY = "soulland_last_meditation_tick";
    public static final String LAST_SPIRIT_TICK_KEY = "soulland_last_spirit_tick";
    public static final String PENDING_SPECIAL_BREAKTHROUGH_KEY = "soulland_pending_special_bt";
    public static final String SPECIAL_BREAKTHROUGH_STRIKE_TICK_KEY = "soulland_special_bt_strike_tick";

    private Cultivation() {
    }

    public static void register(IEventBus modEventBus) {
        CultivationAttachment.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(CultivationCommands::register);
    }
}
