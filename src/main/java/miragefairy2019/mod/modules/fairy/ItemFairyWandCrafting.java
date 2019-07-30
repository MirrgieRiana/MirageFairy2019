package miragefairy2019.mod.modules.fairy;

import net.minecraft.item.ItemStack;

public class ItemFairyWandCrafting extends ItemToolFairy
{

	public ItemFairyWandCrafting()
	{
		this.setMaxDamage(16 - 1);
	}

	@Override
	public boolean hasContainerItem(ItemStack itemStack)
	{
		return !(itemStack.getItemDamage() >= itemStack.getMaxDamage());
	}

	@Override
	public ItemStack getContainerItem(ItemStack itemStack)
	{
		itemStack = itemStack.copy();
		if (itemStack.getItemDamage() >= itemStack.getMaxDamage()) return ItemStack.EMPTY;
		itemStack.setItemDamage(itemStack.getItemDamage() + 1);
		return itemStack;
	}

}
