package miragefairy2019.mod.modules.ore.ore;

import java.util.Random;

import miragefairy2019.mod.lib.multi.IListBlockVariant;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockOre<V extends IBlockVariantOre> extends Block
{

	public final IListBlockVariant<V> variantList;

	public BlockOre(IListBlockVariant<V> variantList)
	{
		super(Material.ROCK);
		this.variantList = variantList;

		// meta
		setDefaultState(blockState.getBaseState()
			.withProperty(VARIANT, variantList.getDefaultMetadata()));

		// style
		setSoundType(SoundType.STONE);

		// 挙動
		setHardness(3.0F);
		setResistance(5.0F);

		for (V variant : variantList) {
			setHarvestLevel(variant.getHarvestTool(), variant.getHarvestLevel(), getState(variant));
		}

	}

	//

	public static final PropertyInteger VARIANT = PropertyInteger.create("variant", 0, 15);

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, VARIANT);
	}

	public IBlockState getState(V variant)
	{
		return getDefaultState().withProperty(VARIANT, variant.getMetadata());
	}

	@Override
	public IBlockState getStateFromMeta(int metadata)
	{
		return getDefaultState().withProperty(VARIANT, metadata);
	}

	@Override
	public int getMetaFromState(IBlockState blockState)
	{
		return blockState.getValue(VARIANT);
	}

	@Override
	public void getSubBlocks(CreativeTabs creativeTab, NonNullList<ItemStack> itemStacks)
	{
		for (V variant : variantList) {
			itemStacks.add(new ItemStack(this, 1, variant.getMetadata()));
		}
	}

	//

	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state)
	{
		return new ItemStack(Item.getItemFromBlock(this), 1, getMetaFromState(state));
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		Random random = world instanceof World ? ((World) world).rand : RANDOM;
		variantList.byMetadata(getMetaFromState(state)).getDrops(drops, random, this, getMetaFromState(state), fortune);
	}

	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player)
	{
		return true;
	}

	@Override
	public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune)
	{
		Random random = world instanceof World ? ((World) world).rand : new Random();
		return variantList.byMetadata(getMetaFromState(state)).getExpDrop(random, fortune);
	}

	//

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.CUTOUT_MIPPED;
	}

}