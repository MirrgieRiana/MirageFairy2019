package miragefairy2019.mod.modules.mirageflower.fairylogdrop;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import miragefairy2019.mod.modules.mirageflower.fairylogdrop.api.IFairyLogDropCondition;
import miragefairy2019.mod.modules.mirageflower.fairylogdrop.api.IFairyLogDropRecipe;
import mirrg.boron.util.suppliterator.ISuppliterator;
import net.minecraft.item.ItemStack;

public final class FairyLogDropRecipe implements IFairyLogDropRecipe
{

	private double rate;
	private Supplier<ItemStack> sItemStackOutput;
	private List<IFairyLogDropCondition> conditions = new ArrayList<>();

	public FairyLogDropRecipe(double rate, Supplier<ItemStack> sItemStackOutput)
	{
		this.rate = rate;
		this.sItemStackOutput = sItemStackOutput;
	}

	@Override
	public double getRate()
	{
		return rate;
	}

	@Override
	public ItemStack getItemStackOutput()
	{
		return sItemStackOutput.get();
	}

	public void addCondition(IFairyLogDropCondition condition)
	{
		conditions.add(condition);
	}

	@Override
	public ISuppliterator<IFairyLogDropCondition> getConditions()
	{
		return ISuppliterator.ofIterable(conditions);
	}

}
