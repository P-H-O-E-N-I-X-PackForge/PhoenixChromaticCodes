package net.phoenix.chromatic_codes.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface ChromaticEvents {

    EventGroup GROUP = EventGroup.of("ChromaticEvents");

    /** Server script event, fired on server start and on every {@code /reload}. */
    EventHandler REGISTER = GROUP.server("register", () -> ChromaticRegisterKubeEvent.class);
}
