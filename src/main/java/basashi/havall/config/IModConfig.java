package basashi.havall.config;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public interface IModConfig{
	void init(Class<?>[] cls, FMLPreInitializationEvent event);
	boolean reloadConfig();
	int getConfigKey(String name);
}