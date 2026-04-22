package net.phoenix.chromatic_codes.mixin;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class FTBCompatibilityPlugin implements IMixinConfigPlugin {

    private static boolean isModLoaded(String modId) {
        try {
            if (ModList.get() == null) {
                return LoadingModList.get().getMods().stream()
                        .map(ModInfo::getModId)
                        .anyMatch(modId::equals);
            } else {
                return ModList.get().isLoaded(modId);
            }
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
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
