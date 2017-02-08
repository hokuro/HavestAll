package basashi.havall.event;

import basashi.havall.network.Packet_HavestBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IHavest {
	CPacketCustomPayload getServerPacket(Packet_HavestBase pkt, World world);
	void startHavest(Packet_HavestBase p, EntityPlayer player);
	Packet_HavestBase makePacket(boolean blAdd, BlockPos pos, IBlockState blk, Packet_HavestBase pkt);
	boolean isRun(BlockPos pos);
}
