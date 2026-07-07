package net.phoenix.chromatic_codes.mixin;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class FTBCompatibilityPlugin implements IMixinConfigPlugin {

    // Exact simple class names of the ModernUI text-pipeline mixins,
    // gated on ModernUI actually being loaded.
    private static final Set<String> MODERNUI_MIXINS = Set.of(
            "MixinTextLayoutProcessor",
            "MixinTextLayout",
            "MixinTextLayoutDraw");

    private static boolean isModLoaded(String modId) {
        try {
            // NeoForge populates ModList very early. If it's initialized, use it directly.
            if (ModList.get() != null) {
                return ModList.get().isLoaded(modId);
            }
            return false;
        } catch (Throwable t) {
            // Fallback safety check if called during extremely early boot stages
            try {
                return net.neoforged.fml.loading.FMLLoader.getLoadingModList()
                        .getMods().stream()
                        .map(ModInfo::getModId)
                        .anyMatch(modId::equals);
            } catch (Throwable e) {
                return false;
            }
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // mixinClassName here is the fully-qualified mixin class, e.g.
        // net.phoenix.chromatic_codes.mixin.MixinTextLayoutProcessor
        String simpleName = mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1);

        if (MODERNUI_MIXINS.contains(simpleName)) {
            return isModLoaded("modernui");
        }
        if (mixinClassName.endsWith("MixinFTBTextUtils")) {
            return isModLoaded("ftbquests");
        }
        if (mixinClassName.endsWith("MixinFTBStringUtils")) {
            return isModLoaded("ftblibrary");
        }
        return true;
    }

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String target, org.objectweb.asm.tree.ClassNode targetClass, String mixin,
                         IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String target, org.objectweb.asm.tree.ClassNode targetClass, String mixin,
                          IMixinInfo mixinInfo) {}
}
