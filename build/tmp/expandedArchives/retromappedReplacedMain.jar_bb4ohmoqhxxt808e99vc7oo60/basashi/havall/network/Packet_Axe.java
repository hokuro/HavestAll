package basashi.havall.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class Packet_Axe extends Packet_HavestBase {
	public int count_cut = 0;

	public Packet_Axe() {
	}

	public void readPacketData(byte[] b) {
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(b));
		try {
			_pos = BlockPos.func_177969_a(stream.readLong());
			int i = stream.readInt();
			Block blk = Block.func_149729_e(i);
			this.blockID = blk.func_176194_O().func_177621_b();
		} catch (IOException localIOException) {
		}
	}

	public PacketBuffer writePacketData() {
		ByteArrayOutputStream byteBuf = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(byteBuf);
		try {
			stream.writeLong(_pos.func_177986_g());
			stream.writeInt(Block.func_149682_b(blockID.func_177230_c()));

			return new PacketBuffer(Unpooled.wrappedBuffer(byteBuf.toByteArray()));
		} catch (IOException localIOException1) {
		} finally {
			try {
				stream.close();
			} catch (IOException localIOException3) {
			}
		}
		return null;
	}

	public String toString() {
		return String.format("Packet_Axe (%s) => (blockid=%s)",
				new Object[] { null == this._pos ? "null" : this._pos.toString(), this.blockID.toString() });
	}

}
