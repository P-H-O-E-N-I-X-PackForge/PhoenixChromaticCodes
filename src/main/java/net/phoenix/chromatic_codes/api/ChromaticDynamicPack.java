package net.phoenix.chromatic_codes.api;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import net.phoenix.ChromaticAPI;
import net.phoenix.chromatic_codes.PhoenixChromaticCodes;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * An in-memory PackResources that generates font definition JSONs at runtime
 * for every chromatic effect registered in ChromaticAPI.
 *
 * IMPORTANT: Pack.readMetaAndCreate calls getMetadataSection(PackMetadataSection.SERIALIZER)
 * to read pack format and description. We must return a real PackMetadataSection here,
 * NOT rely on getRootResource("pack.mcmeta") — that path is not used by readMetaAndCreate.
 */
public class ChromaticDynamicPack implements PackResources {

    public static final ChromaticDynamicPack INSTANCE = new ChromaticDynamicPack();

    private static final String PACK_ID = PhoenixChromaticCodes.MOD_ID + "_dynamic_fonts";
    private static final String NAMESPACE = PhoenixChromaticCodes.MOD_ID;

    // pack_format 15 = Minecraft 1.20 / 1.20.1
    private static final PackMetadataSection METADATA = new PackMetadataSection(
            Component.literal("Phoenix Chromatic Codes dynamic fonts"), 15);

    // Font JSON: delegate all providers to minecraft:default.
    // MixinStringRenderOutput applies the actual color/offset effects at render time.
    private static final byte[] FONT_JSON = "{\"providers\":[{\"type\":\"reference\",\"id\":\"minecraft:default\"}]}"
            .getBytes(StandardCharsets.UTF_8);

    private ChromaticDynamicPack() {}

    /**
     * getMetadataSection is what Pack.readMetaAndCreate actually calls.
     * Return our PackMetadataSection when asked; null for anything else.
     */
    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) {
        // "pack" is the section name PackMetadataSection uses — matches what
        // Pack.readMetaAndCreate requests, without needing the SERIALIZER constant
        // which was renamed between versions.
        if ("pack".equals(serializer.getMetadataSectionName())) {
            return (T) METADATA;
        }
        return null;
    }

    /**
     * getResource returns a nullable IoSupplier<InputStream> in 1.20.1.
     * null = resource not found here.
     */
    @Override
    @Nullable
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type != PackType.CLIENT_RESOURCES) return null;
        if (!location.getNamespace().equals(NAMESPACE)) return null;
        if (!location.getPath().startsWith("font/")) return null;

        // location.getPath() == "font/code_94.json" for code '^' (codepoint 94)
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
        // Not used by Forge 1.20.1 pack loading — metadata is via getMetadataSection.
        return null;
    }

    /**
     * listResources is called during resource discovery.
     * Emit one entry per registered font.
     */
    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput resourceOutput) {
        if (type != PackType.CLIENT_RESOURCES) return;
        if (!namespace.equals(NAMESPACE)) return;

        for (ResourceLocation fontId : ChromaticAPI.getRegisteredFonts()) {
            if (!fontId.getNamespace().equals(NAMESPACE)) continue;

            String resourcePath = "font/" + fontId.getPath() + ".json";
            if (!resourcePath.startsWith(path)) continue;

            ResourceLocation loc = new ResourceLocation(NAMESPACE, resourcePath);
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

    @Override
    public void close() {
        // Nothing to close
    }
}
