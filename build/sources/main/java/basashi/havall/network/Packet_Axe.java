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
			_pos = BlockPos.fromLong(stream.readLong());
			int i = stream.readInt();
			Block blk = Block.getBlockById(i);
			this.blockID = blk.getBlockState().getBaseState();
		} catch (IOException localIOException) {
		}
	}

	public PacketBuffer writePacketData() {
		ByteArrayOutputStream byteBuf = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(byteBuf);
		try {
			stream.writeLong(_pos.toLong());
			stream.writeInt(Block.getIdFromBlock(blockID.getBlock()));

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
