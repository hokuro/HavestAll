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
public class Packet_PickAxe extends Packet_HavestBase {
	public int metadata = 0;
	public int count_mine = 0;
	public boolean flag_rs = false;

	public Packet_PickAxe() {
	}

	public void readPacketData(byte[] b) {
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(b));
		try {
			super._pos = BlockPos.fromLong(stream.readLong());
			int i = stream.readInt();
			Block blk = Block.getBlockById(i);
			this.metadata = stream.readInt();
			this.blockID = blk==null?null: blk.getStateFromMeta(this.metadata);
			this.flag_rs = stream.readBoolean();
		} catch (IOException localIOException) {
		}
	}

	public PacketBuffer writePacketData() {
		ByteArrayOutputStream byteBuf = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(byteBuf);
		try {
			stream.writeLong(this._pos.toLong());
			stream.writeInt(Block.getIdFromBlock(this.blockID.getBlock()));
			stream.writeInt(this.metadata);
			stream.writeBoolean(this.flag_rs);

			return new PacketBuffer(Unpooled.wrappedBuffer(byteBuf.toByteArray()));
		} catch (IOException ex) {
		} finally {
			try {
				stream.close();
			} catch (IOException ex2) {
			}
		}
		return null;
	}

	public String toString() {
		return String.format("Packet_PickAxe (%s) => (blockid=%d, metadata=%d)",
				new Object[] { this._pos, this.blockID, Integer.valueOf(this.metadata) });
	}
}
