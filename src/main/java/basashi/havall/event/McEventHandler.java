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
			World world = minecraft.theWorld;
			if (null != world) {
				for (Iterator<Packet_HavestBase> i = attackHistory.iterator(); i.hasNext();) {
					Packet_HavestBase pkt = (Packet_HavestBase) i.next();
					if (System.nanoTime() - pkt.nanoTime >= 3000000000L) {
						i.remove();
					} else {
						CPacketCustomPayload packet;
						IBlockState blockID1 = world.getBlockState(pkt._pos);
						if ((null == blockID1) || (Blocks.air == blockID1.getBlock())) {
							i.remove();
							Item tool = (null==minecraft.thePlayer.getHeldItemMainhand())?null:minecraft.thePlayer.getHeldItemMainhand().getItem();
							try{
								packet = getHavestInstance(ConfigValue.getToolKind(tool)).getServerPacket(pkt, world);
							}catch(Exception exe){
								packet = null;
							}
							if (packet != null){
								minecraft.getNetHandler().addToSendQueue(packet);
							}
						}
					}
				}
			}
		}
		if ( minecraft.theWorld != null){
			// ツールの有効無効処理
			ToolEnable(minecraft);
			// ブロックの登録
			BlockRegister(minecraft);
		}
	}

	@SubscribeEvent
	public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
		EntityPlayer player = ((NetHandlerPlayServer) event.getHandler()).playerEntity;
		ItemStack itmstk = player.getHeldItemMainhand();
		ConfigValue.TOOLS tool;
		if( (itmstk != null) &&((tool = ConfigValue.getToolKind(itmstk.getItem()))!= ConfigValue.TOOLS.OTHER) ){
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

			p._player = ((NetHandlerPlayServer) event.getHandler()).playerEntity;
			p.readPacketData(event.getPacket().payload().array());
			if (p._player.getDistance(p._pos.getX(), p._pos.getY(), p._pos.getZ()) > 6.0D) {
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
		ItemStack itmstk = p._player.getHeldItemMainhand();
		ConfigValue.TOOLS tool;
		if ((itmstk != null) && ((tool = ConfigValue.getToolKind(itmstk.getItem()))!= ConfigValue.TOOLS.OTHER)){
			getHavestInstance(tool).startHavest(p, p._player);
		}
	}

	@SideOnly(Side.CLIENT)
	public static void addAttackBlock(BlockPos pos, boolean clearHistory, IBlockState block, ConfigValue.TOOLS tool) {
		IHavest instance = getHavestInstance(tool);
		if (clearHistory) {attackHistory.clear();}
		if (instance == null){return;}

		World world = FMLClientHandler.instance().getClient().theWorld;
		EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
		if (null == block) {
			block = world.getBlockState(pos);
		}
		if (!instance.isRun(pos)){return;}
		Item equipedItem = null == player.getHeldItemMainhand() ? null : player.getHeldItemMainhand().getItem();

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
		EntityPlayer player = minecraft.thePlayer;
		ItemStack itemstk= player.getHeldItemMainhand();
		ConfigValue.TOOLS tool = (itemstk!=null?ConfigValue.getToolKind(itemstk.getItem()):ConfigValue.TOOLS.OTHER);
		if (StartMode && ((tool == ConfigValue.TOOLS.OTHER) || (bftool != tool))){
				StartMode = false;
				bftool = tool;
				player.addChatComponentMessage(new TextComponentString("Havest OFF"));
		}else{
			if (ClientProxy.Press_Key_Enable() && (minecraft.currentScreen == null)
					&& (tool != ConfigValue.TOOLS.OTHER)){
				if (flag_change <= 0) {
					StartMode = !StartMode;
					bftool = tool;
					minecraft.thePlayer.addChatComponentMessage(new TextComponentString(
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
		EntityPlayer player = minecraft.thePlayer;
		ItemStack itemstk= player.getHeldItemMainhand();
		ConfigValue.TOOLS tool = (itemstk!=null?ConfigValue.getToolKind(itemstk.getItem()):ConfigValue.TOOLS.OTHER);

		if (ClientProxy.Press_Key_Regist() && (minecraft.currentScreen == null)
				&& (tool != ConfigValue.TOOLS.OTHER)){
			if (flag_change2 <= 0) {
				// プレイヤー座標取得
		        Vec3d vecPl = new Vec3d(minecraft.thePlayer.prevPosX, minecraft.thePlayer.prevPosY + (double)minecraft.thePlayer.getEyeHeight(), minecraft.thePlayer.prevChasingPosZ);
				// プレイヤー視線ベクトル取得
		        Vec3d vecEy = minecraft.thePlayer.getLookVec();
		        // プレイヤー視線座標取得
				Vec3d vecPs = vecPl.addVector(vecEy.xCoord*6.0D, vecEy.yCoord*6.0D, vecEy.zCoord*6.0D);
				// 視線の先のブロックを取得
				RayTraceResult pos = minecraft.theWorld.rayTraceBlocks(vecPl, vecPs);
				if (pos != null && pos.getBlockPos() != null){
					IBlockState blkste = minecraft.theWorld.getBlockState(pos.getBlockPos());
					if(blkste != null){
						boolean result = ConfigValue.addOrRemoveBlocks(itemstk.getItem(), blkste);
						minecraft.thePlayer.addChatComponentMessage(new TextComponentString("Havest " +
						(result?"Add Block ":"Remove Block ") + blkste.getBlock().getRegistryName() +
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
