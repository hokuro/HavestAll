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
			retpkt.flag_Dirt = isDirt(blk.func_177230_c());
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
			World world = server.func_71218_a(player.field_71093_bK);
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
		p.itemstack = player.func_184614_ca();
		if ((p.itemstack == null) || (p.blockID == null)) {
			return false;
		}
		if ((Blocks.field_150350_a == p.blockID) || (p.blockID == Blocks.field_150357_h)) {
			return false;
		}
		if (player.func_184823_b(p.blockID)) {
			return ConfigValue.CheckHavest(p.itemstack.func_77973_b(), p.blockID);
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
				p.itemstack.func_179548_a(world, p.blockID, p._pos, player);
				if (p.itemstack.field_77994_a == 0) {
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
			if (p._pos.func_177958_n() - ConfigValue.Scop.Limiter / 2 == pos.func_177958_n()) {
				xs = 0;
			}
			if (p._pos.func_177958_n() + ConfigValue.Scop.Limiter / 2 == pos.func_177958_n()) {
				xe = 0;
			}
			if (p._pos.func_177956_o() - ConfigValue.Scop.Limiter / 2 == pos.func_177956_o()) {
				ys = 0;
			}
			if (p._pos.func_177956_o() + ConfigValue.Scop.Limiter / 2 == pos.func_177956_o()) {
				ye = 0;
			}
			if (p._pos.func_177952_p() - ConfigValue.Scop.Limiter / 2 == pos.func_177952_p()) {
				zs = 0;
			}
			if (p._pos.func_177952_p() + ConfigValue.Scop.Limiter / 2 == pos.func_177952_p()) {
				ze = 0;
			}
		}
		if ((!ConfigValue.Scop.DestroyUnder) && (p._pos.func_177956_o() == pos.func_177956_o())) {
			ys = 0;
		}
		for (int x2 = -xs; x2 <= xe; x2++) {
			for (int y2 = -ys; y2 <= ye; y2++) {
				for (int z2 = -zs; z2 <= ze; z2++) {
					if (Math.abs(x2) + Math.abs(y2) + Math.abs(z2) == 1) {
						BlockPos pos2 = pos.func_177982_a(x2, y2, z2);
						IBlockState block = world.func_180495_p	(pos2);
						int metadata1 = getMetaFromBlockState(block);
						if (checkBlock(block.func_177230_c(), metadata1, p)) {
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
		IBlockState block1 = world.func_180495_p(pos);
		int metadata1 = getMetaFromBlockState(block1);
		if (checkBlock(block1.func_177230_c(), metadata1, p)) {
			p.count_dig += 1;

			block1.func_177230_c().func_180657_a(world, entityplayer, pos, block1, world.func_175625_s(pos),entityplayer.func_184614_ca());
			try {
				BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, block1, entityplayer);
				block1.func_177230_c().func_180637_b(world, pos, event.getExpToDrop());
			} catch (Exception localException) {
			}
			world.func_175698_g(pos);
			if ((ConfigValue.Scop.DropGather) || (ConfigValue.Scop.AutoCollect)) {
				moveEntityItem(world, entityplayer, pos, p._pos);
			}
			if (ConfigValue.Scop.Durability == 2) {
				p.itemstack.func_179548_a(world, p.blockID, pos, entityplayer);
				if (p.itemstack.field_77994_a == 0) {
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
		List<?> list = world.func_72839_b(entityplayer,
				new AxisAlignedBB(from.func_177958_n(), from.func_177956_o(), from.func_177952_p(),
						from.func_177958_n() + 1, from.func_177956_o() + 1, from.func_177952_p() + 1));
		if ((null == list) || (list.isEmpty())) {
			return;
		}
		for (Object o : list) {
			Entity e = (Entity) o;
			if (((e instanceof EntityItem)) && (!e.field_70128_L)) {
				e.func_70107_b(to.func_177958_n(), to.func_177956_o(), to.func_177952_p());
			}
		}
	}

	private void stackItem(World world, EntityPlayer entityplayer, Packet_Scop p) {
		List<?> list = world.func_72839_b(entityplayer,
				new AxisAlignedBB(p._pos.func_177958_n(), p._pos.func_177956_o(), p._pos.func_177952_p(),
						p._pos.func_177958_n() + 1.0D, p._pos.func_177956_o() + 1.0D, p._pos.func_177952_p() + 1.0D));
		if ((list == null) || (list.isEmpty())) {
			return;
		}
		for (int i = 0; i < list.size(); i++) {
			Entity entity1 = (Entity) list.get(i);
			if (((entity1 instanceof EntityItem)) && (!entity1.field_70128_L)) {
				EntityItem e1 = (EntityItem) entity1;
				ItemStack e1Item = e1.func_92059_d	();
				int itemDamage = e1Item.func_77952_i();
				for (int j = i + 1; j < list.size(); j++) {
					Entity entity2 = (Entity) list.get(j);
					if (((entity2 instanceof EntityItem)) && (!entity2.field_70128_L)) {
						EntityItem e2 = (EntityItem) entity2;
						ItemStack e2Item = e2.func_92059_d();
						int itemDamage1 = e2Item.func_77952_i	();
						if ((e1Item.func_77973_b() == e2Item.func_77973_b	()) && (itemDamage == itemDamage1)) {
							e1Item.field_77994_a += e2Item.field_77994_a;
							entity2.func_70106_y();
						}
					}
				}
				e1.func_92058_a(e1Item);
			}
		}
	}

	private void collectDrop(World world, EntityPlayer entityplayer, Packet_Scop p) {
		List<?> list = world.func_72839_b(entityplayer,
				new AxisAlignedBB(p._pos.func_177958_n() - 0.5D, p._pos.func_177956_o() - 0.5D,
						p._pos.func_177952_p() - 0.5D, p._pos.func_177958_n() + 1.5D, p._pos.func_177956_o() + 1.5D,
						p._pos.func_177952_p() + 1.5D));
		if ((list == null) || (list.isEmpty())) {
			return;
		}
		for (Object i : list) {
			Entity entity = (Entity) i;
			if (((entity instanceof EntityItem)) && (!entity.field_70128_L)) {
				((EntityItem) entity).func_174868_q();
				entity.func_70100_b_(entityplayer);
			}
		}
	}

	private int getMetaFromBlockState(IBlockState blk) {
		try {
			return blk.func_177230_c().func_176201_c(blk);
		} catch (IllegalArgumentException e) {
		}
		return 0;
	}

	private boolean checkBlock(Block block1, int metadata1, Packet_Scop p) {
		if (block1 == null) {
			return false;
		}
		if ((p.flag_Dirt) && ((block1 == Blocks.field_150346_d) || (block1 == Blocks.field_150349_c)
				|| (block1 == Blocks.field_150391_bh) || (block1 == Blocks.field_150458_ak))) {
			return true;
		}
		return (block1 == p.blockID.func_177230_c()) && (p.metadata == metadata1);
	}

	public boolean isDirt(Block blockID) {
		return (blockID == Blocks.field_150346_d) || (blockID == Blocks.field_150349_c)
				|| (blockID == Blocks.field_150391_bh	) || (blockID == Blocks.field_150458_ak);
	}

	@Override
	public boolean isRun(BlockPos pos) {
		// TODO 自動生成されたメソッド・スタブ
		return true;
	}

    public void destroyCurrentEquippedItem(EntityPlayer palyer)
    {
        ItemStack orig = palyer.func_184614_ca();
        palyer.field_71071_by.func_70299_a(palyer.field_71071_by.field_70461_c, (ItemStack)null);
        net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(palyer, orig,EnumHand.MAIN_HAND);
    }


}