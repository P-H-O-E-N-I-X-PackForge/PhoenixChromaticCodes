package net.phoenix.chromatic_codes.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface ChromaticEvents {

    EventGroup GROUP = EventGroup.of("ChromaticEvents");

    /** Server script event, fired on server start and on every {@code /reload}. */
    EventHandler REGISTER = GROUP.server("register", () -> ChromaticRegisterEventJS.class);

    /**
     * Client script event with the same methods as {@link #REGISTER}. Fired after client scripts (re)load (startup,
     * F3+T, /kubejs reload client_scripts); definitions apply on this client only and are rebuilt on each reload.
     */
    EventHandler CLIENT_REGISTER = GROUP.client("clientRegister", () -> ChromaticRegisterEventJS.class);
}
