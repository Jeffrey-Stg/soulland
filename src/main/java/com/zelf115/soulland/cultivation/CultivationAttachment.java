package com.zelf115.soulland.cultivation;

import com.zelf115.soulland.SoulLand;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Registers the NeoForge attachment type used to persist cultivation data on players.
 */
public class CultivationAttachment {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, SoulLand.MODID);

    /**
     * The per-player cultivation data attachment. Serialized to NBT so it survives logout/login.
     */
    public static final Supplier<AttachmentType<CultivationData>> CULTIVATION_DATA =
            ATTACHMENT_TYPES.register("cultivation_data",
                    () -> AttachmentType.serializable(CultivationData::new).build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
