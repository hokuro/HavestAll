package basashi.havall.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.common.ForgeConfigSpec;

public class MyConfig {
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final General _general = new General(BUILDER);
	public static final Scop _scop = new Scop(BUILDER);
	public static final Axe _axe = new Axe(BUILDER);
	public static final PickAxe _pickAxe= new PickAxe(BUILDER);
	public static final ForgeConfigSpec spec = BUILDER.build();


	public enum TOOLS{SCOP,AXE,PICKAXE,OTHER};



	public static void reloadConfig(){
		_general.reload();
		_scop.reload();
		_axe.reload();
		_pickAxe.reload();
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

	/***************************************************************************************************/
	/*           General                                                                               */
	/***************************************************************************************************/
	public static class General extends Values{
		public static int kye_regist = 0;
		public static int key_enable = 0;

		public General(ForgeConfigSpec.Builder builder){
			builder.push("General");
			builder.pop();
		}
		@Override
		public void reload(){
		}
	}


	/***************************************************************************************************/
	/*           Scop                                                                                  */
	/***************************************************************************************************/
	public static class Scop extends Values{

		public final ForgeConfigSpec.ConfigValue<Integer> Limiter;
		public final ForgeConfigSpec.ConfigValue<Integer> Durability;
		public final ForgeConfigSpec.ConfigValue<Boolean> DestroyUnder;
		public final ForgeConfigSpec.ConfigValue<Boolean> AutoCollect;
		public final ForgeConfigSpec.ConfigValue<Boolean> DropGather;

		public final ForgeConfigSpec.ConfigValue<String> ItemIds;
		public final ForgeConfigSpec.ConfigValue<String> BlockIds;


		public Scop(ForgeConfigSpec.Builder builder){
			builder.push("Scop");
			Limiter = builder
					.comment("Destory Limit")
					.define("Limiter", 11);
			Durability = builder
					.comment("Durability")
					.define("Durability",0);
			DestroyUnder = builder
					.comment("DropGather [true/false]")
					.define("DestroyUnder", true);
			AutoCollect = builder
					.comment("Auto Collect[true/false]")
					.define("AutoCollect", true);
			DropGather = builder
					.comment("DropGather [true/false]")
					.define("DropGaher", true);
			ItemIds = builder
					.comment("enable tools ids")
					.define("ItemIds", "iron_shovel, wooden_shovel, stone_shovel, diamond_shovel, golden_shovel");
			BlockIds = builder
					.comment("target block ids")
					.define("BlockIds", "grass_block, dirt, sand, gravel, farmland, snow_layer, clay, soul_sand, mycelium, coarse_dirt");
			builder.pop();

		}

		public static List<Object> ToolId = null;
		public static List<Object> BlockId = null;

		@Override
		public void reload(){
			if (ToolId == null){
				ToolId = idStringToArray(ItemIds.get(),false);
			}
			if (BlockId == null){
				BlockId = idStringToArray(BlockIds.get(),true);
			}
		}

		@Override
		public boolean addOrRemoveBlock(IBlockState blkst){
			// TODO: ゲーム側からコンフィグを書き換える方法が不明
//			Block blk = blkst.getBlock();
//			List<Object> wblockList = BlockId;
//			int idx = BlockId.indexOf(blk);
//			if ( idx < 0){
//				// 未登録
//				if ("".equals(BlockIds)){
//					BlockIds. = blk.getRegistryName().toString();
//				}else{
//					BlockIds = BlockIds + ","+ blk.getRegistryName();
//				return true;
//			}else{
//				BlockIds="";
//				BlockId.remove(idx);
//				for (Object work : wblockList){
//					BlockIds += ((Block)work).getRegistryName()+",";
//				}
//				if(!"".equals(BlockId)){
//					BlockIds = BlockIds.substring(0,BlockIds.length()-1);
//				}
//				return false;
//			}
			return false;
		}

		@Override
		public boolean checkTool(Item tool){
			return isIdInList(tool,ToolId);
		}

		@Override
		public boolean checkBlock(IBlockState blk){
			return isIdInList(blk.getBlock(), BlockId);
		}
	}

	/***************************************************************************************************/
	/*           Axe                                                                                   */
	/***************************************************************************************************/
	public static class Axe extends Values{
		public final ForgeConfigSpec.ConfigValue<Integer> Limiter;
		public final ForgeConfigSpec.ConfigValue<Integer> Durability;
		public final ForgeConfigSpec.ConfigValue<Boolean> DropGather;
		public final ForgeConfigSpec.ConfigValue<Boolean> DestroyUnder;
		public final ForgeConfigSpec.ConfigValue<Integer> LeavesRange;
		public final ForgeConfigSpec.ConfigValue<String> ItemIds;
		public final ForgeConfigSpec.ConfigValue<String> BlockIds;
		public final ForgeConfigSpec.ConfigValue<String> NondestructiveItemIDs;
		public final ForgeConfigSpec.ConfigValue<String> LeavesIds;

		public Axe(ForgeConfigSpec.Builder builder){
			builder.push("Axe");
			Limiter = builder
					.comment("havest limitter")
					.define("Limiter", 0);
			Durability = builder
					.comment("durability")
					.define("Durability", 0);
			DropGather = builder
					.comment("DropGather [true/false]")
					.define("DropGather", true);
			DestroyUnder = builder
					.comment("Destroy Under[true/false]")
					.define("DestroyUnder", false);
			LeavesRange = builder
					.comment("leaves range")
					.define("LeavesRange", 3);
			ItemIds = builder
					.comment("enable tools ids")
					.define("ItemIds", "iron_axe, wooden_axe, stone_axe, diamond_axe, golden_axe");
			BlockIds = builder
					.comment("target block ids")
					.define("BlockIds","oak_log,spruce_log,birch_log,jungle_log,acacia_log,dark_oak_log,stripped_spruce_log,stripped_birch_log,stripped_jungle_log,stripped_acacia_log,stripped_dark_oak_log,stripped_oak_log,oak_wood,spruce_wood,birch_wood,jungle_wood,acacia_wood,dark_oak_wood,stripped_oak_wood,stripped_spruce_wood,stripped_birch_wood,stripped_jungle_wood,stripped_acacia_wood,stripped_dark_oak_wood");
			NondestructiveItemIDs = builder
					.comment("ignore block list")
					.define("NondestructiveItemIDs", "");
			LeavesIds = builder
					.comment("leave ids")
					.define("LeavesIds", "oak_leaves,spruce_leaves,birch_leaves,jungle_leaves,acacia_leaves,dark_oak_leaves");
			builder.pop();

		}

		public static List<Object> ToolId =null;
		public static List<Object> BlockId = null;
		public static List<Object> IgnoreBlockId =null;
		public static List<Object> LeaveBlockId = null;

		@Override
		public void reload(){
			if (ToolId == null){
				ToolId = idStringToArray(ItemIds.get(),false);
			}
			if (BlockId == null){
				BlockId = idStringToArray(BlockIds.get(),true);
			}
			if (IgnoreBlockId == null){
				IgnoreBlockId = idStringToArray(NondestructiveItemIDs.get(),true);
			}
			if (LeaveBlockId == null){
				LeaveBlockId = idStringToArray(LeavesIds.get(),true);
			}
		}

		public static boolean checkLeaves(Block bkl){
			return LeaveBlockId.indexOf(bkl) >= 0;
		}

		@Override
		public boolean addOrRemoveBlock(IBlockState blkst){
			// TODO : ゲーム側からコンフィグを書き換える方法が不明
//			Block blk = blkst.getBlock();
//			List<Object> wblockList = BlockId;
//			int idx = BlockId.indexOf(blk);
//			if ( idx < 0){
//				// 未登録
//				if ("".equals(BlockIds)){
//					BlockIds = blk.getRegistryName().toString();
//				}else{
//					BlockIds = BlockIds + ","+ blk.getRegistryName();
//				}
//				return true;
//			}else{
//				BlockIds="";
//				BlockId.remove(idx);
//				for (Object work : wblockList){
//					BlockIds += ((Block)work).getRegistryName()+",";
//				}
//				if(!"".equals(BlockId)){
//					BlockIds = BlockIds.substring(0,BlockIds.length()-1);
//				}
//				return false;
//			}
			return false;
		}

		@Override
		public boolean checkTool(Item tool){
			return isIdInList(tool,ToolId);
		}

		@Override
		public boolean checkBlock(IBlockState blk){
			return isIdInList(blk.getBlock(), BlockId);
		}
	}

	/***************************************************************************************************/
	/*           PickAxe                                                                               */
	/***************************************************************************************************/
	public static class PickAxe extends Values{

		public final ForgeConfigSpec.ConfigValue<Integer> Limiter;
		public final ForgeConfigSpec.ConfigValue<Boolean> AutoCollect;
		public final ForgeConfigSpec.ConfigValue<Integer> Durability;
		public final ForgeConfigSpec.ConfigValue<Boolean> DropGather;
		public final ForgeConfigSpec.ConfigValue<Boolean> DestroyUnder;
		public final ForgeConfigSpec.ConfigValue<String> ItemIds;
		public final ForgeConfigSpec.ConfigValue<String> BlockIds;

		public PickAxe(ForgeConfigSpec.Builder builder){
			builder.push("PickAxe");
			Limiter = builder
					.comment("Deltroy limit")
					.define("Limiter",  0);
			AutoCollect = builder
					.comment("Auto Collect[true/false]")
					.define("AutoCollect",  true);
			Durability = builder
					.comment("Durability")
					.define("Durability",  0);
			DropGather = builder
					.comment("Drop Gather[true/false]")
					.define("DropGather",  true);
			DestroyUnder = builder
					.comment("Destroy Under[true/false]")
					.define("DestroyUnder",  true);
			ItemIds = builder
					.comment("enable tools id")
					.define("ItemIds",  "iron_pickaxe, wooden_pickaxe, stone_pickaxe, diamond_pickaxe, golden_pickaxe");
			BlockIds = builder
					.comment("target block id")
					.define("BlockIds",  "gold_ore, iron_ore, coal_ore, lapis_ore, obsidian, diamond_ore, redstone_ore, glowstone, emerald_ore, nether_quartz_ore, granite, polished_granite, diorite, polished_diorite, andesite, polished_andesite");

			builder.pop();
		}


		public static List<Object> ToolId = null;
		public static List<Object> BlockId = null;

		@Override
		public void reload(){
			if (ToolId == null){
				ToolId = idStringToArray(ItemIds.get(),false);
			}
			if (BlockId == null){
				BlockId = idStringToArray(BlockIds.get(),true);
			}
		}


		@Override
		public boolean addOrRemoveBlock(IBlockState blk){
			// TODO : ゲーム側からコンフィグを書き換える方法が不明
//			List<Object> wblockList = BlockId;
//			int idx = BlockId.indexOf(blk);
//			if ( idx < 0){
//				// 未登録
//				String meta = "";
//				if (blk.getBlock().getMetaFromState(blk) != 0){
//					meta = ":"+Integer.toString(blk.getBlock().getMetaFromState(blk),10);
//				}
//				if ("".equals(BlockIds)){
//					BlockIds = blk.getBlock().getRegistryName() + meta;
//				}else{
//					BlockIds = BlockIds + ","+ blk.getBlock().getRegistryName() + meta;
//				}
//				return true;
//			}else{
//				BlockIds="";
//				BlockId.remove(idx);
//				for (Object work : wblockList){
//					BlockAndMetadata dat = (BlockAndMetadata)work;
//					BlockIds += dat.toString();
//				}
//				if(!"".equals(BlockId)){
//					BlockIds = BlockIds.substring(0,BlockIds.length()-1);
//				}
//				return false;
//			}
			return false;
		}

		@Override
		public boolean checkTool(Item tool){
			return isIdInList(tool,ToolId);
		}

		@Override
		public boolean checkBlock(IBlockState blk){
			//return isIdInList(new BlockAndMetadata(blk.getBlock(),0), BlockId);
			return isIdInList(blk.getBlock(), BlockId);
		}

//		public List<Object> idStringToArray(String s, boolean isBlock){
//			List<Object> list = new ArrayList();
//			String[] ss = s.split(",");
//			for (String str : ss){
//				String metastr = null;
//				if(isBlock){
//					String[] ss2 = str.split(":",2);
//					str = ss2[0];
//					if (ss2.length >= 2){
//						metastr = ss2[1];
//					}
//				}
//				Object b = null;
//				str = str.trim();
//				b = isBlock ? Block.getBlockFromName(str) : Item.getByNameOrId(str);
//				if(null != b)
//				{
//					if(isBlock){
//						if(Blocks.AIR != b){
//							Block block = (Block)b;
//							BlockAndMetadata bam = new BlockAndMetadata(block, convertMetaString(block,metastr));
//							list.add(bam);
//						}
//					}else{
//						list.add(b);
//					}
//				}
//			}
//			return list;
//		}


		private static final Pattern ptnNum = Pattern.compile("^[0-9]+$");
//		private int convertMetaString(Block b, String s) {
//			if ((null == b) || (null == s)) {
//				return -1;
//			}
//			s = s.trim();
//			if (ptnNum.matcher(s).matches()) {
//				try {
//					return Integer.parseInt(s, 10);
//				} catch (Exception localException1) {
//				}
//			}
//			Class<?> enumCls = null;
//			for (Field f : b.getClass().getDeclaredFields()) {
//				if ((0 != (f.getModifiers() & 0x1)) && (0 != (f.getModifiers() & 0x8))) {
//					if (PropertyEnum.class == f.getType()) {
//						try {
//							enumCls = ((PropertyEnum) f.get(null)).getValueClass();
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			}
//			if (null == enumCls) {
//				return -1;
//			}
//			for (Object o : enumCls.getEnumConstants()) {
//				if ((o instanceof IStringSerializable)) {
//					String name = ((IStringSerializable) o).getName();
//					if (s.equalsIgnoreCase(name)) {
//						Class<?> c = o.getClass();
//						for (Method m : c.getDeclaredMethods()) {
//							if (m.getReturnType() == Integer.TYPE) {
//								if (0 == m.getParameterTypes().length) {
//									try {
//										return ((Integer) m.invoke(o, new Object[0])).intValue();
//									} catch (Exception e) {
//										FMLLog.warning(e.toString(), new Object[0]);
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//			return -1;
//		}
	}

	public static List<Object> idStringToArray(String ids, boolean isBlock) {
		List<Object> lst = new ArrayList();
		String[] idslit = ids.split(",");
		for (String id : idslit) {
			Object blk = null;
			id = id.trim();
			if (!id.isEmpty()) {
				blk = IRegistry.field_212618_g.get(new ResourceLocation(id));
//				while(Block.BLOCK_STATE_IDS.iterator().hasNext()){
//					IBlockState state = Block.BLOCK_STATE_IDS.iterator().next();
//					ResourceLocation location = state.getBlock().getRegistryName();
//					// 完全一致か、ドメインがマインクラフトの場合名称だけ一致で判定
//					if (id.equals(location.toString()) || ("minecraft:"+id).equals(location.toString())){
//						blk = state.getBlock();
//						break;
//					}
//				}
				if (Blocks.AIR.equals(blk)){
					blk = IRegistry.field_212630_s.func_212608_b(new ResourceLocation(id));
				}
				if ((null != blk) && (Blocks.AIR != blk)) {
					lst.add(blk);
				}
			}
		}
		return lst;
	}
}
