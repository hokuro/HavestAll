package basashi.havall.config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import basashi.havall.core.BlockAndMetadata;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ConfigValue {
	public enum TOOLS{SCOP,AXE,PICKAXE,OTHER};

	private static Class<?>[] cls = new Class<?>[]{General.class,
		Scop.class,
		Axe.class,
		PickAxe.class};

	private static General _general;
	private static Scop _scop;
	private static Axe _axe;
	private static PickAxe _pickAxe;

	public static void init(FMLPreInitializationEvent event){
		ModConfig.config.init(cls, event);
		_general = new General();
		_scop = new Scop();
		_axe = new Axe();
		_pickAxe = new PickAxe();

		_general.reload();
		_scop.reload();
		_axe.reload();
		_pickAxe.reload();
	}

	public static void reloadConfig(){
		if( ModConfig.config.reloadConfig()){
			_general.reload();
			_scop.reload();
			_axe.reload();
			_pickAxe.reload();
		}
	}

	public static void saveConfig(){
		ModConfig.config.saveConfig();
	}

	public static TOOLS getToolKind(Item toolName){
		if (_scop.checkTool(toolName)){
			return TOOLS.SCOP;
		}else if(_axe.checkTool(toolName)){
			return TOOLS.AXE;
		}else if(_pickAxe.checkTool(toolName)){
			return TOOLS.PICKAXE;
		}else{
			return TOOLS.OTHER;
		}
	}

	private static Values getToolClass(Item toolName){
		try{
		if (_scop.checkTool(toolName)){
			return _scop;
		}else if(_axe.checkTool(toolName)){
			return _axe;
		}else if(_pickAxe.checkTool(toolName)){
			return _pickAxe;
		}else{
			return null;
		}
		}catch(Exception ex){
			return null;
		}
	}

	public static boolean CheckHavest(Item tool, IBlockState blk){
		Values ins = getToolClass(tool);
		if ( ins != null){
			return ins.checkBlock(blk);
		}else{
			return false;
		}
	}

	public static boolean addOrRemoveBlocks(Item tool, IBlockState blk){
		Values ins = getToolClass(tool);
		if( ins != null){
			boolean ret =ins.addOrRemoveBlock(blk);
			ins.reload();
			saveConfig();
			return ret;
		}else{
			return false;
		}
	}

	/***************************************************************************************************/
	/*           General                                                                               */
	/***************************************************************************************************/
	public static class General extends Values{
		public static int kye_regist = 0;
		public static int key_enable = 0;

		@ConfigProperty(comment="switch enable key")
		public static String EnableKey = "KEY_C";
		@ConfigProperty(comment="switch registitem key")
		public static String RegistKey = "KEY_I";

		@Override
		public void reload(){
			kye_regist = ModConfig.config.getConfigKey(RegistKey);
			key_enable = ModConfig.config.getConfigKey(EnableKey);
		}
	}


	/***************************************************************************************************/
	/*           Scop                                                                                  */
	/***************************************************************************************************/
	public static class Scop extends Values{
		@ConfigProperty(category="scop",comment="Destory Limit")
		public static int Limiter = 11;
		@ConfigProperty(category="scop",comment="Durability")
		public static int Durability = 0;
		@ConfigProperty(category="scop",comment="DropGather [true/false]")
		public static boolean DestroyUnder = true;
		@ConfigProperty(category="scop",comment="Auto Collect[true/false]")
		public static boolean AutoCollect = false;
		@ConfigProperty(category="scop",comment="DropGather [true/false]")
		public static boolean DropGather = false;

		@ConfigProperty(category="scop",comment="enable tools ids")
		public static String ItemIds = "iron_shovel, wooden_shovel, stone_shovel, diamond_shovel, golden_shovel";
		@ConfigProperty(category="scop",comment="target block ids", isSave=true)
		public static String BlockIds = "grass, dirt, sand, gravel, farmland, snow_layer, clay, soul_sand, mycelium";
		public static List<Object> ToolId = new ArrayList<Object>();
		public static List<Object> BlockId = new ArrayList<Object>();

		@Override
		public void reload(){
			ToolId = idStringToArray(ItemIds,false);
			BlockId = idStringToArray(BlockIds,true);
		}

		@Override
		public boolean addOrRemoveBlock(IBlockState blkst){
			Block blk = blkst.func_177230_c();
			List<Object> wblockList = BlockId;
			int idx = BlockId.indexOf(blk);
			if ( idx < 0){
				// 未登録
				if ("".equals(BlockIds)){
					BlockIds = blk.getRegistryName().toString();
				}else{
					BlockIds = BlockIds + ","+ blk.getRegistryName();
				}
				return true;
			}else{
				BlockIds="";
				BlockId.remove(idx);
				for (Object work : wblockList){
					BlockIds += ((Block)work).getRegistryName()+",";
				}
				if(!"".equals(BlockId)){
					BlockIds = BlockIds.substring(0,BlockIds.length()-1);
				}
				return false;
			}
		}

		@Override
		public boolean checkTool(Item tool){
			return isIdInList(tool,ToolId);
		}

		@Override
		public boolean checkBlock(IBlockState blk){
			return isIdInList(blk.func_177230_c(), BlockId);
		}
	}

	/***************************************************************************************************/
	/*           Axe                                                                                   */
	/***************************************************************************************************/
	public static class Axe extends Values{
		@ConfigProperty(category="axe",comment="havest limitter")
		public static int Limiter = 0;
		@ConfigProperty(category="axe",comment="durability")
		public static int Durability = 0;
		@ConfigProperty(category="axe",comment="DropGather [true/false]")
		public static boolean DropGather = true;
		@ConfigProperty(category="axe",comment="Destroy Under[true/false]")
		public static boolean DestroyUnder = false;
		@ConfigProperty(category="axe",comment="leaves range")
		public static int LeavesRange = 3;
		@ConfigProperty(category="axe",comment="enable tools ids")
		public static String ItemIds = "iron_axe, wooden_axe, stone_axe, diamond_axe, golden_axe";
		@ConfigProperty(category="axe",comment="target block ids", isSave=true)
		public static String BlockIds = "log, brown_mushroom_block, red_mushroom_block, log2";
		@ConfigProperty(category="axe",comment="ignore block list")
		public static String NondestructiveItemIDs = "";
		@ConfigProperty(category="axe",comment="leave ids")
		public static String LeavesIds = "";

		public static List<Object> ToolId = new ArrayList<Object>();
		public static List<Object> BlockId = new ArrayList<Object>();
		public static List<Object> IgnoreBlockId = new ArrayList<Object>();
		public static List<Object> LeaveBlockId = new ArrayList<Object>();

		@Override
		public void reload(){
			ToolId = idStringToArray(ItemIds,false);
			BlockId = idStringToArray(BlockIds,true);
			IgnoreBlockId = idStringToArray(NondestructiveItemIDs,true);
			LeaveBlockId = idStringToArray(LeavesIds,true);
		}

		public static boolean checkLeaves(Block bkl){
			return LeaveBlockId.indexOf(bkl) >= 0;
		}

		@Override
		public boolean addOrRemoveBlock(IBlockState blkst){
			Block blk = blkst.func_177230_c();
			List<Object> wblockList = BlockId;
			int idx = BlockId.indexOf(blk);
			if ( idx < 0){
				// 未登録
				if ("".equals(BlockIds)){
					BlockIds = blk.getRegistryName().toString();
				}else{
					BlockIds = BlockIds + ","+ blk.getRegistryName();
				}
				return true;
			}else{
				BlockIds="";
				BlockId.remove(idx);
				for (Object work : wblockList){
					BlockIds += ((Block)work).getRegistryName()+",";
				}
				if(!"".equals(BlockId)){
					BlockIds = BlockIds.substring(0,BlockIds.length()-1);
				}
				return false;
			}
		}

		@Override
		public boolean checkTool(Item tool){
			return isIdInList(tool,ToolId);
		}

		@Override
		public boolean checkBlock(IBlockState blk){
			return isIdInList(blk.func_177230_c(), BlockId);
		}
	}

	/***************************************************************************************************/
	/*           PickAxe                                                                               */
	/***************************************************************************************************/
	public static class PickAxe extends Values{
		@ConfigProperty(category="pickaxe",comment="Deltroy limit")
		public static int Limiter = 0;
		@ConfigProperty(category="pickaxe",comment="Auto Collect[true/false]")
		public static boolean AutoCollect = false;
		@ConfigProperty(category="pickaxe",comment="Durability")
		public static int Durability = 0;
		@ConfigProperty(category="pickaxe",comment="Drop Gather[true/false]")
		public static boolean DropGather = false;
		@ConfigProperty(category="pickaxe",comment="Destroy Under[true/false]")
		public static boolean DestroyUnder = true;

		@ConfigProperty(category="pickaxe",comment="enable tools id")
		public static String ItemIds = "iron_pickaxe, wooden_pickaxe, stone_pickaxe, diamond_pickaxe, golden_pickaxe";
		@ConfigProperty(category="pickaxe",comment="target block id", isSave=true)
		public static String BlockIds = "gold_ore, iron_ore, coal_ore, lapis_ore, obsidian, diamond_ore, redstone_ore, lit_redstone_ore, glowstone, emerald_ore, quartz_ore, stone:granite, stone:smooth_granite, stone:diorite, stone:smooth_diorite, stone:andesite, stone:smooth_andesite";
		public static List<Object> ToolId = new ArrayList<Object>();
		public static List<Object> BlockId = new ArrayList<Object>();

		@Override
		public void reload(){
			ToolId = this.idStringToArray(ItemIds,false);
			BlockId = this.idStringToArray(BlockIds,true);
		}

		@Override
		public boolean addOrRemoveBlock(IBlockState blk){
			List<Object> wblockList = BlockId;
			int idx = BlockId.indexOf(blk);
			if ( idx < 0){
				// 未登録
				String meta = "";
				if (blk.func_177230_c().func_176201_c(blk) != 0){
					meta = ":"+Integer.toString(blk.func_177230_c().func_176201_c(blk),10);
				}
				if ("".equals(BlockIds)){
					BlockIds = blk.func_177230_c().getRegistryName() + meta;
				}else{
					BlockIds = BlockIds + ","+ blk.func_177230_c().getRegistryName() + meta;
				}
				return true;
			}else{
				BlockIds="";
				BlockId.remove(idx);
				for (Object work : wblockList){
					BlockAndMetadata dat = (BlockAndMetadata)work;
					BlockIds += dat.toString();
				}
				if(!"".equals(BlockId)){
					BlockIds = BlockIds.substring(0,BlockIds.length()-1);
				}
				return false;
			}
		}

		@Override
		public boolean checkTool(Item tool){
			return isIdInList(tool,ToolId);
		}

		@Override
		public boolean checkBlock(IBlockState blk){
			return isIdInList(new BlockAndMetadata(blk.func_177230_c(),blk.func_177230_c().func_176201_c(blk)), BlockId);
		}

		public List<Object> idStringToArray(String s, boolean isBlock){
			List<Object> list = new ArrayList();
			String[] ss = s.split(",");
			for (String str : ss){
				String metastr = null;
				if(isBlock){
					String[] ss2 = str.split(":",2);
					str = ss2[0];
					if (ss2.length >= 2){
						metastr = ss2[1];
					}
				}
				Object b = null;
				str = str.trim();
				b = isBlock ? Block.func_149684_b(str) : Item.func_111206_d(str);
				if(null != b)
				{
					if(isBlock){
						if(Blocks.field_150350_a != b){
							Block block = (Block)b;
							BlockAndMetadata bam = new BlockAndMetadata(block, convertMetaString(block,metastr));
							list.add(bam);
						}
					}else{
						list.add(b);
					}
				}
			}
			return list;
		}


		private static final Pattern ptnNum = Pattern.compile("^[0-9]+$");
		private int convertMetaString(Block b, String s) {
			if ((null == b) || (null == s)) {
				return -1;
			}
			s = s.trim();
			if (ptnNum.matcher(s).matches()) {
				try {
					return Integer.parseInt(s, 10);
				} catch (Exception localException1) {
				}
			}
			Class<?> enumCls = null;
			for (Field f : b.getClass().getDeclaredFields()) {
				if ((0 != (f.getModifiers() & 0x1)) && (0 != (f.getModifiers() & 0x8))) {
					if (PropertyEnum.class == f.getType()) {
						try {
							enumCls = ((PropertyEnum) f.get(null)).func_177699_b();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			if (null == enumCls) {
				return -1;
			}
			for (Object o : enumCls.getEnumConstants()) {
				if ((o instanceof IStringSerializable)) {
					String name = ((IStringSerializable) o).func_176610_l();
					if (s.equalsIgnoreCase(name)) {
						Class<?> c = o.getClass();
						for (Method m : c.getDeclaredMethods()) {
							if (m.getReturnType() == Integer.TYPE) {
								if (0 == m.getParameterTypes().length) {
									try {
										return ((Integer) m.invoke(o, new Object[0])).intValue();
									} catch (Exception e) {
										FMLLog.warning(e.toString(), new Object[0]);
									}
								}
							}
						}
					}
				}
			}
			return -1;
		}
	}

	public static List<Object> idStringToArray(String ids, boolean isBlock) {
		List<Object> lst = new ArrayList();
		String[] idslit = ids.split(",");
		for (String id : idslit) {
			Object blk = null;
			id = id.trim();
			if (!id.isEmpty()) {
				blk = isBlock ? Block.func_149684_b(id) : Item.func_111206_d(id);
				if (null == blk) {
					id = "minecraft:" + id;
					blk = isBlock ? Block.func_149684_b(id) : Item.func_111206_d(id);
				}
				if ((null != blk) && (Blocks.field_150350_a != blk)) {
					lst.add(blk);
				}
			}
		}
		return lst;
	}
}
