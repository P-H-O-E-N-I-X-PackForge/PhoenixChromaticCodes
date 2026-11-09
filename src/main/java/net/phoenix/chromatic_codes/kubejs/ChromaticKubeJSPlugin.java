package net.phoenix.chromatic_codes.kubejs;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.phoenix.chromatic_codes.api.ScriptedCodes;
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
        // KubeJS 1.20.1 has no "scripts finished loading" hook, only this per-load one (it runs right before the
        // scripts execute). Note a client reload; the tick handler below fires the event once loading is done.
        if (event.getType() == ScriptType.CLIENT) clientLoadGeneration++;
    }

    private static volatile int clientLoadGeneration = 0;
    private static int clientAppliedGeneration = 0;

    @Override
    public void clientInit() {
        MinecraftForge.EVENT_BUS.addListener(ChromaticKubeJSPlugin::onClientTick);
    }

    // Client script loads are synchronous on the main thread, so by the next tick the scripts have all run. Doing
    // the swap (collect new -> rebuild) in one go also means removed definitions disappear without a gap.
    private static void onClientTick(TickEvent.ClientTickEvent tick) {
        if (tick.phase != TickEvent.Phase.END || clientAppliedGeneration == clientLoadGeneration) return;
        clientAppliedGeneration = clientLoadGeneration;

        ChromaticRegisterEventJS clientEvent = new ChromaticRegisterEventJS();
        ChromaticEvents.CLIENT_REGISTER.post(ScriptType.CLIENT, clientEvent);
        ScriptedCodes.applyClient(clientEvent.build());
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
