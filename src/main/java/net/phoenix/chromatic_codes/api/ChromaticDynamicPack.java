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
import java.util.Set;

public class ChromaticDynamicPack implements PackResources {

    public static final ChromaticDynamicPack INSTANCE = new ChromaticDynamicPack();

    private static final String PACK_ID = PhoenixChromaticCodes.MOD_ID + "_dynamic_fonts";
    private static final String NAMESPACE = PhoenixChromaticCodes.MOD_ID;

    private static final PackMetadataSection METADATA = new PackMetadataSection(
            Component.literal("Phoenix Chromatic Codes dynamic fonts"), 15);

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
    public void close() {}
}
