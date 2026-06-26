package dev.simke.lodestoneteleport.network;

import dev.simke.lodestoneteleport.LodestoneTeleportMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record LodestoneActionPayload(CompoundTag data) implements CustomPacketPayload {
	public static final Type<LodestoneActionPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(LodestoneTeleportMod.MOD_ID, "action"));
	public static final StreamCodec<RegistryFriendlyByteBuf, LodestoneActionPayload> CODEC = CustomPacketPayload.codec(
		(payload, buf) -> buf.writeNbt(payload.data()),
		buf -> new LodestoneActionPayload(buf.readNbt())
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
