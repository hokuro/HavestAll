package basashi.havall.event;

import java.util.List;

import basashi.havall.config.MyConfig;
import basashi.havall.network.Message_Packet;
import basashi.havall.network.Packet_Axe;
import basashi.havall.network.Packet_HavestBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;

public class HavestAxe implements IHavest{
	public static boolean dropleaves = true;

	public Message_Packet getServerPacket(Packet_HavestBase pkt, World world){
		return new Message_Packet(pkt.writePacketData());
	}

	public Packet_HavestBase makePacket(boolean blAdd, BlockPos pos, BlockState blk, Packet_HavestBase pkt){
		Packet_Axe retpkt;
		if (pkt != null){
			retpkt = (Packet_Axe)pkt;
			if ((blAdd) && (pos.equals(retpkt._pos))) {
				retpkt.nanoTime = System.nanoTime();
				retpkt.blockID = (BlockState) blk;
				blAdd = false;
			}else{
				retpkt = null;
			}
		}else{
			retpkt = new Packet_Axe();
			retpkt._pos = pos;
			retpkt.blockID = (BlockState) blk;
			retpkt.nanoTime = System.nanoTime();
		}
		return retpkt;
	}

	public void startHavest(Packet_HavestBase pkt, PlayerEntity player){
		Packet_Axe p = (Packet_Axe)pkt;
		MinecraftServer server = player.getServer();
		if (null == server) {
		}
		World world = server.getWorld(player.dimension);
		if (canCut(player, p)) {
			breakAll(world, player, p);
			if (MyConfig._axe.DropGather.get()) {
				stackItem(world, player, p);
			}
		}
	}

	private boolean canCut(PlayerEntity player, Packet_Axe p) {
		Block block = p.blockID.getBlock();
		p.itemstack = player.getHeldItemMainhand();
		if (p.itemstack == null) {
			return false;
		}
		if ((block == null) || (Blocks.AIR == block)) {
			return false;
		}
		if (block.getMaterial(p.blockID) != Material.WOOD ) {
			return false;
		}
		if (player.canHarvestBlock(p.blockID)) {
			return MyConfig.CheckHavest(p.itemstack.getItem(),p.blockID);
		}
		return false;
	}

	private void stackItem(World world, PlayerEntity entityplayer, Packet_Axe p) {
		List<?> list = world.getEntitiesWithinAABBExcludingEntity(entityplayer,
				new AxisAlignedBB(p._pos.getX(), p._pos.getY(), p._pos.getZ(),
						p._pos.getX() + 1.0D, p._pos.getY() + 1.0D, p._pos.getZ() + 1.0D));
		if ((list == null) || (list.isEmpty())) {
			return;
		}
		for (int i = 0; i < list.size(); i++) {
			Entity entity1 = (Entity) list.get(i);
			if (((entity1 instanceof ItemEntity)) && (entity1.isAlive())) {
				ItemEntity e1 = (ItemEntity) entity1;
				ItemStack e1Item = e1.getItem();
				int itemDamage = e1Item.getDamage();
				for (int j = i + 1; j < list.size(); j++) {
					Entity entity2 = (Entity) list.get(j);
					if (((entity2 instanceof ItemEntity)) && (entity2.isAlive())) {
						ItemEntity e2 = (ItemEntity) entity2;
						ItemStack e2Item = e2.getItem();
						int itemDamage1 = e2Item.getDamage();
						if ((e1Item.getItem() == e2Item.getItem()) && (itemDamage == itemDamage1)) {
							e1Item.grow(e2Item.getCount());
							entity2.remove();
						}
					}
				}
				e1.setItem(e1Item);
			}
		}
	}

	private void moveItemEntity(World world, PlayerEntity entityplayer, BlockPos from, BlockPos to) {
		List<?> list = world.getEntitiesWithinAABBExcludingEntity(entityplayer,
				new AxisAlignedBB(from.getX(), from.getY(), from.getZ(),
						from.getX() + 1, from.getY() + 1, from.getZ() + 1));
//				AxisAlignedBB.fromBounds(from.getX(), from.getY(), from.getZ(),
//						from.getX() + 1, from.getY() + 1, from.getZ() + 1));
		if ((null == list) || (list.isEmpty())) {
			return;
		}
		for (Object o : list) {
			Entity e = (Entity) o;
			if (((e instanceof ItemEntity)) && (e.isAlive())) {
				e.setPosition(to.getX(), to.getY(), to.getZ());
			}
		}
	}

	private void breakAll(World world, PlayerEntity player, Packet_Axe p) {
		p.count_cut = 0;
		if (dropleaves) {
			checkLeaves(world, player, p._pos, p);
		}
		checkConnection(world, p._pos, p);
		while (breakBlock(world, player, p)) {
		}
		p.position.clear();
		if (MyConfig._axe.Durability.get() == 1) {
			for (int i = 0; i < p.count_cut; i++) {
				p.itemstack.onBlockDestroyed(world, p.blockID, p._pos, player);
				if (p.itemstack.getCount() == 0) {
					destroyCurrentEquippedItem(player);
					//player.destroyCurrentEquippedItem();
					break;
				}
			}
		}
	}

	private void checkConnection(World world, BlockPos pos, Packet_Axe p) {
		int xs = 1;
		int xe = 1;
		int ys = 1;
		int ye = 1;
		int zs = 1;
		int ze = 1;
		if (MyConfig._axe.Limiter.get() != 0) {
			if (p._pos.getX() - MyConfig._axe.Limiter.get() / 2 == pos.getX()) {
				xs = 0;
			}
			if (p._pos.getX() + MyConfig._axe.Limiter.get() / 2 == pos.getX()) {
				xe = 0;
			}
			if (p._pos.getY() - MyConfig._axe.Limiter.get() / 2 == pos.getY()) {
				ys = 0;
			}
			if (p._pos.getY() + MyConfig._axe.Limiter.get() / 2 == pos.getY()) {
				ye = 0;
			}
			if (p._pos.getZ() - MyConfig._axe.Limiter.get() / 2 == pos.getZ()) {
				zs = 0;
			}
			if (p._pos.getZ() + MyConfig._axe.Limiter.get() / 2 == pos.getZ()) {
				ze = 0;
			}
		}
		if ((!MyConfig._axe.DestroyUnder.get()) && (p._pos.getY() == pos.getY())) {
			ys = 0;
		}
		for (int x2 = -xs; x2 <= xe; x2++) {
			for (int y2 = -ys; y2 <= ye; y2++) {
				for (int z2 = -zs; z2 <= ze; z2++) {
					BlockState blockID1 = world.getBlockState(pos.add(x2, y2, z2));
					if (checkBlock(blockID1.getBlock(), p)) {
						p.position.offer(pos.add(x2, y2, z2));
					}
				}
			}
		}
	}

	private boolean breakBlock(World world, PlayerEntity entityplayer, Packet_Axe p) {
		BlockPos pos = (BlockPos) p.position.poll();
		if (pos == null) {
			return false;
		}
		BlockState block1 = world.getBlockState(pos);
		if (checkBlock(block1.getBlock(), p)) {
			p.count_cut += 1;
			BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, block1, entityplayer);
			Block block = block1.getBlock();
			block.harvestBlock(world, entityplayer, pos, block1, world.getTileEntity(pos),entityplayer.getHeldItemMainhand());
			try {
				block.dropXpOnBlockBreak(world, pos, event.getExpToDrop());
				world.setBlockState(pos, Blocks.AIR.getDefaultState());
			} catch (Exception localException) {
			}
			if (MyConfig._axe.DropGather.get()) {
				moveItemEntity(world, entityplayer, pos, p._pos);
			}
			if (dropleaves) {
				checkLeaves(world, entityplayer, pos, p);
			}
			if (MyConfig._axe.Durability.get() == 2) {
				p.itemstack.onBlockDestroyed(world, p.blockID, pos, entityplayer);
				if (p.itemstack.getCount() == 0) {
					//entityplayer.destroyCurrentEquippedItem();
					destroyCurrentEquippedItem(entityplayer);
					return false;
				}
			}
			checkConnection(world, pos, p);
		}
		return true;
	}

	private boolean checkBlock(Block block1, Packet_Axe p) {
		if ((block1 == null) || (Blocks.AIR == block1)) {
			return false;
		}
		return block1 == p.blockID.getBlock();
	}

	private void checkLeaves(World world, PlayerEntity entityplayer, BlockPos pos, Packet_Axe p) {
		for (int i2 = -MyConfig._axe.LeavesRange.get(); i2 <= MyConfig._axe.LeavesRange.get(); i2++) {
			for (int j2 = -MyConfig._axe.LeavesRange.get(); j2 <= MyConfig._axe.LeavesRange.get(); j2++) {
				for (int k2 = -MyConfig._axe.LeavesRange.get(); k2 <= MyConfig._axe.LeavesRange.get(); k2++) {
					BlockPos pos2 = pos.add(i2, j2, k2);
					BlockState block1 = world.getBlockState(pos2);
					Block block = block1.getBlock();
					if ((block1 != null) && (block.getMaterial(block1) == Material.LEAVES)
							&& MyConfig._axe.checkLeaves(block)) {
						BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos2, block1, entityplayer);
						block.harvestBlock(world, entityplayer, pos2, block1, world.getTileEntity(pos2),entityplayer.getHeldItemMainhand());
						block.dropXpOnBlockBreak(world, pos2, event.getExpToDrop());
						world.setBlockState(pos2, Blocks.AIR.getDefaultState());
						if (MyConfig._axe.DropGather.get()) {
							moveItemEntity(world, entityplayer, pos2, p._pos);
						}
					}
				}
			}
		}
	}

	@Override
	public boolean isRun(BlockPos pos) {
		// TODO 自動生成されたメソッド・スタブ
		return true;
	}

    public void destroyCurrentEquippedItem(PlayerEntity palyer)
    {
        ItemStack orig = palyer.getHeldItemMainhand();
        palyer.inventory.setInventorySlotContents(palyer.inventory.currentItem, (ItemStack)null);
        net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(palyer, orig,Hand.MAIN_HAND);
    }
}
