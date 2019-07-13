package miragefairy2019.mod.lib.multi;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemVariant
{

	public final String registryName;
	public final String oreName;

	private int metadata = 0;
	private Item item = null;

	public ItemVariant(String registryName, String oreName)
	{
		this.registryName = registryName;
		this.oreName = oreName;
	}

	public int getMetadata()
	{
		return metadata;
	}

	public void setMetadata(int metadata)
	{
		this.metadata = metadata;
	}

	public Item getItem()
	{
		return item;
	}

	public void setItem(Item item)
	{
		this.item = item;
	}

	public ItemStack createItemStack()
	{
		return new ItemStack(item, 1, metadata);
	}

	public String getUnlocalizedName()
	{
		return "item." + oreName;
	}

}
