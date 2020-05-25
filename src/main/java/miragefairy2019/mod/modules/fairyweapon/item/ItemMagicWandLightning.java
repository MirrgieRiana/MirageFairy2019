package miragefairy2019.mod.modules.fairyweapon.item;

import static miragefairy2019.mod.api.fairy.AbilityTypes.*;
import static miragefairy2019.mod.api.fairyweapon.formula.ApiFormula.*;

import java.util.List;

import miragefairy2019.mod.api.fairy.ApiFairy;
import miragefairy2019.mod.api.fairy.IFairyType;
import miragefairy2019.mod.api.fairyweapon.formula.IFormulaDouble;
import miragefairy2019.mod.api.fairyweapon.formula.IMagicStatus;
import miragefairy2019.mod.api.main.ApiMain;
import miragefairy2019.mod.modules.fairyweapon.magic.MagicExecutor;
import miragefairy2019.mod.modules.fairyweapon.magic.SelectorEntityRanged;
import miragefairy2019.mod.modules.fairyweapon.magic.SelectorRayTrace;
import mirrg.boron.util.UtilsMath;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMagicWandLightning extends ItemFairyWeaponBase
{

	public IMagicStatus<Double> damage = registerMagicStatus("damage", formatterDouble1(),
		add(new IFormulaDouble[] {
			val(1),
			scale(wind(), 60.0, 10.0, 1),
			scale(ability(thunder), 20.0, 10.0),
		}));

	public IMagicStatus<CriticalRate> criticalRate = registerMagicStatus("criticalRate", formatterCriticalRate(),
		val(new CriticalRate(0, 0, 0, 7, 2, 1, 0, 0)));

	public IMagicStatus<Double> additionalReach = registerMagicStatus("additionalReach", formatterDouble1(),
		add(new IFormulaDouble[] {
			val(2),
			scale(aqua(), 30.0, 8.0, 1),
			scale(ability(shoot), 10.0, 4.0),
		}));

	public IMagicStatus<Double> radius = registerMagicStatus("radius", formatterDouble1(),
		add(new IFormulaDouble[] {
			val(1),
			scale(gaia(), 30.0, 3.0),
		}));

	public IMagicStatus<Double> wear = registerMagicStatus("wear", formatterPercent0(),
		mul(new IFormulaDouble[] {
			div(cost(), 50),
			pow(0.5, norm(fire(), 30.0)),
		}));

	public IMagicStatus<Double> coolTime = registerMagicStatus("coolTime", formatterTick(),
		mul(new IFormulaDouble[] {
			mul(cost(), 1 / 50.0 * 40),
			pow(0.5, norm(dark(), 30.0)),
			pow(0.5, norm(ability(knowledge), 10.0)),
		}));

	//

	public MagicExecutor getExecutor(ItemFairyWeaponBase item, World world, ItemStack itemStack, EntityPlayer player)
	{

		// 妖精取得
		IFairyType fairyType = findFairy(itemStack, player).map(t -> t.y).orElseGet(ApiFairy::empty);

		// 視線判定
		SelectorRayTrace selectorRayTrace = new SelectorRayTrace(world, player, additionalReach.get(fairyType));

		// 対象判定
		SelectorEntityRanged<EntityLivingBase> selectorEntityRanged = new SelectorEntityRanged<>(
			world,
			selectorRayTrace.getTarget(),
			EntityLivingBase.class,
			e -> e != player,
			radius.get(fairyType),
			1);

		// 実行可能性を計算
		boolean ok;
		int color;
		if (fairyType.isEmpty()) {
			ok = false;
			color = 0xFF00FF;
		} else if (itemStack.getItemDamage() + (int) Math.ceil(wear.get(fairyType)) > itemStack.getMaxDamage()) {
			ok = false;
			color = 0xFF0000;
		} else if (selectorEntityRanged.getEffectiveEntities().count() == 0) {
			ok = false;
			color = 0x00FFFF;
		} else if (player.getCooldownTracker().hasCooldown(item)) {
			ok = false;
			color = 0xFFFF00;
		} else {
			ok = true;
			color = 0xFFFFFF;
		}

		return new MagicExecutor() {
			@Override
			public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
			{
				if (!ok) return new ActionResult<>(EnumActionResult.PASS, itemStack);

				int targetCount = 0;
				for (EntityLivingBase target : selectorEntityRanged.getEffectiveEntities()) {

					// 耐久が足りないので中止
					if (itemStack.getItemDamage() + (int) Math.ceil(wear.get(fairyType)) > itemStack.getMaxDamage()) break;

					// パワーが足りないので中止
					if (targetCount >= 1) break;

					// 行使
					itemStack.damageItem(UtilsMath.randomInt(world.rand, wear.get(fairyType)), player);
					targetCount++;
					{
						double damage2 = damage.get(fairyType);

						EnumCriticalFactor factor = criticalRate.get(fairyType).get(world.rand);
						damage2 *= factor.coefficient;

						target.attackEntityFrom(new DamageSourceFairyMagic(player, 0), (float) damage2);

						// エフェクト
						if (factor.coefficient > 1) {
							for (int i = 0; i < 16; i++) {
								double dx = world.rand.nextDouble() * 2 - 1;
								double dy = world.rand.nextDouble() * 2 - 1;
								double dz = world.rand.nextDouble() * 2 - 1;
								if (dx * dx + dy * dy + dz * dz <= 1) {
									double x = target.posX + dx * target.width / 4.0;
									double y = target.getEntityBoundingBox().minY + target.height / 2.0 + dy * target.height / 4.0;
									double z = target.posZ + dz * target.width / 4.0;
									world.spawnParticle(EnumParticleTypes.CRIT, x, y, z, dx, dy + 0.2, dz); // TODO 色
								}
							}
						}

						if (world instanceof WorldServer) {
							int count = UtilsMath.randomInt(world.rand, damage2 / 2.0);
							if (count > 0) {
								((WorldServer) world).spawnParticle(
									EnumParticleTypes.DAMAGE_INDICATOR,
									target.posX,
									target.posY + target.height * 0.5,
									target.posZ,
									count,
									0.1,
									0,
									0.1,
									0.2);
							}
						}

					}

				}

				if (targetCount >= 1) {

					// エフェクト
					world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_LIGHTNING_IMPACT, SoundCategory.PLAYERS, 0.2F, 1.0F);

					// クールタイム
					player.getCooldownTracker().setCooldown(item, (int) (double) coolTime.get(fairyType));

				}

				return new ActionResult<>(targetCount >= 1 ? EnumActionResult.SUCCESS : EnumActionResult.PASS, itemStack);
			}

			@Override
			public void onUpdate(ItemStack itemStack, World world, Entity entity, int itemSlot, boolean isSelected)
			{
				selectorRayTrace.effect(color);
				selectorEntityRanged.effect();
			}
		};
	}

	//

	@Override
	@SideOnly(Side.CLIENT)
	protected void addInformationFunctions(ItemStack itemStack, World world, List<String> tooltip, ITooltipFlag flag)
	{

		super.addInformationFunctions(itemStack, world, tooltip, flag);

		tooltip.add(TextFormatting.RED + "Right click to use magic");

	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{

		// アイテム取得
		ItemStack itemStack = player.getHeldItem(hand);

		return getExecutor(this, world, itemStack, player).onItemRightClick(world, player, hand);
	}

	@Override
	public void onUpdate(ItemStack itemStack, World world, Entity entity, int itemSlot, boolean isSelected)
	{

		// クライアントのみ
		if (!ApiMain.side().isClient()) return;

		// プレイヤー取得
		if (!(entity instanceof EntityPlayer)) return;
		EntityPlayer player = (EntityPlayer) entity;

		// アイテム取得
		if (!isSelected && player.getHeldItemOffhand() != itemStack) return;

		getExecutor(this, world, itemStack, player).onUpdate(itemStack, world, entity, itemSlot, isSelected);

	}

}