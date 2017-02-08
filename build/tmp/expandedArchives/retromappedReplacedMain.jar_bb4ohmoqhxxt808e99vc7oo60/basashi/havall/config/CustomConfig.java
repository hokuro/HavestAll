package basashi.havall.config;

import java.io.File;
import java.util.Map;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Property.Type;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class CustomConfig extends Configuration{

	public CustomConfig(File _file){
		super(_file);
	}

	public void set(String category, String key, int value){
		set(category,key,value,Type.INTEGER);
	}
	public void set(String category, String key, int[] value){
		set(category,key,value,Type.INTEGER);
	}
	public void set(String category, String key, double value){
		set(category,key,value,Type.DOUBLE);
	}
	public void set(String category, String key, double[] value){
		set(category,key,value,Type.DOUBLE);
	}
	public void set(String category, String key, boolean value){
		set(category,key,value,Type.BOOLEAN);}
	public void set(String category, String key, boolean[] value){
		set(category,key,value,Type.BOOLEAN);
	}
	public void set(String category, String key, String value){
		set(category,key,value,Type.STRING);
	}
	public void set(String category, String key, String[] value){
		set(category,key,value,Type.STRING);
	}

	public void set(String category, String key, Object value, Property.Type type)
	{
		String strVal = value!=null?String.valueOf(value):"";

		ConfigCategory conf = getCategory(category);
		Map<String, Property> properties;
		Property prop = new Property(key, strVal, type);
		if (conf.containsKey(key))
		{
			properties = ObfuscationReflectionHelper.getPrivateValue(ConfigCategory.class, conf, "properties");
			properties.put(key, prop);
		}
	}

	public void set(String category, String key, Object[] lst, Property.Type type)
	{
		String[] strList;
		if (lst == null){
			strList = new String[0];
		}else{
			strList = new String[lst.length];
			for(int i =0; i< lst.length;i++){
					strList[i] = String.valueOf(lst[i]);
			}
		}
		ConfigCategory conf = getCategory(category);
		Map<String, Property> properties;
		Property prop = new Property(key, strList, type);
		if (conf.containsKey(key))
		{
			properties = ObfuscationReflectionHelper.getPrivateValue(ConfigCategory.class, conf, "properties");
			properties.put(key, prop);
		}
	}


}
