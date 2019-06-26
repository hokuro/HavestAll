package basashi.havall.core;

import basashi.havall.config.MyConfig;
import basashi.havall.event.McEventHandler;
import basashi.havall.network.Message_Packet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

@Mod(ModCommon.MOD_ID)
public class HavestAll {
	public static final McEventHandler mcEvent = new McEventHandler();
	private static CommonProxy proxy;
	private static final String PROTOCOL_VERSION = Integer.toString(1);
	public static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(ModCommon.MOD_ID, ModCommon.MOD_CHANEL))
			.clientAcceptedVersions(PROTOCOL_VERSION::equals)
			.serverAcceptedVersions(PROTOCOL_VERSION::equals)
			.networkProtocolVersion(() -> PROTOCOL_VERSION).simpleChannel();

    public HavestAll() {
    	ModLoadingContext.get().
        registerConfig(
        		net.minecraftforge.fml.config.ModConfig.Type.COMMON,
        		MyConfig.spec);

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

    	//event_instance = new EventHook();
    	MinecraftForge.EVENT_BUS.register( mcEvent);
    	int disc = 0;
		HANDLER.registerMessage(disc++, Message_Packet.class, Message_Packet::encode, Message_Packet::decode, Message_Packet.Handler::handle);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        proxy = new CommonProxy.Client();
        proxy.preInit();
    }

	@OnlyIn(Dist.CLIENT)
	public static void addAttackBlock(BlockPos pos, boolean clearHistory, IBlockState state, MyConfig.TOOLS tool){
		McEventHandler.addAttackBlock(pos,clearHistory,state,tool);
	}




}
