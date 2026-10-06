package com.zelf115.soulland.client.screen;

import com.zelf115.soulland.cultivation.CultivationAttachment;
import com.zelf115.soulland.cultivation.CultivationData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/** The local player and the copy of their cultivation record the server keeps synced to them. */
public final class ClientCultivation {

    private ClientCultivation() {
    }

    public static LocalPlayer player() {
        return Minecraft.getInstance().player;
    }

    public static CultivationData data() {
        return player().getData(CultivationAttachment.CULTIVATION_DATA.get());
    }
}
