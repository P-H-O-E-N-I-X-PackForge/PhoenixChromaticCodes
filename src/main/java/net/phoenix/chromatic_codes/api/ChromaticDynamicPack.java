package net.phoenix.chromatic_codes.api;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import net.phoenix.chromatic_codes.ChromaticAPI;
import net.phoenix.chromatic_codes.PhoenixChromaticCodes;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

/**
 * An in-memory PackResources that generates font definition JSONs at runtime
 * for every chromatic effect registered in ChromaticAPI.
 */
public class ChromaticDynamicPack implements PackResources {

    public static final ChromaticDynamicPack INSTANCE = new ChromaticDynamicPack();

    private static final String PACK_ID = PhoenixChromaticCodes.MOD_ID + "_dynamic_fonts";
    private static final String NAMESPACE = PhoenixChromaticCodes.MOD_ID;

    // FIX: Updated pack_format to 34 (Minecraft 1.21.1 standard specification)
    // and wrapped it with the new Optional bounding system configuration.
    private static final PackMetadataSection METADATA = new PackMetadataSection(
            Component.literal("Phoenix Chromatic Codes dynamic fonts"),
            34,
            Optional.empty() // no explicit upper bounds capping necessary
    );

    private static final byte[] FONT_JSON = "{\"providers\":[{\"type\":\"reference\",\"id\":\"minecraft:default\"}]}"
            .getBytes(StandardCharsets.UTF_8);

    private ChromaticDynamicPack() {}

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) {
        if ("pack".equals(serializer.getMetadataSectionName())) {
            return (T) METADATA;
        }
        return null;
    }

    @Override
    @Nullable
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type != PackType.CLIENT_RESOURCES) return null;
        if (!location.getNamespace().equals(NAMESPACE)) return null;
        if (!location.getPath().startsWith("font/")) return null;

        for (ResourceLocation fontId : ChromaticAPI.getRegisteredFonts()) {
            if (!fontId.getNamespace().equals(NAMESPACE)) continue;
            if (location.getPath().equals("font/" + fontId.getPath() + ".json")) {
                return () -> new ByteArrayInputStream(FONT_JSON);
            }
        }
        return null;
    }

    @Override
    @Nullable
    public IoSupplier<InputStream> getRootResource(String... elements) {
        return null;
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput resourceOutput) {
        if (type != PackType.CLIENT_RESOURCES) return;
        if (!namespace.equals(NAMESPACE)) return;

        for (ResourceLocation fontId : ChromaticAPI.getRegisteredFonts()) {
            if (!fontId.getNamespace().equals(NAMESPACE)) continue;

            String resourcePath = "font/" + fontId.getPath() + ".json";
            if (!resourcePath.startsWith(path)) continue;

            // FIX: Replaced old 'new ResourceLocation' syntax with 1.21 factory call
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(NAMESPACE, resourcePath);
            resourceOutput.accept(loc, () -> new ByteArrayInputStream(FONT_JSON));
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        if (type != PackType.CLIENT_RESOURCES) return Set.of();
        return Set.of(NAMESPACE);
    }

    @Override
    public String packId() {
        return PACK_ID;
    }

    // FIX: Implement the required location() contract for 1.21.1 PackResources
    @Override
    public net.minecraft.server.packs.PackLocationInfo location() {
        return new net.minecraft.server.packs.PackLocationInfo(
                this.packId(),
                net.minecraft.network.chat.Component.literal("Chromatic Dynamic Fonts"),
                net.minecraft.server.packs.repository.PackSource.BUILT_IN,
                java.util.Optional.empty());
    }

    @Override
    public void close() {
        // Nothing to close
    }
}
