package basashi.havall.network;

import java.util.function.Supplier;

import basashi.havall.event.McEventHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class Message_Packet {
	private int _size;
	private byte[] _packet;
	public Message_Packet(PacketBuffer packet)
	{
		_packet = packet.array();
		_size = _packet.length;
	}

	public Message_Packet(byte[] packet){
		_size = packet.length;
		_packet = packet.clone();
	}

	public static void encode(Message_Packet pkt, PacketBuffer buf)
	{
		buf.writeInt(pkt._size);
		buf.writeByteArray(pkt._packet);
	}

	public static Message_Packet decode(PacketBuffer buf)
	{
		int size = buf.readInt();
		byte[] packet = buf.readByteArray(size);
		return new Message_Packet(packet);
	}

	public static class Handler
	{
		public static void handle(final Message_Packet pkt, Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(() -> {
				McEventHandler.onServerPacket(pkt, ctx.get().getSender());
			});
			ctx.get().setPacketHandled(true);
		}
	}

	public byte[] Data() {
		return _packet;
	}

}