package miragefairy2019.mod.modules.fairyweapon;

import java.util.List;
import java.util.function.Predicate;

import miragefairy2019.mod.modules.fairycrystal.ItemFairyCrystal;
import miragefairy2019.mod.modules.sphere.EnumSphere;
import mirrg.boron.util.suppliterator.ISuppliterator;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreIngredient;

public class ItemSummoningFairyWand extends ItemFairyCraftingToolBase
{

	public ItemSummoningFairyWand()
	{
		this.setMaxDamage(16 - 1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, World world, List<String> tooltip, ITooltipFlag flag)
	{

		// ポエム
		tooltip.add("餌付けのステッキ");

		// アイテムステータス
		tooltip.add(TextFormatting.GREEN + "Durability: " + (getMaxDamage(itemStack) - getDamage(itemStack)) + " / " + getMaxDamage(itemStack));

		// 素材
		tooltip.add(TextFormatting.YELLOW + "Contains: Wood(2.000), Sphere of \"CRYSTAL\"");

		// 機能
		tooltip.add(TextFormatting.RED + "Hold right mouse button to use fairy crystals quickly");
		tooltip.add(TextFormatting.RED + "Can be repaired by crafting with contained sphere");

		super.addInformation(itemStack, world, tooltip, flag);

	}

	//

	@Override
	public EnumAction getItemUseAction(ItemStack stack)
	{
		return EnumAction.BOW;
	}

	// 永続
	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		player.setActiveHand(hand);
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase entityLivingBase, int count)
	{
		if (entityLivingBase.world.isRemote) return;

		if (entityLivingBase instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entityLivingBase;

			// 使用Tickじゃないなら抜ける
			if (!isUsingTick(count)) return;

			// 妖晶を得る
			ItemStack nItemStackFairyCrystal = findItem(player, itemStack -> itemStack.getItem() instanceof ItemFairyCrystal).orElse(null);
			if (nItemStackFairyCrystal == null) return;

			// プレイヤー視点判定
			RayTraceResult rayTraceResult = rayTrace(player.world, player, false);
			if (rayTraceResult == null) return; // ブロックに当たらなかった場合は無視
			if (rayTraceResult.typeOfHit != Type.BLOCK) return; // ブロックに当たらなかった場合は無視

			// ガチャを引く
			ItemStack nItemStackDrop = ItemFairyCrystal.drop(
				player,
				player.world,
				rayTraceResult.getBlockPos(),
				player.getHeldItem(EnumHand.MAIN_HAND).getItem() == this ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND,
				rayTraceResult.sideHit,
				(float) rayTraceResult.hitVec.x,
				(float) rayTraceResult.hitVec.y,
				(float) rayTraceResult.hitVec.z).orElse(null);
			if (nItemStackDrop == null) return; // ガチャが引けなかった場合は無視
			if (nItemStackDrop.isEmpty()) return; // ガチャが引けなかった場合は無視

			// 成立

			// ガチャアイテムを消費
			nItemStackFairyCrystal.shrink(1);
			player.addStat(StatList.getObjectUseStats(nItemStackFairyCrystal.getItem()));

			// 妖精をドロップ
			BlockPos pos2 = rayTraceResult.getBlockPos().offset(rayTraceResult.sideHit);
			EntityItem entityitem = new EntityItem(player.world, pos2.getX() + 0.5, pos2.getY() + 0.5, pos2.getZ() + 0.5, nItemStackDrop.copy());
			entityitem.setNoPickupDelay();
			player.world.spawnEntity(entityitem);

		}
	}

	private boolean isUsingTick(int count)
	{
		count = 72000 - count;
		if (count >= 60) return true;
		if (count >= 20) return count % 2 == 0;
		if (count >= 5) return count % 5 == 0;
		if (count == 1) return true;
		return false;
	}

	//

	@Override
	public NonNullList<Predicate<ItemStack>> getSpheres(ItemStack itemStack)
	{
		return ISuppliterator.of(
			new OreIngredient(EnumSphere.crystal.getOreName()))
			.toCollection(NonNullList::create);
	}

}