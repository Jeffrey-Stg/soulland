package com.zelf115.soulland.client;

import com.zelf115.soulland.SoulLand;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

/** Finds the mod's entity skins, so a skin that hasn't been drawn yet falls back to a vanilla one. */
final class EntitySkins {
    private static final String SKIN_FOLDER = "textures/entity/";
    private static final String SKIN_EXTENSION = ".png";

    private final ResourceManager resources;

    EntitySkins(final ResourceManager resources) {
        this.resources = resources;
    }

    ResourceLocation skinOrFallback(final String path, final ResourceLocation fallback) {
        final ResourceLocation skin = skinAt(path);
        return exists(skin) ? skin : fallback;
    }

    /** Every numbered skin in the folder, counting up from 0 until the first one missing. */
    List<ResourceLocation> numberedSkins(final String folder) {
        final List<ResourceLocation> skins = new ArrayList<>();
        ResourceLocation next = skinAt(folder + "/0");
        while (exists(next)) {
            skins.add(next);
            next = skinAt(folder + "/" + skins.size());
        }
        return skins;
    }

    private static ResourceLocation skinAt(final String path) {
        return ResourceLocation.fromNamespaceAndPath(SoulLand.MODID, SKIN_FOLDER + path + SKIN_EXTENSION);
    }

    private boolean exists(final ResourceLocation skin) {
        return resources.getResource(skin).isPresent();
    }
}
