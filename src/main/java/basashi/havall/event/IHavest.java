package basashi.havall.event;

import basashi.havall.network.Message_Packet;
import basashi.havall.network.Packet_HavestBase;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IHavest {
	Message_Packet getServerPacket(Packet_HavestBase pkt, World world);
	void startHavest(Packet_HavestBase p, PlayerEntity player);
	Packet_HavestBase makePacket(boolean blAdd, BlockPos pos, BlockState blk, Packet_HavestBase pkt);
	boolean isRun(BlockPos pos);
}
