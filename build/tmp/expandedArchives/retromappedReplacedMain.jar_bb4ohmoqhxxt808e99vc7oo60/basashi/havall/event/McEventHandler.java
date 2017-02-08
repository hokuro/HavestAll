package basashi.havall.event;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import basashi.havall.client.ClientProxy;
import basashi.havall.config.ConfigValue;
import basashi.havall.network.Packet_Axe;
import basashi.havall.network.Packet_HavestBase;
import basashi.havall.network.Packet_PickAxe;
import basashi.havall.network.Packet_Scop;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class McEventHandler{
	public static final long attackHistoryDelayNanoTime = 15000000000L;
	public static final BlockingQueue<Packet_HavestBase> attackHistory = new LinkedBlockingQueue();
	public static final BlockingQueue<Packet_HavestBase> _serverPacket = new LinkedBlockingQueue();
	private boolean StartMode = false;
	private int flag_change = 0;
	private int flag_change2 = 0;

	public static IHavest _scop = new HavestScop();
	public static IHavest _axe = new HavestAxe();
	public static IHavest _pickaxe = new HavestPickAxe();
	public ConfigValue.TOOLS bftool = ConfigValue.TOOLS.OTHER;

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void tickEvent(TickEvent.ClientTickEvent event) {
		if (!TickEvent.Phase.END.equals(event.phase)) {
			return;
		}
		ConfigValue.reloadConfig();

		Minecraft minecraft = FMLClientHandler.instance().getClient();
		if (!StartMode) {
			attackHistory.clear();
		} else {
			World world = minecraft.field_71441_e;
			if (null != world) {
				for (Iterator<Packet_HavestBase> i = attackHistory.iterator(); i.hasNext();) {
					Packet_HavestBase pkt = (Packet_HavestBase) i.next();
					if (System.nanoTime() - pkt.nanoTime >= 3000000000L) {
						i.remove();
					} else {
						CPacketCustomPayload packet;
						IBlockState blockID1 = world.func_180495_p(pkt._pos);
						if ((null == blockID1) || (Blocks.field_150350_a == blockID1.func_177230_c())) {
							i.remove();
							Item tool = (null==minecraft.field_71439_g.func_184614_ca())?null:minecraft.field_71439_g.func_184614_ca().func_77973_b();
							try{
								packet = getHavestInstance(ConfigValue.getToolKind(tool)).getServerPacket(pkt, world);
							}catch(Exception exe){
								packet = null;
							}
							if (packet != null){
								minecraft.func_147114_u().func_147297_a(packet);
							}
						}
					}
				}
			}
		}
		if ( minecraft.field_71441_e != null){
			// ツールの有効無効処理
			ToolEnable(minecraft);
			// ブロックの登録
			BlockRegister(minecraft);
		}
	}

	@SubscribeEvent
	public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
		EntityPlayer player = ((NetHandlerPlayServer) event.getHandler()).field_147369_b;
		ItemStack itmstk = player.func_184614_ca();
		ConfigValue.TOOLS tool;
		if( (itmstk != null) &&((tool = ConfigValue.getToolKind(itmstk.func_77973_b()))!= ConfigValue.TOOLS.OTHER) ){
			Packet_HavestBase p;
			switch(tool){
			case SCOP:
				p = new Packet_Scop();
				break;
			case AXE:
				p = new Packet_Axe();
				break;
			case PICKAXE:
				p = new Packet_PickAxe();
				break;
				default:
					p=new Packet_HavestBase();
				break;
			}

			p._player = ((NetHandlerPlayServer) event.getHandler()).field_147369_b;
			p.readPacketData(event.getPacket().payload().array());
			if (p._player.func_70011_f(p._pos.func_177958_n(), p._pos.func_177956_o(), p._pos.func_177952_p()) > 6.0D) {
				return;
			}
			_serverPacket.offer(p);
		}
	}

	@SubscribeEvent
	public void tickEventServer(TickEvent.ServerTickEvent event) {
		if (!TickEvent.Phase.END.equals(event.phase)) {
			return;
		}
		Packet_HavestBase p = _serverPacket.poll();
		if (null == p) {
			return;
		}
		ItemStack itmstk = p._player.func_184614_ca();
		ConfigValue.TOOLS tool;
		if ((itmstk != null) && ((tool = ConfigValue.getToolKind(itmstk.func_77973_b()))!= ConfigValue.TOOLS.OTHER)){
			getHavestInstance(tool).startHavest(p, p._player);
		}
	}

	@SideOnly(Side.CLIENT)
	public static void addAttackBlock(BlockPos pos, boolean clearHistory, IBlockState block, ConfigValue.TOOLS tool) {
		IHavest instance = getHavestInstance(tool);
		if (clearHistory) {attackHistory.clear();}
		if (instance == null){return;}

		World world = FMLClientHandler.instance().getClient().field_71441_e;
		EntityPlayer player = FMLClientHandler.instance().getClient().field_71439_g;
		if (null == block) {
			block = world.func_180495_p(pos);
		}
		if (!instance.isRun(pos)){return;}
		Item equipedItem = null == player.func_184614_ca() ? null : player.func_184614_ca().func_77973_b();

		boolean blAdd = ConfigValue.CheckHavest(equipedItem, block);
		for (Iterator<Packet_HavestBase> i = attackHistory.iterator(); i.hasNext();) {
			Packet_HavestBase w = i.next();
			Packet_HavestBase l =  instance.makePacket(blAdd,pos,block,w);
			if (l == null){l = w;}
			else{blAdd = false;}
		}
		if (blAdd) {
			Packet_HavestBase p = instance.makePacket(blAdd,pos,block,null);
			try {
				attackHistory.put(p);
			} catch (InterruptedException localInterruptedException) {
			}
		}
	}

	private static IHavest getHavestInstance(ConfigValue.TOOLS tool){
		switch(tool){
		case SCOP:
			return _scop;
		case AXE:
			return _axe;
		case PICKAXE:
			return _pickaxe;
		default:
			return null;
		}
	}

	@SideOnly(Side.CLIENT)
	private void ToolEnable(Minecraft minecraft){
		// ツールの有効無効処理
		EntityPlayer player = minecraft.field_71439_g;
		ItemStack itemstk= player.func_184614_ca();
		ConfigValue.TOOLS tool = (itemstk!=null?ConfigValue.getToolKind(itemstk.func_77973_b()):ConfigValue.TOOLS.OTHER);
		if (StartMode && ((tool == ConfigValue.TOOLS.OTHER) || (bftool != tool))){
				StartMode = false;
				bftool = tool;
				player.func_146105_b(new TextComponentString("Havest OFF"));
		}else{
			if (ClientProxy.Press_Key_Enable() && (minecraft.field_71462_r == null)
					&& (tool != ConfigValue.TOOLS.OTHER)){
				if (flag_change <= 0) {
					StartMode = !StartMode;
					bftool = tool;
					minecraft.field_71439_g.func_146105_b(new TextComponentString(
							"Havest " + (StartMode?"ON":"OFF")));
					this.flag_change = 10;
				}
			}
		}
		if (this.flag_change >= 1) {
			this.flag_change -= 1;
		}
	}

	@SideOnly(Side.CLIENT)
	private void BlockRegister(Minecraft minecraft){
		// ブロックの登録・解除処理
		EntityPlayer player = minecraft.field_71439_g;
		ItemStack itemstk= player.func_184614_ca();
		ConfigValue.TOOLS tool = (itemstk!=null?ConfigValue.getToolKind(itemstk.func_77973_b()):ConfigValue.TOOLS.OTHER);

		if (ClientProxy.Press_Key_Regist() && (minecraft.field_71462_r == null)
				&& (tool != ConfigValue.TOOLS.OTHER)){
			if (flag_change2 <= 0) {
				// プレイヤー座標取得
		        Vec3d vecPl = new Vec3d(minecraft.field_71439_g.field_70169_q, minecraft.field_71439_g.field_70167_r + (double)minecraft.field_71439_g.func_70047_e(), minecraft.field_71439_g.field_71097_bO);
				// プレイヤー視線ベクトル取得
		        Vec3d vecEy = minecraft.field_71439_g.func_70040_Z();
		        // プレイヤー視線座標取得
				Vec3d vecPs = vecPl.func_72441_c(vecEy.field_72450_a*6.0D, vecEy.field_72448_b*6.0D, vecEy.field_72449_c*6.0D);
				// 視線の先のブロックを取得
				RayTraceResult pos = minecraft.field_71441_e.func_72933_a(vecPl, vecPs);
				if (pos != null && pos.func_178782_a() != null){
					IBlockState blkste = minecraft.field_71441_e.func_180495_p(pos.func_178782_a());
					if(blkste != null){
						boolean result = ConfigValue.addOrRemoveBlocks(itemstk.func_77973_b(), blkste);
						minecraft.field_71439_g.func_146105_b(new TextComponentString("Havest " +
						(result?"Add Block ":"Remove Block ") + blkste.func_177230_c().getRegistryName() +
						" by tool " + tool.toString()));
							this.flag_change2 = 10;
					}
				}
			}
		} else if (this.flag_change2 >= 1) {
			this.flag_change2 -= 1;
		}
	}





}
