package basashi.havall.config;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;

public class Values {

	public void reload(){}

	public boolean checkTool(Item tool){return false;}
	public boolean checkBlock(BlockState blk){return false;}
	public boolean addOrRemoveBlock(BlockState blk){return false;}

	protected boolean isIdInList(Object o, List<Object> list) {
		return list.indexOf(o) >= 0;
	}
}
