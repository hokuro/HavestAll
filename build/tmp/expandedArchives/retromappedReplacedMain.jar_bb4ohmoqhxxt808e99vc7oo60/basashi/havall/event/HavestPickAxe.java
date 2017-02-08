package basashi.havall.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import basashi.havall.config.ConfigValue;
import basashi.havall.core.ModCommon;
import basashi.havall.network.Packet_HavestBase;
import basashi.havall.network.Packet_PickAxe;
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

public class HavestPickAxe implements IHavest {

	private final int FLAG_CHANGE_NUM = 10;


	private static final int untouchableMiliSec = 3000;
	private static final Map<Long, List<BlockPos>> untouchableArea = new TreeMap();

	public CPacketCustomPayload getServerPacket(Packet_HavestBase pkt, World world){
		Packet_PickAxe pktScop = (Packet_PickAxe)pkt;
		if (!isDelArea(pkt._pos)) {
			Long delAreaKey = Long.valueOf(System.currentTimeMillis() + 3000L);
			if (!untouchableArea.containsKey(delAreaKey)) {
				untouchableArea.put(delAreaKey, new ArrayList());
			}
			List<BlockPos> delArea = (List) untouchableArea.get(delAreaKey);
			pkt.position.clear();
			pkt.position.add(pkt._pos);
			while (!pkt.position.isEmpty()) {
				BlockPos n = (BlockPos) pkt.position.poll();
				if (null == n) {
					break;
				}
				boolean isContain = false;
				for (BlockPos m : delArea) {
					if (m.equals(n)) {
						isContain = true;
						break;
					}
				}
				if (!isContain) {
					delArea.add(n);
					checkConnection(world, n, (Packet_PickAxe)pkt);
				}
			}
			pkt.position.clear();
			return new CPacketCustomPayload(ModCommon.MOD_CHANEL,pkt.writePacketData());
		}
		return null;
	}

	public Packet_HavestBase makePacket(boolean blAdd, BlockPos pos, IBlockState blk, Packet_HavestBase pkt){
		Packet_PickAxe retpkt;
		if (pkt != null){
			retpkt = (Packet_PickAxe)pkt;
			if ((blAdd) && (pos.equals(retpkt._pos))) {
				retpkt.nanoTime = System.nanoTime();
				retpkt.blockID = blk;
				retpkt.metadata = getMetaFromBlockState(blk);
				blAdd = false;
			}else{
				retpkt = null;
			}
		}else{
			retpkt = new Packet_PickAxe();
			retpkt._pos = pos;
			retpkt.blockID = blk;
			retpkt.metadata = getMetaFromBlockState(blk);
			if ((retpkt.blockID == Blocks.field_150450_ax) || (retpkt.blockID == Blocks.field_150439_ay)) {
				retpkt.flag_rs = true;
			}
			retpkt.nanoTime = System.nanoTime();
		}
		return retpkt;
	}

	public void startHavest(Packet_HavestBase pkt, EntityPlayer player) {
		Packet_PickAxe p = (Packet_PickAxe)pkt;
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (null == server) {
		}
		World world = server.func_71218_a(player.field_71093_bK);
		if (!canMine(player, p)) {
			return;
		}
		breakAll(world, player, p);
		if (ConfigValue.PickAxe.AutoCollect) {
			collectDrop(world, player, p);
		}
		if (ConfigValue.PickAxe.DropGather) {
			stackItem(world, player, p);
		}
	}

	private void breakAll(World world, EntityPlayer player, Packet_PickAxe p) {
		checkConnection(world, p._pos, p);
		while (breakBlock(world, player, p)) {
		}
		p.position.clear();
		if (ConfigValue.PickAxe.Durability == 1) {
			for (int i = 0; i < p.count_mine; i++) {
				p.itemstack.func_179548_a(world, p.blockID, p._pos, player);
				if (p.itemstack.field_77994_a == 0) {
					destroyCurrentEquippedItem(player);
					//player.destroyCurrentEquippedItem();
					break;
				}
			}
		}
	}

	private boolean breakBlock(World world, EntityPlayer entityplayer, Packet_PickAxe p) {
		BlockPos pos = (BlockPos) p.position.poll();
		if (pos == null) {
			return false;
		}
		IBlockState block1 = world.func_180495_p(pos);
		int metadata1 = getMetaFromBlockState(block1);
		if (checkBlock(block1.func_177230_c(), metadata1, p)) {
			p.count_mine += 1;
			block1.func_177230_c().func_180657_a(world, entityplayer, pos, block1, world.func_175625_s(pos), entityplayer.func_184614_ca());
			try {
				BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, block1, entityplayer);
				block1.func_177230_c().func_180637_b(world, pos, event.getExpToDrop());
			} catch (Exception localException) {
			}
			world.func_175698_g(pos);
			if ((ConfigValue.PickAxe.DropGather) || (ConfigValue.PickAxe.AutoCollect)) {
				moveEntityItem(world, entityplayer, pos, p._pos);
			}
			if (ConfigValue.PickAxe.Durability == 2) {
				p.itemstack.func_179548_a(world, p.blockID, pos, entityplayer);
				if (p.itemstack.field_77994_a == 0) {
					destroyCurrentEquippedItem(entityplayer);
					//entityplayer.destroyCurrentEquippedItem();
					return false;
				}
			}
			checkConnection(world, pos, p);
		}
		return true;
	}

	private static boolean canMine(EntityPlayer player, Packet_PickAxe p) {
		Block block = p.blockID.func_177230_c();
		p.itemstack = player.func_184614_ca();
		if ((p.itemstack == null) || (block == null)) {
			return false;
		}
		if (p.itemstack.field_77994_a <= 0) {
			return false;
		}
		if ((null == p.blockID) || (Blocks.field_150357_h == p.blockID.func_177230_c())) {
			return false;
		}
		if (ConfigValue.getToolKind(p.itemstack.func_77973_b()) != ConfigValue.TOOLS.PICKAXE) {
			return false;
		}
		if (p.flag_rs) {
			return (ConfigValue.CheckHavest(p.itemstack.func_77973_b(), Blocks.field_150450_ax.func_176223_P()) ||
					ConfigValue.CheckHavest(p.itemstack.func_77973_b(), Blocks.field_150439_ay.func_176223_P()));

		}
		return ConfigValue.CheckHavest(p.itemstack.func_77973_b(),p.blockID);
	}

	private static void collectDrop(World world, EntityPlayer entityplayer, Packet_PickAxe p) {
		List<?> list = world.func_72839_b(entityplayer,
				new AxisAlignedBB(p._pos.func_177958_n() - 0.5D, p._pos.func_177956_o() - 0.5D,
						p._pos.func_177952_p() - 0.5D, p._pos.func_177958_n() + 1.5D, p._pos.func_177956_o() + 1.5D,
						p._pos.func_177952_p() + 1.5D));
		if ((list == null) || (list.isEmpty())) {
			return;
		}
		for (Object o : list) {
			Entity entity = (Entity) o;
			if ((entity instanceof EntityItem)) {
				((EntityItem) entity).func_174868_q();
				entity.func_70100_b_(entityplayer);
			}
		}
	}

	private static void stackItem(World world, EntityPlayer entityplayer, Packet_PickAxe p) {
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
				ItemStack e1Item = e1.func_92059_d();
				int itemDamage = e1Item.func_77952_i();
				for (int j = i + 1; j < list.size(); j++) {
					Entity entity2 = (Entity) list.get(j);
					if (((entity2 instanceof EntityItem)) && (!entity2.field_70128_L)) {
						EntityItem e2 = (EntityItem) entity2;
						ItemStack e2Item = e2.func_92059_d();
						int itemDamage1 = e2Item.func_77952_i();
						if ((e1Item.func_77973_b() == e2Item.func_77973_b()) && (itemDamage == itemDamage1)) {
							e1Item.field_77994_a += e2Item.field_77994_a;
							entity2.func_70106_y();
						}
					}
				}
				e1.func_92058_a(e1Item);
			}
		}
	}

	private static boolean isDelArea(BlockPos pos) {
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

	private void checkConnection(World world, BlockPos pos, Packet_PickAxe p) {
		int xs = 1;
		int xe = 1;
		int ys = 1;
		int ye = 1;
		int zs = 1;
		int ze = 1;
		if (ConfigValue.PickAxe.Limiter != 0) {
			if (p._pos.func_177958_n() - ConfigValue.PickAxe.Limiter / 2 == pos.func_177958_n()) {
				xs = 0;
			}
			if (p._pos.func_177958_n() + ConfigValue.PickAxe.Limiter / 2 == pos.func_177958_n()) {
				xe = 0;
			}
			if (p._pos.func_177956_o() - ConfigValue.PickAxe.Limiter / 2 == pos.func_177956_o()) {
				ys = 0;
			}
			if (p._pos.func_177956_o() + ConfigValue.PickAxe.Limiter / 2 == pos.func_177956_o()) {
				ye = 0;
			}
			if (p._pos.func_177952_p() - ConfigValue.PickAxe.Limiter / 2 == pos.func_177952_p()) {
				zs = 0;
			}
			if (p._pos.func_177952_p() + ConfigValue.PickAxe.Limiter / 2 == pos.func_177952_p()) {
				ze = 0;
			}
		}
		if ((!ConfigValue.PickAxe.DestroyUnder) && (p._pos.func_177956_o() == pos.func_177956_o())) {
			ys = 0;
		}
		for (int x2 = -xs; x2 <= xe; x2++) {
			for (int y2 = -ys; y2 <= ye; y2++) {
				for (int z2 = -zs; z2 <= ze; z2++) {
					if (Math.abs(x2) + Math.abs(y2) + Math.abs(z2) == 1) {
						BlockPos pos2 = pos.func_177982_a(x2, y2, z2);
						IBlockState block = world.func_180495_p(pos2);
						int metadata1 = getMetaFromBlockState(block);
						if (checkBlock(block.func_177230_c(), metadata1, p)) {
							p.position.offer(pos2);
						}
					}
				}
			}
		}
	}

	private boolean checkBlock(Block block1, int l, Packet_PickAxe p) {
		if (block1 == null) {
			return false;
		}
		if (!p.flag_rs) {
			if ((block1 != p.blockID.func_177230_c()) || (p.metadata != l)) {
				return false;
			}
		} else if ((block1 != Blocks.field_150450_ax) && (block1 != Blocks.field_150439_ay)) {
			return false;
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
				e.func_70107_b(to.func_177958_n() + 0.5D, to.func_177956_o() + 0.5D, to.func_177952_p() + 0.5D);
			}
		}
	}

	private int getMetaFromBlockState(IBlockState i) {
		try {
			return i.func_177230_c().func_176201_c(i);
		} catch (IllegalArgumentException e) {
		}
		return 0;
	}

	@Override
	public boolean isRun(BlockPos pos) {
		// TODO 自動生成されたメソッド・スタブ
		if (isDelArea(pos)) {return false;}
		return true;
	}

    public void destroyCurrentEquippedItem(EntityPlayer palyer)
    {
        ItemStack orig = palyer.func_184614_ca();
        palyer.field_71071_by.func_70299_a(palyer.field_71071_by.field_70461_c, (ItemStack)null);
        net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(palyer, orig,EnumHand.MAIN_HAND);
    }
}
