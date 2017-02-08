package basashi.havall.core;

import net.minecraft.block.Block;

public class BlockAndMetadata {
	private final Block _block;
	private final int _meta;

	private BlockAndMetadata() {
		this._block = null;
		this._meta = -1;
	}

	public BlockAndMetadata(Block b, int meta) {
		this._block = b;
		this._meta = meta;
	}

	public String toString() {
		if (null == this._block) {
			return "(null)";
		}
		String s = _block.getRegistryName() + ((_meta==0)?"":Integer.toString(_meta));
		return s;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (null == obj) {
			return false;
		}
		if (!(obj instanceof BlockAndMetadata)) {
			return false;
		}
		BlockAndMetadata bam = (BlockAndMetadata) obj;
		if (this._block != bam._block) {
			return false;
		}
		if ((this._meta < 0) || (bam._meta < 0)) {
			return true;
		}
		return this._meta == bam._meta;
	}
}
