package basashi.havall.config;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import basashi.havall.core.ModCommon;
import basashi.havall.core.log.ModLog;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

public class ModConfig implements IModConfig{

	public static final ModConfig config = new ModConfig();
	protected Class<?>[] _cls =null;
	protected File _file=null;
	protected long _lastModify = 0L;
	protected long _checkTime = 0L;

	public void init(Class<?>[] cls, FMLPreInitializationEvent event){
		_cls = cls;
		_file = event.getSuggestedConfigurationFile();
		loadConfig();
	}

	public boolean reloadConfig(){
		long n = System.currentTimeMillis() - _checkTime;
		if (n < ModCommon.MOD_CONFIG_RELOAD) {
			return false;
		}
		if (!_file.isFile()) {
			return false;
		}
		if (_lastModify == _file.lastModified()) {
			return false;
		}
		loadConfig();
		_checkTime = System.currentTimeMillis();

		ModLog.log().info("config reload");
		return true;
	}

	public int getConfigKey(String name){
		if (FMLCommonHandler.instance().getSide() != Side.CLIENT) {
			return -1;
		}
		Field[] fs = null;
		try {
			Class<?> cls = Class.forName("org.lwjgl.input.Keyboard");
			fs = cls.getFields();
		} catch (ClassNotFoundException e1) {
			return -1;
		}
		if (null == fs) {
			return -1;
		}
		int key_no = 0;
		for (Field f : fs) {
			if (Modifier.isStatic(f.getModifiers())) {
				String s = f.getName();
				if (0 == s.indexOf("KEY_")) {
					if (s.equalsIgnoreCase(name)) {
						try {
							key_no = f.getInt(null);
						} catch (IllegalArgumentException localIllegalArgumentException) {
						} catch (IllegalAccessException localIllegalAccessException) {
						}
					}
				}
			}
		}
		ModLog.log().info("keyChange=" +key_no);
		return key_no;
	}

	protected void loadConfig(){
		boolean isSave = (!_file.isFile()) || (_file.length() <= 0L);
		Configuration config = new Configuration(_file);
		config.load();
		for (Class<?> cls : _cls){
			Field[] fields = cls.getFields();
			for (Field fld : fields){
				ConfigProperty prop = (ConfigProperty)fld.getAnnotation(ConfigProperty.class);
				if ( prop == null){continue;}

				if (Modifier.isStatic(fld.getModifiers())){
					Class<?> type = fld.getType();
					Property p = null;
					try {
						if (!config.hasCategory(prop.category())){
							isSave = true;
						}
						String comment=prop.comment();
						if (Integer.TYPE.equals(type)){
							p = config.get(prop.category(), fld.getName(), fld.getInt(null));
							fld.setInt(null, p.getInt());
						}else if (Double.TYPE.equals(type)){
							p = config.get(prop.category(), fld.getName(), fld.getDouble(null));
							fld.setDouble(null, p.getDouble(0.0D));
						}else if (String.class.equals(type)){
							p = config.get(prop.category(),fld.getName(),fld.get(null).toString());
							fld.set(null, p.getString());
						}else if (Boolean.TYPE.equals(type)){
							p = config.get(prop.category(),fld.getName(),fld.getBoolean(null));
							fld.setBoolean(null, p.getBoolean(fld.getBoolean(null)));
						}else{
							ModLog.log().warn("unknowntype :"+type.getCanonicalName());
						}
						if ((null != p) && (null != comment)) {
							p.setComment(comment);
						}
					} catch (IllegalArgumentException localIllegalArgumentException) {
					} catch (IllegalAccessException localIllegalAccessException) {
					}
				}
			}
		}

		if (isSave) {
			config.save();
		}
		_lastModify = _file.lastModified();
		_checkTime = System.currentTimeMillis();
	}

	public void saveConfig(){
		ModLog.log().debug("start");
		CustomConfig config = new CustomConfig(_file);
		config.load();
		for (Class<?> cls : _cls){
			Field[] fields = cls.getFields();
			for (Field fld : fields){
				ConfigProperty prop = (ConfigProperty)fld.getAnnotation(ConfigProperty.class);
				if ( prop == null){continue;}

				if (Modifier.isStatic(fld.getModifiers())){
					Class<?> type = fld.getType();
					Property p = null;
					try {
						String comment=prop.comment();
						if (Integer.TYPE.equals(type)){
							p = config.get(prop.category(), fld.getName(), fld.getInt(null));
							if(prop.isSave()) config.set(prop.category(), fld.getName(), fld.getInt(null));
							ModLog.log().debug("config Int : "+prop.category() + ":" + fld.getName() + " : value ="+fld.getInt(null));
						}else if (Double.TYPE.equals(type)){
							p = config.get(prop.category(), fld.getName(), fld.getDouble(null));
							if(prop.isSave()) config.set(prop.category(), fld.getName(), fld.getDouble(null));
							ModLog.log().debug("config Double : "+prop.category() + ":" + fld.getName() + " : value ="+fld.getDouble(null));
						}else if (String.class.equals(type)){
							p = config.get(prop.category(),fld.getName(),fld.get(null).toString());
							if(prop.isSave()) config.set(prop.category(), fld.getName(), fld.get(null).toString());
							ModLog.log().debug("config String : "+prop.category() + ":" + fld.getName() + " : value ="+fld.toString());
						}else if (Boolean.TYPE.equals(type)){
							p = config.get(prop.category(),fld.getName(),fld.getBoolean(null));
							if(prop.isSave()) config.set(prop.category(), fld.getName(), fld.getBoolean(null));
							ModLog.log().debug("config Bool : "+prop.category() + ":" + fld.getName() + " : value ="+fld.getBoolean(null));
						}else{
							ModLog.log().warn("unknowntype :"+type.getCanonicalName());
						}
						if ((null != p) && (null != comment)) {
							p.setComment(comment);
						}
					} catch (IllegalArgumentException localIllegalArgumentException) {
					} catch (IllegalAccessException localIllegalAccessException) {
					}
				}
			}
		}
		config.save();
		_lastModify = _file.lastModified();
		_checkTime = System.currentTimeMillis();

		ModLog.log().debug("end");
	}
}