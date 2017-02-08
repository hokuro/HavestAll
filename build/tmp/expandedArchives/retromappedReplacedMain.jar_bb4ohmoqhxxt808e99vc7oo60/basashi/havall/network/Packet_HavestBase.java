package basashi.havall.network;

import java.util.LinkedList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class Packet_HavestBase implements IPacket_ModBase {
	public EntityPlayer _player = null;
	public ItemStack itemstack = null;
	public BlockPos _pos = null;
	public IBlockState blockID = null;
	public long nanoTime = 0L;
	public LinkedList<BlockPos> position = new LinkedList();

	@Override
	public void readPacketData(byte[] b) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public PacketBuffer writePacketData() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
