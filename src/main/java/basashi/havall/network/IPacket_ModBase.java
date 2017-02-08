package basashi.havall.network;
import net.minecraft.network.PacketBuffer;

public interface IPacket_ModBase {
	void readPacketData(byte[] b);
	PacketBuffer writePacketData();
	String toString();
}
