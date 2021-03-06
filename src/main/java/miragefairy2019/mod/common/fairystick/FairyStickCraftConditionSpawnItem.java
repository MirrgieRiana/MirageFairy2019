package miragefairy2019.mod.common.fairystick;

import java.util.function.Supplier;

import miragefairy2019.mod.api.fairystick.IFairyStickCraftCondition;
import miragefairy2019.mod.api.fairystick.IFairyStickCraftEnvironment;
import miragefairy2019.mod.api.fairystick.IFairyStickCraftExecutor;
import mirrg.boron.util.suppliterator.ISuppliterator;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

public class FairyStickCraftConditionSpawnItem implements IFairyStickCraftCondition
{

	private Supplier<ItemStack> sItemStack;

	public FairyStickCraftConditionSpawnItem(Supplier<ItemStack> sItemStack)
	{
		this.sItemStack = sItemStack;
	}

	@Override
	public boolean test(IFairyStickCraftEnvironment environment, IFairyStickCraftExecutor executor)
	{
		executor.hookOnCraft(setterItemStackFairyStick -> {

			if (!environment.getWorld().isRemote) {
				EntityItem entityitem = new EntityItem(
					environment.getWorld(),
					environment.getBlockPos().getX() + 0.5,
					environment.getBlockPos().getY() + 0.5,
					environment.getBlockPos().getZ() + 0.5,
					sItemStack.get().copy());
				entityitem.setNoPickupDelay();
				environment.getWorld().spawnEntity(entityitem);
			}

		});
		return true;
	}

	@Override
	public ISuppliterator<Iterable<ItemStack>> getIngredientsOutput()
	{
		return ISuppliterator.of(ISuppliterator.of(sItemStack.get()));
	}

}
