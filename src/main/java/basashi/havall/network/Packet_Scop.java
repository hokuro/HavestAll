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

public class Packet_Scop extends Packet_HavestBase {
	public int metadata = 0;
	public boolean flag_Dirt = false;
	public int count_dig = 0;

	@Override
	public void readPacketData(byte[] b) {
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(b));
		try{
			_pos = BlockPos.fromLong(stream.readLong());
			int i = stream.readInt();
			this.blockID = Block.getStateById(i);
			metadata = stream.readInt();
			flag_Dirt = stream.readBoolean();
		}catch(IOException ex){
		}
	}

	@Override
	public PacketBuffer writePacketData() {
		ByteArrayOutputStream bytebuf = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytebuf);
		try{
			stream.writeLong(_pos.toLong());
			stream.writeInt(Block.getStateId(blockID.getBlockState()));
			stream.writeInt(metadata);
			stream.writeBoolean(flag_Dirt);

			return new PacketBuffer(Unpooled.wrappedBuffer(bytebuf.toByteArray()));
			}catch(IOException ex){
			try{
				stream.close();
			}catch(IOException ex2){
			}
		}
		return null;
	}

	@Override
	public String toString(){
		return String.format("Packet_Havestt_Scop (%s) => (block=%s, metadata=%d)",
				new Object[] { null == this._pos ? "null" : this._pos.toString(), this.blockID.toString(),
				Integer.valueOf(this.metadata) });
	}
}
