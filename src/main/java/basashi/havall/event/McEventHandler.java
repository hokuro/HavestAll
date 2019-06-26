package basashi.havall.event;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import basashi.havall.client.ClientProxy;
import basashi.havall.config.MyConfig;
import basashi.havall.core.HavestAll;
import basashi.havall.network.Message_Packet;
import basashi.havall.network.Packet_Axe;
import basashi.havall.network.Packet_HavestBase;
import basashi.havall.network.Packet_PickAxe;
import basashi.havall.network.Packet_Scop;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

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
	public MyConfig.TOOLS bftool = MyConfig.TOOLS.OTHER;

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void tickEvent(TickEvent.ClientTickEvent event) {
		if (!TickEvent.Phase.END.equals(event.phase)) {
			return;
		}

		// ワールド情報、プレイヤー情報を取得
		Minecraft minecraft = Minecraft.getInstance();
		World world = minecraft.world;
		EntityPlayerSP player = minecraft.player;
		if (null == player) {
			return;
		}
		MyConfig.reloadConfig();

		if (!StartMode) {
			attackHistory.clear();
		} else {
			if (null != world) {
				for (Iterator<Packet_HavestBase> i = attackHistory.iterator(); i.hasNext();) {
					Packet_HavestBase pkt = (Packet_HavestBase) i.next();
					if (System.nanoTime() - pkt.nanoTime >= 3000000000L) {
						i.remove();
					} else {
						Message_Packet packet;
						IBlockState blockID1 = world.getBlockState(pkt._pos);
						if ((null == blockID1) || (Blocks.AIR == blockID1.getBlock())) {
							i.remove();
							Item tool = (null==minecraft.player.getHeldItemMainhand())?null:minecraft.player.getHeldItemMainhand().getItem();
							try{
								packet = getHavestInstance(MyConfig.getToolKind(tool)).getServerPacket(pkt, world);
							}catch(Exception exe){
								packet = null;
							}
							if (packet != null){
								HavestAll.HANDLER.sendToServer(packet);
								///minecraft.getConnection().sendPacket(packet);
							}
						}
					}
				}
			}
		}
		if ( minecraft.world != null){
			// ツールの有効無効処理
			ToolEnable(minecraft);
			// ブロックの登録
			BlockRegister(minecraft);
		}
	}

	public static void onServerPacket(Message_Packet pkt, EntityPlayer player) {
		ItemStack itmstk = player.getHeldItemMainhand();
		MyConfig.TOOLS tool;
		if( (itmstk != null) &&((tool = MyConfig.getToolKind(itmstk.getItem()))!= MyConfig.TOOLS.OTHER) ){
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

			p._player = player;
			p.readPacketData(pkt.Data());
			if (p._player.getDistance(p._pos.getX(), p._pos.getY(), p._pos.getZ()) > 6.0D) {
				return;
			}
			_serverPacket.offer(p);
		}
	}
//	@SubscribeEvent
//	public void onServerPacket(NetworkEvent.ServerCustomPayloadEvent event) {
//		EntityPlayer player = event.getSource().get().getSender();
//		ItemStack itmstk = player.getHeldItemMainhand();
//		MyConfig.TOOLS tool;
//		if( (itmstk != null) &&((tool = MyConfig.getToolKind(itmstk.getItem()))!= MyConfig.TOOLS.OTHER) ){
//			Packet_HavestBase p;
//			switch(tool){
//			case SCOP:
//				p = new Packet_Scop();
//				break;
//			case AXE:
//				p = new Packet_Axe();
//				break;
//			case PICKAXE:
//				p = new Packet_PickAxe();
//				break;
//				default:
//					p=new Packet_HavestBase();
//				break;
//			}
//
//			p._player = event.getSource().get().getSender();
//			p.readPacketData(event.getPayload().array());
//			if (p._player.getDistance(p._pos.getX(), p._pos.getY(), p._pos.getZ()) > 6.0D) {
//				return;
//			}
//			_serverPacket.offer(p);
//		}
//	}

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
		MyConfig.TOOLS tool;
		if ((itmstk != null) && ((tool = MyConfig.getToolKind(itmstk.getItem()))!= MyConfig.TOOLS.OTHER)){
			getHavestInstance(tool).startHavest(p, p._player);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void addAttackBlock(BlockPos pos, boolean clearHistory, IBlockState block, MyConfig.TOOLS tool) {
		IHavest instance = getHavestInstance(tool);
		if (clearHistory) {attackHistory.clear();}
		if (instance == null){return;}

		World world = Minecraft.getInstance().world;
		EntityPlayer player = Minecraft.getInstance().player;
		if (null == block) {
			block = world.getBlockState(pos);
		}
		if (!instance.isRun(pos)){return;}
		Item equipedItem = null == player.getHeldItemMainhand() ? null : player.getHeldItemMainhand().getItem();

		boolean blAdd = MyConfig.CheckHavest(equipedItem, block);
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

	private static IHavest getHavestInstance(MyConfig.TOOLS tool){
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

	@OnlyIn(Dist.CLIENT)
	private void ToolEnable(Minecraft minecraft){
		// ツールの有効無効処理
		EntityPlayer player = minecraft.player;
		ItemStack itemstk= player.getHeldItemMainhand();
		MyConfig.TOOLS tool = (itemstk!=null?MyConfig.getToolKind(itemstk.getItem()):MyConfig.TOOLS.OTHER);
		if (StartMode && ((tool == MyConfig.TOOLS.OTHER) || (bftool != tool))){
				StartMode = false;
				bftool = tool;
				player.sendStatusMessage(new TextComponentString("Havest OFF"),false);
		}else{
			if (ClientProxy.Press_Key_Enable() && (minecraft.currentScreen == null)
					&& (tool != MyConfig.TOOLS.OTHER)){
				if (flag_change <= 0) {
					StartMode = !StartMode;
					bftool = tool;
					minecraft.player.sendStatusMessage(new TextComponentString(
							"Havest " + (StartMode?"ON":"OFF")),false);
					this.flag_change = 10;
				}
			}
		}
		if (this.flag_change >= 1) {
			this.flag_change -= 1;
		}
	}

	@OnlyIn(Dist.CLIENT)
	private void BlockRegister(Minecraft minecraft){
//		// ブロックの登録・解除処理
//		EntityPlayer player = minecraft.player;
//		ItemStack itemstk= player.getHeldItemMainhand();
//		MyConfig.TOOLS tool = (itemstk!=null?MyConfig.getToolKind(itemstk.getItem()):MyConfig.TOOLS.OTHER);
//
//		if (ClientProxy.Press_Key_Regist() && (minecraft.currentScreen == null)
//				&& (tool != MyConfig.TOOLS.OTHER)){
//			if (flag_change2 <= 0) {
//				// プレイヤー座標取得
//		        Vec3d vecPl = new Vec3d(minecraft.player.prevPosX, minecraft.player.prevPosY + (double)minecraft.player.getEyeHeight(), minecraft.player.prevChasingPosZ);
//				// プレイヤー視線ベクトル取得
//		        Vec3d vecEy = minecraft.player.getLookVec();
//		        // プレイヤー視線座標取得
//				Vec3d vecPs = vecPl.addVector(vecEy.x*6.0D, vecEy.y*6.0D, vecEy.z*6.0D);
//				// 視線の先のブロックを取得
//				RayTraceResult pos = minecraft.world.rayTraceBlocks(vecPl, vecPs);
//				if (pos != null && pos.getBlockPos() != null){
//					IBlockState blkste = minecraft.world.getBlockState(pos.getBlockPos());
//					if(blkste != null){
//						boolean result = MyConfig.addOrRemoveBlocks(itemstk.getItem(), blkste);
//						minecraft.player.sendStatusMessage(new TextComponentString("Havest " +
//						(result?"Add Block ":"Remove Block ") + blkste.getBlock().getRegistryName() +
//						" by tool " + tool.toString()),false);
//							this.flag_change2 = 10;
//					}
//				}
//			}
//		} else if (this.flag_change2 >= 1) {
//			this.flag_change2 -= 1;
//		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
			ItemStack itmstk = event.getEntityPlayer().getHeldItemMainhand();
			if ( itmstk == null){return;}
			Item tool = event.getEntityPlayer().getHeldItemMainhand().getItem();

			switch(MyConfig.getToolKind(tool)){
			case SCOP:
				HavestAll.addAttackBlock(event.getPos(), false, event.getState(),MyConfig.TOOLS.SCOP);
				break;
			case AXE:
				HavestAll.addAttackBlock(event.getPos(), false,null,MyConfig.TOOLS.AXE);
				break;
			case PICKAXE:
				HavestAll.addAttackBlock(event.getPos(), false,null,MyConfig.TOOLS.PICKAXE);
				break;
				default:
					break;
			}
	}



}
