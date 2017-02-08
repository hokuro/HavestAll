package basashi.havall.core;

import basashi.havall.config.ConfigValue;
import basashi.havall.event.McEventHandler;
import basashi.havall.event.ServerSideEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = ModCommon.MOD_ID, name = ModCommon.MOD_NAME, version = ModCommon.MOD_VERSION)
public class HavestAll {
	@Mod.Instance(ModCommon.MOD_ID)
	public static HavestAll instance;
	@SidedProxy(clientSide = ModCommon.MOD_PACKAGE + ModCommon.MOD_CLIENT_SIDE, serverSide = ModCommon.MOD_PACKAGE + ModCommon.MOD_SERVER_SIDE)
	public static CommonProxy proxy;
	public static final McEventHandler mcEvent = new McEventHandler();

	@EventHandler
	public void construct(FMLConstructionEvent event) {
		ModCommon.isDebug = true;
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ConfigValue.init(event);
		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		FMLEventChannel e = NetworkRegistry.INSTANCE.newEventDrivenChannel(ModCommon.MOD_CHANEL);
		e.register(mcEvent);
		if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
			MinecraftForge.EVENT_BUS.register(new ServerSideEvent());
		}
		MinecraftForge.EVENT_BUS.register(mcEvent);
	}

	@SideOnly(Side.CLIENT)
	public static void addAttackBlock(BlockPos pos, boolean clearHistory, IBlockState state, ConfigValue.TOOLS tool){
		McEventHandler.addAttackBlock(pos,clearHistory,state,tool);
	}




}
