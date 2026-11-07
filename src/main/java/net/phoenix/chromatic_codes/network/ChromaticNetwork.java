package net.phoenix.chromatic_codes.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.phoenix.chromatic_codes.PhoenixChromaticCodes;
import net.phoenix.chromatic_codes.api.ScriptedCodes;
import net.phoenix.chromatic_codes.api.ScriptedCodes.ColorDef;
import net.phoenix.chromatic_codes.api.ScriptedCodes.Definitions;
import net.phoenix.chromatic_codes.api.ScriptedCodes.Fx;
import net.phoenix.chromatic_codes.api.ScriptedCodes.OutlineDef;

import java.util.ArrayList;
import java.util.List;

/**
 * Ships script-defined codes (see {@link ScriptedCodes}) from the logical server to clients. The channel is
 * registered optional, so it never blocks a connection either way; {@link #sendTo} skips clients that don't have it.
 */
public final class ChromaticNetwork {

    private static final String PROTOCOL = "1";

    // What the logical server currently wants every client to have. Replaced on each script reload.
    private static volatile Definitions serverDefinitions = Definitions.EMPTY;

    private ChromaticNetwork() {}

    /** Mod-bus listener, hooked up in the mod constructor. */
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(PROTOCOL).optional().playToClient(SyncPayload.TYPE, SyncPayload.STREAM_CODEC,
                SyncPayload::handle);
    }

    public static void setServerDefinitions(Definitions defs) {
        serverDefinitions = defs == null ? Definitions.EMPTY : defs;
    }

    public static void sendTo(ServerPlayer player) {
        // Sending a payload the client didn't negotiate throws, so check first.
        if (!player.connection.hasChannel(SyncPayload.TYPE)) return;
        PacketDistributor.sendToPlayer(player, new SyncPayload(serverDefinitions));
    }

    public record SyncPayload(Definitions defs) implements CustomPacketPayload {

        public static final Type<SyncPayload> TYPE = new Type<>(PhoenixChromaticCodes.id("sync"));
        public static final StreamCodec<FriendlyByteBuf, SyncPayload> STREAM_CODEC = StreamCodec.of(
                SyncPayload::encode, SyncPayload::decode);

        // Decode-side sanity caps; a malicious server shouldn't be able to make a client allocate wildly.
        private static final int MAX_ENTRIES = 4096;
        private static final int MAX_COLORS = 64;

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void encode(FriendlyByteBuf buf, SyncPayload p) {
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

        public static SyncPayload decode(FriendlyByteBuf buf) {
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
                effects.add(new Fx(key, named, movement, colorSpeed, hasMove ? move : null, colors));
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

            return new SyncPayload(new Definitions(effects, colors, outlines, hasOutlineColor ? outlineColor : null));
        }

        public static void handle(SyncPayload p, IPayloadContext ctx) {
            ctx.enqueueWork(() -> ScriptedCodes.apply(p.defs));
        }
    }
}
