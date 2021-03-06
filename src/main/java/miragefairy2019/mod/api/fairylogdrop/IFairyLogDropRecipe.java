package miragefairy2019.mod.api.fairylogdrop;

import mirrg.boron.util.suppliterator.ISuppliterator;
import net.minecraft.item.ItemStack;

public interface IFairyLogDropRecipe
{

	public double getRate();

	public ItemStack getItemStackOutput();

	public ISuppliterator<IFairyLogDropCondition> getConditions();

}
