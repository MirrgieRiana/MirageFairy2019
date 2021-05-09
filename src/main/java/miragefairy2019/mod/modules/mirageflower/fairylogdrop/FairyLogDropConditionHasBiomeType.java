package miragefairy2019.mod.modules.mirageflower.fairylogdrop;

import miragefairy2019.mod.modules.mirageflower.fairylogdrop.api.IFairyLogDropCondition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class FairyLogDropConditionHasBiomeType implements IFairyLogDropCondition
{

	private final Type biome;

	public FairyLogDropConditionHasBiomeType(Type biome)
	{
		this.biome = biome;
	}

	@Override
	public boolean test(World world, BlockPos blockPos)
	{
		return BiomeDictionary.hasType(world.getBiome(blockPos), biome);
	}

	@Override
	public String getLocalizedDescription()
	{
		return biome.getName();
	}

}
