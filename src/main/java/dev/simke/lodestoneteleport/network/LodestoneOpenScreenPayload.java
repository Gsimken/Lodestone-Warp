package dev.simke.lodestoneteleport.network;

import dev.simke.lodestoneteleport.LodestoneTeleportMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record LodestoneOpenScreenPayload(CompoundTag data) implements CustomPacketPayload {
	public static final Type<LodestoneOpenScreenPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(LodestoneTeleportMod.MOD_ID, "open_screen"));
	public static final StreamCodec<RegistryFriendlyByteBuf, LodestoneOpenScreenPayload> CODEC = CustomPacketPayload.codec(
		(payload, buf) -> buf.writeNbt(payload.data()),
		buf -> new LodestoneOpenScreenPayload(buf.readNbt())
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
