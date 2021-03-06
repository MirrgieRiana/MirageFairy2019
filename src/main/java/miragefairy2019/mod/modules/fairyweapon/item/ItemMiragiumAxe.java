package miragefairy2019.mod.modules.fairyweapon.item;

import static miragefairy2019.mod.api.fairy.AbilityTypes.*;
import static miragefairy2019.mod.api.fairyweapon.formula.ApiFormula.*;

import java.util.List;

import miragefairy2019.mod.api.ApiMirageFlower;
import miragefairy2019.mod.api.fairy.IFairyType;
import miragefairy2019.mod.api.fairyweapon.formula.IFormulaDouble;
import miragefairy2019.mod.api.fairyweapon.formula.IMagicStatus;
import miragefairy2019.mod.api.main.ApiMain;
import mirrg.boron.util.struct.Tuple;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMiragiumAxe extends ItemFairyWeaponBase
{

	public IMagicStatus<Integer> maxHeight = registerMagicStatus("maxHeight", formatterInteger(),
		round(add(new IFormulaDouble[] {
			val(1.0),
			min(div(gaia(), 2), 100),
		})));

	public IMagicStatus<Double> power = registerMagicStatus("power", formatterDouble1(),
		add(new IFormulaDouble[] {
			val(2.0),
			div(aqua(), 2),
			div(abilityRaw(fell), 4),
		}));

	public IMagicStatus<Integer> fortune = registerMagicStatus("fortune", formatterInteger(),
		round(min(div(shine(), 5), 3)));

	public IMagicStatus<Double> coolTime = registerMagicStatus("coolTime", formatterTick(),
		mul(new IFormulaDouble[] {
			mul(cost(), 2),
			pow(0.5, div(dark(), 20)),
		}));

	public IMagicStatus<Double> wear = registerMagicStatus("wear", formatterPercent0(),
		mul(new IFormulaDouble[] {
			val(0.25),
			pow(0.5, div(fire(), 20))
		}));

	public IMagicStatus<Double> additionalReach = registerMagicStatus("additionalReach", formatterDouble1(),
		min(div(wind(), 5), 20));

	public IMagicStatus<Boolean> collection = registerMagicStatus("collection", formatterYesNo(),
		gte(abilityRaw(warp), 10));

	public ItemMiragiumAxe()
	{
		setHarvestLevel("axe", 1);
		destroySpeed = 6;
	}

	//

	@Override
	@SideOnly(Side.CLIENT)
	protected void addInformationFunctions(ItemStack itemStack, World world, List<String> tooltip, ITooltipFlag flag)
	{

		tooltip.add(TextFormatting.RED + "Right click to cut whole tree");

		super.addInformationFunctions(itemStack, world, tooltip, flag);

	}

	//

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack itemStack = player.getHeldItem(hand);

		// 妖精を取得
		Tuple<ItemStack, IFairyType> fairy = findFairy(itemStack, player).orElse(null);
		if (fairy == null) return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStack);

		// 視線判定
		RayTraceResult rayTraceResult = rayTrace(world, player, false, additionalReach.get(fairy.y));
		if (rayTraceResult == null) return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStack);
		if (rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStack);
		BlockPos blockPos = rayTraceResult.getBlockPos();

		// 対象が原木でない場合は不発
		if (!isLog(world, blockPos)) return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStack);

		// きこり発動
		{

			// 音取得
			SoundEvent breakSound;
			{
				IBlockState blockState = world.getBlockState(blockPos);
				breakSound = blockState.getBlock().getSoundType(blockState, world, blockPos, player).getBreakSound();
			}

			int yMin = blockPos.getY();

			// 基点検索
			int yMax = blockPos.getY();
			for (int yi = 1; yi < maxHeight.get(fairy.y); yi++) {
				if (isLog(world, blockPos.add(0, yi, 0))) {
					yMax = blockPos.getY() + yi;
				} else {
					break;
				}
			}

			// 破壊
			int successed = 0;
			double power2 = power.get(fairy.y);
			for (int y = yMax; y >= yMin; y--) {
				BlockPos blockPos2 = new BlockPos(blockPos.getX(), y, blockPos.getZ());

				IBlockState blockState = world.getBlockState(blockPos2);
				float blockHardness = blockState.getBlockHardness(world, blockPos2);

				// パワーが足りないので破壊をやめる
				if (power2 < blockHardness) break;

				// 耐久が0のときは破壊をやめる
				if (itemStack.getItemDamage() >= itemStack.getMaxDamage()) break;

				if (world.rand.nextDouble() < wear.get(fairy.y)) itemStack.damageItem(1, player);
				power2 -= blockHardness;
				breakBlock(world, player, rayTraceResult.sideHit, itemStack, blockPos2, fortune.get(fairy.y), collection.get(fairy.y));
				successed++;

			}

			if (successed > 0) {

				// エフェクト
				player.playSound(breakSound, 1.0F, 1.0F);

				// クールタイム
				player.getCooldownTracker().setCooldown(this, (int) (coolTime.get(fairy.y) * (1 - power2 / power.get(fairy.y))));

			}

		}

		return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
	}

	private boolean isLog(World world, BlockPos blockPos)
	{
		IBlockState blockState = world.getBlockState(blockPos);
		if (blockState.getBlock().equals(ApiMirageFlower.blockFairyLog)) return true;
		if (blockState.getBlock() instanceof BlockLog) return true;
		if (blockState.getBlock() instanceof BlockLeaves) return true;
		return false;
	}

	@Override
	public void onUpdate(ItemStack itemStack, World world, Entity entity, int itemSlot, boolean isSelected)
	{
		if (entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entity;

			if (isSelected || player.getHeldItemOffhand() == itemStack) {

				if (ApiMain.side().isClient()) {

					// 妖精がない場合はマゼンタ
					Tuple<ItemStack, IFairyType> fairy = findFairy(itemStack, player).orElse(null);
					if (fairy == null) {
						spawnParticle(
							world,
							getSight(player, player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue()),
							0xFF00FF);
						return;
					}

					// 耐久がない場合は赤
					// 対象が発動対象でない場合は緑
					// クールタイムの場合は黄色
					RayTraceResult rayTraceResult = rayTrace(world, player, false, additionalReach.get(fairy.y));
					if (rayTraceResult == null) {
						spawnParticle(
							world,
							getSight(player, player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() + additionalReach.get(fairy.y)),
							itemStack.getItemDamage() >= itemStack.getMaxDamage() ? 0xFF0000 : player.getCooldownTracker().hasCooldown(this) ? 0x00FF00 : 0x00FFFF);
						return;
					}
					if (!canExecute(world, rayTraceResult)) {
						spawnParticle(
							world,
							rayTraceResult.hitVec,
							itemStack.getItemDamage() >= itemStack.getMaxDamage() ? 0xFF0000 : player.getCooldownTracker().hasCooldown(this) ? 0x00FF00 : 0x00FFFF);
						return;
					}

					spawnParticle(
						world,
						rayTraceResult.hitVec,
						itemStack.getItemDamage() >= itemStack.getMaxDamage() ? 0xFF0000 : player.getCooldownTracker().hasCooldown(this) ? 0xFFFF00 : 0xFFFFFF);

				}

			}

		}
	}

	protected boolean canExecute(World world, RayTraceResult rayTraceResult)
	{
		return rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK && isLog(world, rayTraceResult.getBlockPos());
	}

}
