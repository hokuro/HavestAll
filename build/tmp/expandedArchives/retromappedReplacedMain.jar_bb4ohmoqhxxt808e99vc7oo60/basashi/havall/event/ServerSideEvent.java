package basashi.havall.event;

import basashi.havall.config.ConfigValue;
import basashi.havall.core.HavestAll;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
public class ServerSideEvent {
	public ServerSideEvent() {
	}

	@SubscribeEvent
	public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
		if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
			ItemStack itmstk = event.getEntityPlayer().func_184614_ca();
			if ( itmstk == null){return;}
			Item tool = event.getEntityPlayer().func_184614_ca().func_77973_b();

			switch(ConfigValue.getToolKind(tool)){
			case SCOP:
				HavestAll.addAttackBlock(event.getPos(), false, event.getState(),ConfigValue.TOOLS.SCOP);
				break;
			case AXE:
				HavestAll.addAttackBlock(event.getPos(), false,null,ConfigValue.TOOLS.AXE);
				break;
			case PICKAXE:
				HavestAll.addAttackBlock(event.getPos(), false,null,ConfigValue.TOOLS.PICKAXE);
				break;
				default:
					break;
			}
		}
	}
}
