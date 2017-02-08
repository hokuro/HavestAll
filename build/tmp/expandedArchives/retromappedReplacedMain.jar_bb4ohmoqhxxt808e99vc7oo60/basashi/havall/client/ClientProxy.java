package basashi.havall.client;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientProxy extends basashi.havall.core.CommonProxy {
	private static final KeyBinding KEYBINDING_ENABLE = new KeyBinding("haveall.key.switch", Keyboard.KEY_C, "haveall.key.category");
	private static final KeyBinding KEYBINDING_REGIST = new KeyBinding("haveall.key.regist", Keyboard.KEY_T, "haveall.key.category");

	@Override
	public void preInit(){
		InitializeKey();
	}

	@SideOnly(Side.CLIENT)
	public void InitializeKey(){
		ClientRegistry.registerKeyBinding(KEYBINDING_ENABLE);
		ClientRegistry.registerKeyBinding(KEYBINDING_REGIST);
	}

	@SideOnly(Side.CLIENT)
	public static boolean Press_Key_Enable(){
		return KEYBINDING_ENABLE.func_151468_f();
	}

	@SideOnly(Side.CLIENT)
	public static boolean Press_Key_Regist(){
		return KEYBINDING_REGIST.func_151468_f();
	}
}
