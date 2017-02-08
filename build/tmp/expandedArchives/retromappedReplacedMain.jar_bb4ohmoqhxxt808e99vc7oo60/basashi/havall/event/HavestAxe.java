package basashi.havall.event;

import java.util.List;

import basashi.havall.config.ConfigValue;
import basashi.havall.core.ModCommon;
import basashi.havall.network.Packet_Axe;
import basashi.havall.network.Packet_HavestBase;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
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

public class HavestAxe implements IHavest{
	public static boolean dropleaves = true;

	public CPacketCustomPayload getServerPacket(Packet_HavestBase pkt, World world){
		return new CPacketCustomPayload(ModCommon.MOD_CHANEL,pkt.writePacketData());
	}

	public Packet_HavestBase makePacket(boolean blAdd, BlockPos pos, IBlockState blk, Packet_HavestBase pkt){
		Packet_Axe retpkt;
		if (pkt != null){
			retpkt = (Packet_Axe)pkt;
			if ((blAdd) && (pos.equals(retpkt._pos))) {
				retpkt.nanoTime = System.nanoTime();
				retpkt.blockID = (IBlockState) blk;
				blAdd = false;
			}else{
				retpkt = null;
			}
		}else{
			retpkt = new Packet_Axe();
			retpkt._pos = pos;
			retpkt.blockID = (IBlockState) blk;
			retpkt.nanoTime = System.nanoTime();
		}
		return retpkt;
	}

	public void startHavest(Packet_HavestBase pkt, EntityPlayer player){
		Packet_Axe p = (Packet_Axe)pkt;
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (null == server) {
		}
		World world = server.func_71218_a(player.field_71093_bK);
		if (canCut(player, p)) {
			breakAll(world, player, p);
			if (ConfigValue.Axe.DropGather) {
				stackItem(world, player, p);
			}
		}
	}

	private boolean canCut(EntityPlayer player, Packet_Axe p) {
		Block block = p.blockID.func_177230_c();
		p.itemstack = player.func_184614_ca();
		if (p.itemstack == null) {
			return false;
		}
		if ((block == null) || (Blocks.field_150350_a == block)) {
			return false;
		}
		if (block.func_149688_o(p.blockID) != Material.field_151575_d ) {
			return false;
		}
		if (player.func_184823_b(p.blockID)) {
			return ConfigValue.CheckHavest(p.itemstack.func_77973_b(),p.blockID);
		}
		return false;
	}

	private void stackItem(World world, EntityPlayer entityplayer, Packet_Axe p) {
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

	private void moveEntityItem(World world, EntityPlayer entityplayer, BlockPos from, BlockPos to) {
		List<?> list = world.func_72839_b(entityplayer,
				new AxisAlignedBB(from.func_177958_n(), from.func_177956_o(), from.func_177952_p(),
						from.func_177958_n() + 1, from.func_177956_o() + 1, from.func_177952_p() + 1));
//				AxisAlignedBB.fromBounds(from.getX(), from.getY(), from.getZ(),
//						from.getX() + 1, from.getY() + 1, from.getZ() + 1));
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

	private void breakAll(World world, EntityPlayer player, Packet_Axe p) {
		p.count_cut = 0;
		if (dropleaves) {
			checkLeaves(world, player, p._pos, p);
		}
		checkConnection(world, p._pos, p);
		while (breakBlock(world, player, p)) {
		}
		p.position.clear();
		if (ConfigValue.Axe.Durability == 1) {
			for (int i = 0; i < p.count_cut; i++) {
				p.itemstack.func_179548_a(world, p.blockID, p._pos, player);
				if (p.itemstack.field_77994_a == 0) {
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
		if (ConfigValue.Axe.Limiter != 0) {
			if (p._pos.func_177958_n() - ConfigValue.Axe.Limiter / 2 == pos.func_177958_n()) {
				xs = 0;
			}
			if (p._pos.func_177958_n() + ConfigValue.Axe.Limiter / 2 == pos.func_177958_n()) {
				xe = 0;
			}
			if (p._pos.func_177956_o() - ConfigValue.Axe.Limiter / 2 == pos.func_177956_o()) {
				ys = 0;
			}
			if (p._pos.func_177956_o() + ConfigValue.Axe.Limiter / 2 == pos.func_177956_o()) {
				ye = 0;
			}
			if (p._pos.func_177952_p() - ConfigValue.Axe.Limiter / 2 == pos.func_177952_p()) {
				zs = 0;
			}
			if (p._pos.func_177952_p() + ConfigValue.Axe.Limiter / 2 == pos.func_177952_p()) {
				ze = 0;
			}
		}
		if ((!ConfigValue.Axe.DestroyUnder) && (p._pos.func_177956_o() == pos.func_177956_o())) {
			ys = 0;
		}
		for (int x2 = -xs; x2 <= xe; x2++) {
			for (int y2 = -ys; y2 <= ye; y2++) {
				for (int z2 = -zs; z2 <= ze; z2++) {
					IBlockState blockID1 = world.func_180495_p(pos.func_177982_a(x2, y2, z2));
					if (checkBlock(blockID1.func_177230_c(), p)) {
						p.position.offer(pos.func_177982_a(x2, y2, z2));
					}
				}
			}
		}
	}

	private boolean breakBlock(World world, EntityPlayer entityplayer, Packet_Axe p) {
		BlockPos pos = (BlockPos) p.position.poll();
		if (pos == null) {
			return false;
		}
		IBlockState block1 = world.func_180495_p(pos);
		if (checkBlock(block1.func_177230_c(), p)) {
			p.count_cut += 1;
			BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, block1, entityplayer);
			Block block = block1.func_177230_c();
			block.func_180657_a(world, entityplayer, pos, block1, world.func_175625_s(pos),entityplayer.func_184614_ca());
			try {
				block.func_180637_b(world, pos, event.getExpToDrop());
				world.func_175698_g(pos);
			} catch (Exception localException) {
			}
			if (ConfigValue.Axe.DropGather) {
				moveEntityItem(world, entityplayer, pos, p._pos);
			}
			if (dropleaves) {
				checkLeaves(world, entityplayer, pos, p);
			}
			if (ConfigValue.Axe.Durability == 2) {
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

	private boolean checkBlock(Block block1, Packet_Axe p) {
		if ((block1 == null) || (Blocks.field_150350_a == block1)) {
			return false;
		}
		return block1 == p.blockID.func_177230_c();
	}

	private void checkLeaves(World world, EntityPlayer entityplayer, BlockPos pos, Packet_Axe p) {
		for (int i2 = -ConfigValue.Axe.LeavesRange; i2 <= ConfigValue.Axe.LeavesRange; i2++) {
			for (int j2 = -ConfigValue.Axe.LeavesRange; j2 <= ConfigValue.Axe.LeavesRange; j2++) {
				for (int k2 = -ConfigValue.Axe.LeavesRange; k2 <= ConfigValue.Axe.LeavesRange; k2++) {
					BlockPos pos2 = pos.func_177982_a(i2, j2, k2);
					IBlockState block1 = world.func_180495_p(pos2);
					Block block = block1.func_177230_c();
					if ((block1 != null) && (block.func_149688_o(block1) == Material.field_151584_j)
							&& ConfigValue.Axe.checkLeaves(block)) {
						BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos2, block1, entityplayer);
						block.func_180657_a(world, entityplayer, pos2, block1, world.func_175625_s(pos2),entityplayer.func_184614_ca());
						block.func_180637_b(world, pos2, event.getExpToDrop());
						world.func_175698_g(pos2);
						if (ConfigValue.Axe.DropGather) {
							moveEntityItem(world, entityplayer, pos2, p._pos);
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

    public void destroyCurrentEquippedItem(EntityPlayer palyer)
    {
        ItemStack orig = palyer.func_184614_ca();
        palyer.field_71071_by.func_70299_a(palyer.field_71071_by.field_70461_c, (ItemStack)null);
        net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(palyer, orig,EnumHand.MAIN_HAND);
    }
}
