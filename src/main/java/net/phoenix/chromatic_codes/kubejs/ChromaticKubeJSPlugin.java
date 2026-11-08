package net.phoenix.chromatic_codes.kubejs;

import net.phoenix.chromatic_codes.network.ChromaticNetwork;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ClassFilter;

/**
 * Loaded by KubeJS only (see kubejs.plugins.txt), so Chromatic Codes works fine without KubeJS installed.
 * Nothing outside this package may reference these classes.
 */
public class ChromaticKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void registerEvents() {
        ChromaticEvents.GROUP.register();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("Chromatic", ChromaticJS.class);
    }

    @Override
    public void registerClasses(ScriptType type, ClassFilter filter) {
        // Raw API access (ChromaticAPI, IChromaticEffect, MovementRegistry...) for startup/client scripts, which run
        // on the side that renders. Server scripts go through ChromaticEvents.register so changes reach clients.
        if (type != ScriptType.SERVER) {
            filter.allow("net.phoenix.chromatic_codes.ChromaticAPI");
            filter.allow("net.phoenix.chromatic_codes.api");
        }
    }

    // Runs inside every server script (re)load, after the scripts' own top level has executed.
    @Override
    public void onServerReload() {
        ChromaticRegisterEventJS event = new ChromaticRegisterEventJS();
        ChromaticEvents.REGISTER.post(ScriptType.SERVER, event);
        ChromaticNetwork.setServerDefinitions(event.build());
        // Players get the new set from ChromaticNetworkEvents once the reload finishes.
    }
}
