package com.zelf115.soulland.cultivation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

/**
 * Sends a player their own cultivation record, and nobody else's, so the cultivation screens can
 * read it. The whole record goes every time, in the same NBT form it is saved in.
 */
public final class CultivationDataSync implements AttachmentSyncHandler<CultivationData> {

    @Override
    public boolean sendToPlayer(final IAttachmentHolder holder, final ServerPlayer to) {
        return holder == to;
    }

    @Override
    public void write(final RegistryFriendlyByteBuf buf, final CultivationData data, final boolean initialSync) {
        ByteBufCodecs.COMPOUND_TAG.encode(buf, data.serializeNBT(buf.registryAccess()));
    }

    /** Always a fresh record: reading into the old one would keep fields the new NBT leaves out, such as a cleared soul. */
    @Override
    public CultivationData read(final IAttachmentHolder holder, final RegistryFriendlyByteBuf buf,
                                @Nullable final CultivationData previousValue) {
        final CompoundTag tag = ByteBufCodecs.COMPOUND_TAG.decode(buf);
        final CultivationData data = new CultivationData();
        data.deserializeNBT(buf.registryAccess(), tag);
        return data;
    }
}
