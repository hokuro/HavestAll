package basashi.havall.core;

import java.awt.event.KeyEvent;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class CommonProxy {
	public void preInit(){}

	public static class Client extends CommonProxy{
		private static final KeyBinding KEYBINDING_ENABLE = new KeyBinding("haveall.key.switch", KeyEvent.VK_N, "haveall.key.category");

		@Override
		public void preInit(){
			InitializeKey();
		}

		@OnlyIn(Dist.CLIENT)
		public void InitializeKey(){
			ClientRegistry.registerKeyBinding(KEYBINDING_ENABLE);
		}

		@OnlyIn(Dist.CLIENT)
		public static boolean Press_Key_Enable(){
			return KEYBINDING_ENABLE.isPressed();
		}
	}

	public static class Server extends CommonProxy{

	}
}
