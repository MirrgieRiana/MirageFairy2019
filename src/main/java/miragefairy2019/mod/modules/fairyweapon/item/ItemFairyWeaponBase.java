package miragefairy2019.mod.modules.fairyweapon.item;

import static net.minecraft.util.text.TextFormatting.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.base.Predicate;

import miragefairy2019.mod.api.composite.ApiComposite;
import miragefairy2019.mod.api.composite.IComponentInstance;
import miragefairy2019.mod.api.composite.IComposite;
import miragefairy2019.mod.api.composite.IItemComposite;
import miragefairy2019.mod.api.fairy.ApiFairy;
import miragefairy2019.mod.api.fairy.IComponentAbilityType;
import miragefairy2019.mod.api.fairy.IFairyType;
import miragefairy2019.mod.api.fairy.IItemFairy;
import miragefairy2019.mod.api.fairyweapon.formula.ApiFormula;
import miragefairy2019.mod.api.fairyweapon.formula.IFormula;
import miragefairy2019.mod.api.fairyweapon.formula.IMagicStatus;
import miragefairy2019.mod.api.fairyweapon.item.IItemFairyWeapon;
import miragefairy2019.mod.api.fairyweapon.recipe.ICombiningItem;
import miragefairy2019.mod.api.fairyweapon.recipe.ISphereReplacementItem;
import miragefairy2019.mod.api.main.ApiMain;
import miragefairy2019.mod.lib.BakedModelBuiltinWrapper;
import miragefairy2019.mod.lib.UtilsMinecraft;
import miragefairy2019.mod.modules.sphere.EnumSphere;
import mirrg.boron.util.struct.Tuple;
import mirrg.boron.util.suppliterator.ISuppliterator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreIngredient;

public class ItemFairyWeaponBase extends Item implements ISphereReplacementItem, IItemComposite, ICombiningItem, IItemFairyWeapon
{

	public ItemFairyWeaponBase()
	{
		setMaxStackSize(1);
		if (ApiMain.side().isClient()) {
			new Object() {
				@SideOnly(Side.CLIENT)
				public void run()
				{
					setTileEntityItemStackRenderer(new TileEntityItemStackRenderer() {
						@Override
						public void renderByItem(ItemStack itemStack, float partialTicks)
						{

							GlStateManager.disableRescaleNormal();

							// 本体描画
							{
								IBakedModel bakedModel = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(itemStack, null, null);
								if (bakedModel instanceof BakedModelBuiltinWrapper) {

									GlStateManager.pushMatrix();
									try {
										GlStateManager.translate(0.5F, 0.5F, 0.5F);

										Minecraft.getMinecraft().getRenderItem().renderItem(itemStack, ((BakedModelBuiltinWrapper) bakedModel).bakedModel);

									} finally {
										GlStateManager.popMatrix();
									}

								}
							}

							GlStateManager.disableRescaleNormal();

							// 搭乗妖精描画
							ItemStack itemStackFairy = getCombinedFairy(itemStack);
							if (!itemStackFairy.isEmpty()) {
								IBakedModel bakedModel = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(itemStackFairy, null, null);

								GlStateManager.pushMatrix();
								try {
									GlStateManager.translate(0.75F, 0.25F, 0.51F);
									GlStateManager.scale(0.5F, 0.5F, 1.0F);

									Minecraft.getMinecraft().getRenderItem().renderItem(itemStackFairy, bakedModel);

								} finally {
									GlStateManager.popMatrix();
								}

							}

						}
					});
				}
			}.run();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isFull3D()
	{
		return true;
	}

	@Override
	public boolean isEnchantable(ItemStack stack)
	{
		return false;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
	{
		return false;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book)
	{
		return false;
	}

	@Override
	public boolean isRepairable()
	{
		return false;
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker)
	{
		damageItem(stack, attacker);
		return true;
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving)
	{
		if (!worldIn.isRemote && state.getBlockHardness(worldIn, pos) != 0.0) {
			damageItem(stack, entityLiving);
		}
		return true;
	}

	//

	@Override
	@SideOnly(Side.CLIENT)
	public final void addInformation(ItemStack itemStack, World world, List<String> tooltip, ITooltipFlag flag)
	{

		// ポエム
		if (UtilsMinecraft.canTranslate(getUnlocalizedName() + ".poem")) {
			String string = UtilsMinecraft.translateToLocal(getUnlocalizedName() + ".poem");
			if (!string.isEmpty()) {
				tooltip.add(string);
			}
		}
		if (UtilsMinecraft.canTranslate(getUnlocalizedName() + ".author")) {
			String string = UtilsMinecraft.translateToLocal(getUnlocalizedName() + ".author");
			if (!string.isEmpty()) {
				tooltip.add(LIGHT_PURPLE + "Author: " + string);
			}
		}

		// 機能
		addInformationFunctions(itemStack, world, tooltip, flag);

		// アイテムステータス
		tooltip.add(GREEN + "Durability: " + Math.max(getMaxDamage(itemStack) - getDamage(itemStack), 0) + " / " + getMaxDamage(itemStack));

		// 搭乗中の妖精
		ItemStack itemStackFairy = getCombinedFairy(itemStack);
		if (!itemStackFairy.isEmpty()) tooltip.add(AQUA + "Combined: " + itemStackFairy.getDisplayName());

		// 素材
		tooltip.add(new TextComponentString("Contains: ")
			.setStyle(new Style().setColor(YELLOW))
			.appendSibling(getComposite().getDisplayString())
			.getFormattedText());

		// 妖精魔法ステータス
		Tuple<ItemStack, IFairyType> fairy = Optional.ofNullable(Minecraft.getMinecraft().player)
			.flatMap(p -> findFairy(itemStack, p))
			.orElseGet(() -> Tuple.of(ItemStack.EMPTY, ApiFairy.empty()));
		tooltip.add(new TextComponentString("Magic: ")
			.setStyle(new Style().setColor(BLUE))
			.appendSibling(getFairyMagicDisplayName(itemStack)
				.setStyle(new Style().setColor(AQUA).setBold(true)))
			.appendText(" with ")
			.appendSibling(new TextComponentString(fairy.x.isEmpty() ? "no fairy" : fairy.x.getDisplayName())
				.setStyle(new Style().setColor(WHITE)))
			.getFormattedText());
		addInformationFairyWeapon(itemStack, fairy.x, fairy.y, world, tooltip, flag);

	}

	//

	protected interface IInformationHandler
	{

		@SideOnly(Side.CLIENT)
		public void addInformation(ItemStack itemStack, World world, List<String> tooltip, ITooltipFlag flag);

	}

	private List<IInformationHandler> informationHandlersFunctions = new ArrayList<>();

	protected void addInformationHandlerFunctions(IInformationHandler informationHandler)
	{
		informationHandlersFunctions.add(informationHandler);
	}

	protected void addInformationHandlerFunctions(String information)
	{
		addInformationHandlerFunctions(new IInformationHandler() {
			@Override
			@SideOnly(Side.CLIENT)
			public void addInformation(ItemStack itemStack, World world, List<String> tooltip, ITooltipFlag flag)
			{
				tooltip.add(RED + information);
			}
		});
	}

	@SideOnly(Side.CLIENT)
	protected void addInformationFunctions(ItemStack itemStack, World world, List<String> tooltip, ITooltipFlag flag)
	{

		tooltip.add(RED + "Can be combined with fairy by crafting");

		tooltip.add(RED + "Can be repaired by crafting with contained sphere");

		for (IInformationHandler informationHandler : informationHandlersFunctions) {
			informationHandler.addInformation(itemStack, world, tooltip, flag);
		}

	}

	//

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformationFairyWeapon(ItemStack itemStackFairyWeapon, ItemStack itemStackFairy, IFairyType fairyType, World world, List<String> tooltip, ITooltipFlag flag)
	{

		addInformationMagicStatuses(itemStackFairyWeapon, itemStackFairy, fairyType, world, tooltip, flag);

	}

	@Override
	public ITextComponent getFairyMagicDisplayName(ItemStack itemStack)
	{
		String magicName;
		if (UtilsMinecraft.canTranslate(getUnlocalizedName() + ".magic.name")) {
			String string = UtilsMinecraft.translateToLocal(getUnlocalizedName() + ".magic.name");
			if (!string.isEmpty()) {
				magicName = string;
			} else {
				magicName = getItemStackDisplayName(itemStack);
			}
		} else {
			magicName = getItemStackDisplayName(itemStack);
		}
		return new TextComponentString(magicName);
	}

	//

	protected float destroySpeed = 1;

	@Override
	public float getDestroySpeed(ItemStack stack, IBlockState state)
	{
		for (String type : getToolClasses(stack)) {
			if (state.getBlock().isToolEffective(type, state)) {
				return destroySpeed;
			}
		}
		return 1;
	}

	//

	protected void damageItem(ItemStack itemStack, EntityLivingBase entityLivingBase)
	{
		ItemStack itemStackFairy = getCombinedFairy(itemStack);
		itemStack.damageItem(1, entityLivingBase);
		if (itemStack.isEmpty()) {

			// 妖精をドロップ
			EntityItem entityItem = new EntityItem(entityLivingBase.world, entityLivingBase.posX, entityLivingBase.posY, entityLivingBase.posZ, itemStackFairy.copy());
			entityItem.setNoPickupDelay();
			entityLivingBase.world.spawnEntity(entityItem);

		}
	}

	protected double getFairyAttribute(String attributeName, ItemStack itemStack)
	{
		if (!itemStack.hasTagCompound()) return 0;
		NBTTagCompound nbt = itemStack.getTagCompound();
		if (!nbt.hasKey("Fairy", NBT.TAG_COMPOUND)) return 0;
		NBTTagCompound fairy = nbt.getCompoundTag("Fairy");
		if (!fairy.hasKey(attributeName, NBT.TAG_DOUBLE)) return 0;
		return fairy.getDouble(attributeName);
	}

	protected void setFairyAttribute(String attributeName, ItemStack itemStack, double value)
	{
		if (!itemStack.hasTagCompound()) itemStack.setTagCompound(new NBTTagCompound());
		NBTTagCompound nbt = itemStack.getTagCompound();
		if (!nbt.hasKey("Fairy", NBT.TAG_COMPOUND)) nbt.setTag("Fairy", new NBTTagCompound());
		NBTTagCompound fairy = nbt.getCompoundTag("Fairy");
		fairy.setDouble(attributeName, value);
		itemStack.setTagCompound(nbt);
	}

	public ItemStack getCombinedFairy(ItemStack itemStack)
	{
		if (!itemStack.hasTagCompound()) return ItemStack.EMPTY;
		NBTTagCompound nbt = itemStack.getTagCompound();
		if (!nbt.hasKey("Fairy", NBT.TAG_COMPOUND)) return ItemStack.EMPTY;
		NBTTagCompound fairy = nbt.getCompoundTag("Fairy");
		if (!fairy.hasKey("CombinedFairy", NBT.TAG_COMPOUND)) return ItemStack.EMPTY;
		return new ItemStack(fairy.getCompoundTag("CombinedFairy"));
	}

	public void setCombinedFairy(ItemStack itemStack, ItemStack itemStackFairy)
	{
		if (!itemStack.hasTagCompound()) itemStack.setTagCompound(new NBTTagCompound());
		NBTTagCompound nbt = itemStack.getTagCompound();
		if (!nbt.hasKey("Fairy", NBT.TAG_COMPOUND)) nbt.setTag("Fairy", new NBTTagCompound());
		NBTTagCompound fairy = nbt.getCompoundTag("Fairy");
		fairy.setTag("CombinedFairy", itemStackFairy.writeToNBT(new NBTTagCompound()));
		itemStack.setTagCompound(nbt);
	}

	protected Optional<Tuple<ItemStack, IFairyType>> findFairy(ItemStack itemStack, EntityPlayer player)
	{

		// 搭乗中の妖精を優先
		{
			ItemStack itemStackFairy = getCombinedFairy(itemStack);
			if (getFairy(itemStackFairy).isPresent()) {
				return Optional.of(Tuple.of(itemStackFairy, getFairy(itemStackFairy).get()));
			}
		}

		return findItem(player, itemStackFairy -> getFairy(itemStackFairy).isPresent())
			.map(itemStackFairy -> Tuple.of(itemStackFairy, getFairy(itemStackFairy).get()));
	}

	protected Optional<ItemStack> findItem(EntityPlayer player, Predicate<ItemStack> predicate)
	{
		ItemStack itemStack;

		itemStack = player.getHeldItem(EnumHand.OFF_HAND);
		if (predicate.test(itemStack)) return Optional.of(itemStack);

		itemStack = player.getHeldItem(EnumHand.MAIN_HAND);
		if (predicate.test(itemStack)) return Optional.of(itemStack);

		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {

			itemStack = player.inventory.getStackInSlot(i);
			if (predicate.test(itemStack)) return Optional.of(itemStack);

		}

		return Optional.empty();
	}

	protected Optional<IFairyType> getFairy(ItemStack itemStack)
	{
		Item item = itemStack.getItem();
		if (!(item instanceof IItemFairy)) return Optional.empty();
		return ((IItemFairy) item).getMirageFairy2019Fairy(itemStack);
	}

	protected Vec3d getSight(EntityPlayer player, double distance)
	{
		float rotationPitch = player.rotationPitch;
		float rotationYaw = player.rotationYaw;
		double x = player.posX;
		double y = player.posY + (double) player.getEyeHeight();
		double z = player.posZ;
		Vec3d vec1 = new Vec3d(x, y, z);
		float f2 = MathHelper.cos(-rotationYaw * 0.017453292F - (float) Math.PI);
		float f3 = MathHelper.sin(-rotationYaw * 0.017453292F - (float) Math.PI);
		float f4 = -MathHelper.cos(-rotationPitch * 0.017453292F);
		float f5 = MathHelper.sin(-rotationPitch * 0.017453292F);
		float f6 = f3 * f4;
		float f7 = f2 * f4;
		Vec3d vec2 = vec1.addVector((double) f6 * distance, (double) f5 * distance, (double) f7 * distance);
		return vec2;
	}

	protected boolean breakBlock(World world, EntityPlayer player, EnumFacing facing, ItemStack itemStack, BlockPos blockPos, int fortune, boolean collection)
	{
		if (!world.isBlockModifiable(player, blockPos)) return false;
		if (!player.canPlayerEdit(blockPos, facing, itemStack)) return false;

		IBlockState blockState = world.getBlockState(blockPos);
		Block block = blockState.getBlock();
		block.dropBlockAsItem(world, blockPos, blockState, fortune);
		world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 3);
		if (collection) {
			for (EntityItem entityItem : world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(blockPos))) {
				entityItem.setPosition(player.posX, player.posY, player.posZ);
				entityItem.setNoPickupDelay();
			}
		}

		return true;
	}

	protected RayTraceResult rayTrace(World world, EntityPlayer player, boolean useLiquids, double additionalReach)
	{
		return rayTrace(world, player, useLiquids, additionalReach, Entity.class, e -> true);
	}

	protected <E extends Entity> RayTraceResult rayTrace(
		World world,
		EntityPlayer player,
		boolean useLiquids,
		double additionalReach,
		Class<? extends E> classEntity,
		Predicate<? super E> filterEntity)
	{
		float rotationPitch = player.rotationPitch;
		float rotationYaw = player.rotationYaw;
		double x = player.posX;
		double y = player.posY + (double) player.getEyeHeight();
		double z = player.posZ;
		Vec3d vec3d = new Vec3d(x, y, z);
		float f2 = MathHelper.cos(-rotationYaw * 0.017453292F - (float) Math.PI);
		float f3 = MathHelper.sin(-rotationYaw * 0.017453292F - (float) Math.PI);
		float f4 = -MathHelper.cos(-rotationPitch * 0.017453292F);
		float f5 = MathHelper.sin(-rotationPitch * 0.017453292F);
		float f6 = f3 * f4;
		float f7 = f2 * f4;
		double d3 = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() + additionalReach;
		Vec3d vec3d1 = vec3d.addVector((double) f6 * d3, (double) f5 * d3, (double) f7 * d3);

		// ブロックのレイトレース
		RayTraceResult rayTraceResultBlock = world.rayTraceBlocks(vec3d, vec3d1, useLiquids, !useLiquids, false);
		double squareDistanceBlock = rayTraceResultBlock != null
			? vec3d.squareDistanceTo(rayTraceResultBlock.hitVec)
			: 0;

		// エンティティのレイトレース
		RayTraceResult rayTraceResultEntity = null;
		double squareDistanceEntity = 0;
		{
			List<E> entities = world.getEntitiesWithinAABB(classEntity, new AxisAlignedBB(
				vec3d.x,
				vec3d.y,
				vec3d.z,
				vec3d1.x,
				vec3d1.y,
				vec3d1.z), filterEntity);

			Tuple<Double, RayTraceResult> nTuple = ISuppliterator.ofIterable(entities)
				.mapIfPresent(entity -> {
					if (entity == player) return Optional.empty();
					AxisAlignedBB aabb = entity.getEntityBoundingBox();
					RayTraceResult rayTraceResult = aabb.calculateIntercept(vec3d, vec3d1);
					if (rayTraceResult == null) return Optional.empty();
					return Optional.of(Tuple.of(vec3d.squareDistanceTo(rayTraceResult.hitVec), new RayTraceResult(entity, rayTraceResult.hitVec)));
				})
				.min((a, b) -> a.x.compareTo(b.x)).orElse(null);
			if (nTuple != null) {
				rayTraceResultEntity = nTuple.y;
				squareDistanceEntity = nTuple.x;
			}
		}

		if (rayTraceResultBlock != null && rayTraceResultEntity != null) {
			if (squareDistanceBlock < squareDistanceEntity) {
				return rayTraceResultBlock;
			} else {
				return rayTraceResultEntity;
			}
		} else if (rayTraceResultBlock != null) {
			return rayTraceResultBlock;
		} else if (rayTraceResultEntity != null) {
			return rayTraceResultEntity;
		} else {
			return null;
		}
	}

	protected void spawnParticle(World world, Vec3d sight, int color)
	{
		world.spawnParticle(
			EnumParticleTypes.SPELL_MOB,
			sight.x,
			sight.y,
			sight.z,
			((color >> 16) & 0xFF) / 255.0,
			((color >> 8) & 0xFF) / 255.0,
			((color >> 0) & 0xFF) / 255.0);
	}

	//////////////////// コンポジット関連

	private IComposite composite = ApiComposite.composite();

	public void addComponent(IComponentInstance componentInstance)
	{
		composite = composite.add(componentInstance);
	}

	public void addComponent(IComposite composite)
	{
		this.composite = this.composite.add(composite);
	}

	public IComposite getComposite()
	{
		return composite;
	}

	@Override
	public Optional<IComposite> getMirageFairy2019Composite(ItemStack itemStack)
	{
		return Optional.of(getComposite());
	}

	//////////////////// 妖精搭乗関連

	@Override
	public boolean canCombine(ItemStack itemStack)
	{
		return true;
	}

	@Override
	public boolean canCombineWith(ItemStack itemStack, ItemStack itemStackPart)
	{
		return itemStackPart.getItem() instanceof IItemFairy;
	}

	@Override
	public boolean canUncombine(ItemStack itemStack)
	{
		return !getCombinedFairy(itemStack).isEmpty();
	}

	@Override
	public ItemStack getCombinedPart(ItemStack itemStack)
	{
		return getCombinedFairy(itemStack);
	}

	@Override
	public void setCombinedPart(ItemStack itemStack, ItemStack itemStackPart)
	{
		setCombinedFairy(itemStack, itemStackPart);
	}

	//

	@Override
	public boolean hasContainerItem(ItemStack itemStack)
	{
		return !getContainerItem(itemStack).isEmpty();
	}

	@Override
	public ItemStack getContainerItem(ItemStack itemStack)
	{
		return getCombinedFairy(itemStack);
	}

	//////////////////// スフィア交換関連

	@Override
	public boolean canSphereReplace(ItemStack itemStack)
	{
		return true;
	}

	@Override
	public NonNullList<Ingredient> getRepairmentSpheres(ItemStack itemStack)
	{
		return getComposite().getComponents()
			.filter(e -> e.getComponent() instanceof IComponentAbilityType)
			.map(e -> Tuple.of((IComponentAbilityType) e.getComponent(), e.getNanoAmount()))
			.mapIfPresent(e -> EnumSphere.of(e.x.getAbilityType()).map(s -> Tuple.of(s, e.y)))
			.flatMap(e -> {
				long amount = e.y;
				int count = (int) (amount / 1_000_000_000L) + (amount % 1_000_000_000L != 0 ? 1 : 0);
				return ISuppliterator.range(count)
					.map(i -> new OreIngredient(e.x.getOreName()));
			})
			.toCollection(NonNullList::create);
	}

	@Override
	public ItemStack getSphereReplacedItem(ItemStack itemStack)
	{
		itemStack = itemStack.copy();
		itemStack.setItemDamage(0);
		return itemStack;
	}

	//////////////////// 妖精魔法ステータス関連

	private List<IMagicStatus<?>> magicStatuses = new ArrayList<>();

	protected <T> IMagicStatus<T> registerMagicStatus(String name, Function<T, ITextComponent> formatter, IFormula<T> formula)
	{
		IMagicStatus<T> magicStatus = ApiFormula.createMagicStatus(name, formatter, formula);
		magicStatuses.add(magicStatus);
		return magicStatus;
	}

	protected ISuppliterator<IMagicStatus<?>> getMagicStatuses()
	{
		return ISuppliterator.ofIterable(magicStatuses);
	}

	@SideOnly(Side.CLIENT)
	protected void addInformationMagicStatuses(ItemStack itemStackFairyWeapon, ItemStack itemStackFairy, IFairyType fairyType, World world, List<String> tooltip, ITooltipFlag flag)
	{
		for (IMagicStatus<?> magicStatus : getMagicStatuses()) {
			tooltip.add(magicStatus.getDisplayString(fairyType)
				.setStyle(new Style().setColor(BLUE))
				.getFormattedText());
		}
	}

}
