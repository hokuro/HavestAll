package basashi.havall.event;

import basashi.havall.config.MyConfig;
import basashi.havall.core.HavestAll;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
public class ServerSideEvent {
	public ServerSideEvent() {
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
