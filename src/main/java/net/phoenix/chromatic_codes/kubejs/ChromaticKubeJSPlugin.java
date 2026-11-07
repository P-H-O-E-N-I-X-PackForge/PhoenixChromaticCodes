package net.phoenix.chromatic_codes.kubejs;

import net.phoenix.chromatic_codes.network.ChromaticNetwork;

import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.ClassFilter;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.script.ScriptType;

/**
 * Loaded by KubeJS only (see kubejs.plugins.txt), so Chromatic Codes works fine without KubeJS installed.
 * Nothing outside this package may reference these classes.
 */
public class ChromaticKubeJSPlugin implements KubeJSPlugin {

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(ChromaticEvents.GROUP);
    }

    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("Chromatic", ChromaticJS.class);
    }

    @Override
    public void registerClasses(ClassFilter filter) {
        // Raw API access (ChromaticAPI, IChromaticEffect, MovementRegistry...) for startup/client scripts, which run
        // on the side that renders. Server scripts go through ChromaticEvents.register so changes reach clients.
        if (filter.scriptType != ScriptType.SERVER) {
            filter.allow("net.phoenix.chromatic_codes.ChromaticAPI");
            filter.allow("net.phoenix.chromatic_codes.api");
        }
    }

    // Runs after every script (re)load of each manager; we only care about the server one (start and /reload).
    @Override
    public void afterScriptsLoaded(ScriptManager manager) {
        if (manager.scriptType != ScriptType.SERVER) return;

        ChromaticRegisterKubeEvent event = new ChromaticRegisterKubeEvent();
        ChromaticEvents.REGISTER.post(ScriptType.SERVER, event);
        ChromaticNetwork.setServerDefinitions(event.build());
        // Players get the new set from ChromaticNetworkEvents once the reload finishes.
    }
}
