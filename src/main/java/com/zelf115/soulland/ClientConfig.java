package com.zelf115.soulland;

import com.zelf115.soulland.client.HudAnchor;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Settings that belong to one player's client: how and where their cultivation HUD shows. */
public final class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue HUD_VISIBLE = BUILDER
            .comment("Whether the cultivation HUD panel is shown.")
            .define("hud.visible", true);

    public static final ModConfigSpec.EnumValue<HudAnchor> HUD_ANCHOR = BUILDER
            .comment("Where on the screen the cultivation HUD panel sits.")
            .defineEnum("hud.anchor", HudAnchor.TOP_LEFT);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ClientConfig() {
    }
}
