package net.phoenix.chromatic_codes.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.phoenix.chromatic_codes.PhoenixChromaticCodes;
import net.phoenix.chromatic_codes.api.ScriptedCodes;
import net.phoenix.chromatic_codes.api.ScriptedCodes.ColorDef;
import net.phoenix.chromatic_codes.api.ScriptedCodes.Definitions;
import net.phoenix.chromatic_codes.api.ScriptedCodes.Fx;
import net.phoenix.chromatic_codes.api.ScriptedCodes.OutlineDef;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Ships script-defined codes (see {@link ScriptedCodes}) from the logical server to clients. The channel accepts any
 * remote version / absence so it never blocks a connection; {@link #sendTo} skips clients that don't have it.
 */
public final class ChromaticNetwork {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            PhoenixChromaticCodes.id("sync"), () -> PROTOCOL, v -> true, v -> true);

    // What the logical server currently wants every client to have. Replaced on each script reload.
    private static volatile Definitions serverDefinitions = Definitions.EMPTY;

    private ChromaticNetwork() {}

    public static void init() {
        CHANNEL.registerMessage(0, SyncPacket.class, SyncPacket::encode, SyncPacket::decode, SyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void setServerDefinitions(Definitions defs) {
        serverDefinitions = defs == null ? Definitions.EMPTY : defs;
    }

    public static void sendTo(ServerPlayer player) {
        if (!CHANNEL.isRemotePresent(player.connection.connection)) return;
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncPacket(serverDefinitions));
    }

    public static final class SyncPacket {

        // Decode-side sanity caps; a malicious server shouldn't be able to make a client allocate wildly.
        private static final int MAX_ENTRIES = 4096;
        private static final int MAX_COLORS = 64;

        private final Definitions defs;

        public SyncPacket(Definitions defs) {
            this.defs = defs;
        }

        public static void encode(SyncPacket p, FriendlyByteBuf buf) {
            Definitions d = p.defs;

            buf.writeVarInt(d.effects().size());
            for (Fx fx : d.effects()) {
                buf.writeUtf(fx.key());
                buf.writeBoolean(fx.named());
                buf.writeUtf(fx.movement());
                buf.writeFloat(fx.colorSpeed());
                buf.writeBoolean(fx.moveSpeed() != null);
                buf.writeFloat(fx.moveSpeed() != null ? fx.moveSpeed() : 0f);
                buf.writeVarInt(fx.colors().size());
                for (int c : fx.colors()) buf.writeInt(c);
                buf.writeBoolean(fx.font() != null);
                buf.writeUtf(fx.font() != null ? fx.font() : "");
            }

            buf.writeVarInt(d.colors().size());
            for (ColorDef c : d.colors()) {
                buf.writeUtf(c.key());
                buf.writeBoolean(c.named());
                buf.writeInt(c.rgb());
            }

            buf.writeVarInt(d.outlines().size());
            for (OutlineDef o : d.outlines()) {
                buf.writeUtf(o.key());
                buf.writeBoolean(o.named());
            }

            buf.writeBoolean(d.outlineColor() != null);
            buf.writeInt(d.outlineColor() != null ? d.outlineColor() : 0);
        }

        public static SyncPacket decode(FriendlyByteBuf buf) {
            int fxCount = Math.min(buf.readVarInt(), MAX_ENTRIES);
            List<Fx> effects = new ArrayList<>();
            for (int i = 0; i < fxCount; i++) {
                String key = buf.readUtf(256);
                boolean named = buf.readBoolean();
                String movement = buf.readUtf(64);
                float colorSpeed = buf.readFloat();
                boolean hasMove = buf.readBoolean();
                float move = buf.readFloat();
                int n = Math.min(buf.readVarInt(), MAX_COLORS);
                List<Integer> colors = new ArrayList<>(n);
                for (int j = 0; j < n; j++) colors.add(buf.readInt());
                boolean hasFont = buf.readBoolean();
                String font = buf.readUtf(64);
                effects.add(
                        new Fx(key, named, movement, colorSpeed, hasMove ? move : null, colors, hasFont ? font : null));
            }

            int colorCount = Math.min(buf.readVarInt(), MAX_ENTRIES);
            List<ColorDef> colors = new ArrayList<>();
            for (int i = 0; i < colorCount; i++) {
                colors.add(new ColorDef(buf.readUtf(256), buf.readBoolean(), buf.readInt()));
            }

            int outlineCount = Math.min(buf.readVarInt(), MAX_ENTRIES);
            List<OutlineDef> outlines = new ArrayList<>();
            for (int i = 0; i < outlineCount; i++) {
                outlines.add(new OutlineDef(buf.readUtf(256), buf.readBoolean()));
            }

            boolean hasOutlineColor = buf.readBoolean();
            int outlineColor = buf.readInt();

            return new SyncPacket(new Definitions(effects, colors, outlines, hasOutlineColor ? outlineColor : null));
        }

        public static void handle(SyncPacket p, Supplier<NetworkEvent.Context> ctx) {
            NetworkEvent.Context c = ctx.get();
            c.enqueueWork(() -> ScriptedCodes.apply(p.defs));
            c.setPacketHandled(true);
        }
    }
}
