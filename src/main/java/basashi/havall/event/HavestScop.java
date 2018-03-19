package basashi.havall.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import basashi.havall.config.ConfigValue;
import basashi.havall.core.ModCommon;
import basashi.havall.network.Packet_HavestBase;
import basashi.havall.network.Packet_Scop;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class HavestScop implements IHavest  {

	private final Map<Long, List<BlockPos>> untouchableArea = new TreeMap();

	public CPacketCustomPayload getServerPacket(Packet_HavestBase pkt, World world){
		Packet_Scop pktScop = (Packet_Scop)pkt;
		if (!isDelArea(pktScop._pos)) {
			Long delAreaKey = Long.valueOf(System.currentTimeMillis() + 3000L);
			if (!untouchableArea.containsKey(delAreaKey)) {
				untouchableArea.put(delAreaKey, new ArrayList());
			}
			List<BlockPos> delArea = (List) untouchableArea.get(delAreaKey);
			pktScop.position.clear();
			pktScop.position.add(pktScop._pos);
			while (!pktScop.position.isEmpty()) {
				BlockPos n = (BlockPos) pktScop.position.poll();
				if (null == n) {break;}
				boolean isContain = false;
				for (BlockPos m : delArea) {
					if (m.equals(n)) {
						isContain = true;
						break;
					}
				}
				if (!isContain) {
					delArea.add(n);
					this.checkConnection(world, n, pktScop);
				}
			}
			pktScop.position.clear();
			return new CPacketCustomPayload(ModCommon.MOD_CHANEL,pktScop.writePacketData());
		}
		return null;
	}

	public Packet_HavestBase makePacket(boolean blAdd, BlockPos pos, IBlockState blk, Packet_HavestBase pkt){
		Packet_Scop retpkt;
		if (pkt != null){
			retpkt = (Packet_Scop)pkt;
			if ((blAdd) && (pos.equals(retpkt._pos))) {
				retpkt.nanoTime = System.nanoTime();
				retpkt.blockID = blk;
				retpkt.metadata = getMetaFromBlockState(blk);
				blAdd = false;
			}else{
				retpkt = null;
			}
		}else{
			retpkt = new Packet_Scop();
			retpkt._pos = pos;
			retpkt.nanoTime = System.nanoTime();
			retpkt.flag_Dirt = isDirt(blk.getBlock());
			retpkt.blockID = blk;
			retpkt.metadata = getMetaFromBlockState(blk);
		}

		return retpkt;
	}

	private boolean isDelArea(BlockPos pos) {
		for (Iterator<Map.Entry<Long, List<BlockPos>>> i = untouchableArea.entrySet().iterator(); i.hasNext();) {
			Map.Entry<Long, List<BlockPos>> e = (Map.Entry) i.next();
			if (((Long) e.getKey()).longValue() <= System.currentTimeMillis()) {
				i.remove();
			} else {
				for (BlockPos xyz : e.getValue()) {
					if (xyz.equals(pos)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void startHavest(Packet_HavestBase pkt, EntityPlayer player) {
		Packet_Scop p = (Packet_Scop)pkt;
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (null != server) {
			World world = server.getWorld(player.dimension);
			if (canDig(player, p)) {
				breakAll(world, player, p);
				if (ConfigValue.Scop.AutoCollect) {
					collectDrop(world, player, p);
				}
				if (ConfigValue.Scop.DropGather) {
					stackItem(world, player, p);
				}
			}
		}
	}

	private boolean canDig(EntityPlayer player, Packet_Scop p) {
		p.itemstack = player.getHeldItemMainhand();
		if ((p.itemstack == null) || (p.blockID == null)) {
			return false;
		}
		if ((Blocks.AIR == p.blockID) || (p.blockID == Blocks.BEDROCK)) {
			return false;
		}
		if (player.canHarvestBlock(p.blockID)) {
			return ConfigValue.CheckHavest(p.itemstack.getItem(), p.blockID);
		}
		return false;
	}

	private void breakAll(World world, EntityPlayer player, Packet_Scop p) {
		p.count_dig = 0;
		checkConnection(world, p._pos, p);
		while (breakBlock(world, player, p)) {}
		p.position.clear();
		if (ConfigValue.Scop.Durability == 1) {
			for (int i = 0; i < p.count_dig; i++) {
				p.itemstack.onBlockDestroyed(world, p.blockID, p._pos, player);
				if (p.itemstack.getCount() == 0) {
					//player.destroyCurrentEquippedItem();
					destroyCurrentEquippedItem(player);
					break;
				}
			}
		}
	}

	public void checkConnection(World world, BlockPos pos, Packet_Scop p) {
		int xs = 1, xe = 1;
		int ys = 1, ye = 1;
		int zs = 1, ze = 1;
		if (ConfigValue.Scop.Limiter != 0) {
			if (p._pos.getX() - ConfigValue.Scop.Limiter / 2 == pos.getX()) {
				xs = 0;
			}
			if (p._pos.getX() + ConfigValue.Scop.Limiter / 2 == pos.getX()) {
				xe = 0;
			}
			if (p._pos.getY() - ConfigValue.Scop.Limiter / 2 == pos.getY()) {
				ys = 0;
			}
			if (p._pos.getY() + ConfigValue.Scop.Limiter / 2 == pos.getY()) {
				ye = 0;
			}
			if (p._pos.getZ() - ConfigValue.Scop.Limiter / 2 == pos.getZ()) {
				zs = 0;
			}
			if (p._pos.getZ() + ConfigValue.Scop.Limiter / 2 == pos.getZ()) {
				ze = 0;
			}
		}
		if ((!ConfigValue.Scop.DestroyUnder) && (p._pos.getY() == pos.getY())) {
			ys = 0;
		}
		for (int x2 = -xs; x2 <= xe; x2++) {
			for (int y2 = -ys; y2 <= ye; y2++) {
				for (int z2 = -zs; z2 <= ze; z2++) {
					if (Math.abs(x2) + Math.abs(y2) + Math.abs(z2) == 1) {
						BlockPos pos2 = pos.add(x2, y2, z2);
						IBlockState block = world.getBlockState	(pos2);
						int metadata1 = getMetaFromBlockState(block);
						if (checkBlock(block.getBlock(), metadata1, p)) {
							p.position.offer(pos2);
						}
					}
				}
			}
		}
	}

	private boolean breakBlock(World world, EntityPlayer entityplayer, Packet_Scop p) {
		BlockPos pos = (BlockPos) p.position.poll();
		if (pos == null) {
			return false;
		}
		IBlockState block1 = world.getBlockState(pos);
		int metadata1 = getMetaFromBlockState(block1);
		if (checkBlock(block1.getBlock(), metadata1, p)) {
			p.count_dig += 1;

			block1.getBlock().harvestBlock(world, entityplayer, pos, block1, world.getTileEntity(pos),entityplayer.getHeldItemMainhand());
			try {
				BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, block1, entityplayer);
				block1.getBlock().dropXpOnBlockBreak(world, pos, event.getExpToDrop());
			} catch (Exception localException) {
			}
			world.setBlockToAir(pos);
			if ((ConfigValue.Scop.DropGather) || (ConfigValue.Scop.AutoCollect)) {
				moveEntityItem(world, entityplayer, pos, p._pos);
			}
			if (ConfigValue.Scop.Durability == 2) {
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

	private void moveEntityItem(World world, EntityPlayer entityplayer, BlockPos from, BlockPos to) {
		List<?> list = world.getEntitiesWithinAABBExcludingEntity(entityplayer,
				new AxisAlignedBB(from.getX(), from.getY(), from.getZ(),
						from.getX() + 1, from.getY() + 1, from.getZ() + 1));
		if ((null == list) || (list.isEmpty())) {
			return;
		}
		for (Object o : list) {
			Entity e = (Entity) o;
			if (((e instanceof EntityItem)) && (!e.isDead)) {
				e.setPosition(to.getX(), to.getY(), to.getZ());
			}
		}
	}

	private void stackItem(World world, EntityPlayer entityplayer, Packet_Scop p) {
		List<?> list = world.getEntitiesWithinAABBExcludingEntity(entityplayer,
				new AxisAlignedBB(p._pos.getX(), p._pos.getY(), p._pos.getZ(),
						p._pos.getX() + 1.0D, p._pos.getY() + 1.0D, p._pos.getZ() + 1.0D));
		if ((list == null) || (list.isEmpty())) {
			return;
		}
		for (int i = 0; i < list.size(); i++) {
			Entity entity1 = (Entity) list.get(i);
			if (((entity1 instanceof EntityItem)) && (!entity1.isDead)) {
				EntityItem e1 = (EntityItem) entity1;
				ItemStack e1Item = e1.getItem();
				int itemDamage = e1Item.getItemDamage();
				for (int j = i + 1; j < list.size(); j++) {
					Entity entity2 = (Entity) list.get(j);
					if (((entity2 instanceof EntityItem)) && (!entity2.isDead)) {
						EntityItem e2 = (EntityItem) entity2;
						ItemStack e2Item = e2.getItem();
						int itemDamage1 = e2Item.getItemDamage	();
						if ((e1Item.getItem() == e2Item.getItem	()) && (itemDamage == itemDamage1)) {
							e1Item.setCount(e1Item.getCount()+e2Item.getCount());
							entity2.setDead();
						}
					}
				}
				e1.setItem(e1Item);
			}
		}
	}

	private void collectDrop(World world, EntityPlayer entityplayer, Packet_Scop p) {
		List<?> list = world.getEntitiesWithinAABBExcludingEntity(entityplayer,
				new AxisAlignedBB(p._pos.getX() - 0.5D, p._pos.getY() - 0.5D,
						p._pos.getZ() - 0.5D, p._pos.getX() + 1.5D, p._pos.getY() + 1.5D,
						p._pos.getZ() + 1.5D));
		if ((list == null) || (list.isEmpty())) {
			return;
		}
		for (Object i : list) {
			Entity entity = (Entity) i;
			if (((entity instanceof EntityItem)) && (!entity.isDead)) {
				((EntityItem) entity).setNoPickupDelay();
				entity.onCollideWithPlayer(entityplayer);
			}
		}
	}

	private int getMetaFromBlockState(IBlockState blk) {
		try {
			return blk.getBlock().getMetaFromState(blk);
		} catch (IllegalArgumentException e) {
		}
		return 0;
	}

	private boolean checkBlock(Block block1, int metadata1, Packet_Scop p) {
		if (block1 == null) {
			return false;
		}
		if ((p.flag_Dirt) && ((block1 == Blocks.DIRT) || (block1 == Blocks.GRASS)
				|| (block1 == Blocks.MYCELIUM) || (block1 == Blocks.FARMLAND))) {
			return true;
		}
		return (block1 == p.blockID.getBlock()) && (p.metadata == metadata1);
	}

	public boolean isDirt(Block blockID) {
		return (blockID == Blocks.DIRT) || (blockID == Blocks.GRASS)
				|| (blockID == Blocks.MYCELIUM	) || (blockID == Blocks.FARMLAND);
	}

	@Override
	public boolean isRun(BlockPos pos) {
		// TODO 自動生成されたメソッド・スタブ
		return true;
	}

    public void destroyCurrentEquippedItem(EntityPlayer palyer)
    {
        ItemStack orig = palyer.getHeldItemMainhand();
        palyer.inventory.setInventorySlotContents(palyer.inventory.currentItem, (ItemStack)null);
        net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(palyer, orig,EnumHand.MAIN_HAND);
    }


}