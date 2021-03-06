package miragefairy2019.mod.api.playeraura;

import java.util.Optional;

import miragefairy2019.mod.api.fairy.IManaSet;
import miragefairy2019.mod.modules.fairy.EnumManaType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public interface IPlayerAura
{

	public double getAura(EnumManaType manaType);

	public Optional<IManaSet> getFoodAura(ItemStack itemStack);

	public void setAura(double shine, double fire, double wind, double gaia, double aqua, double dark);

	/**
	 * Server World Only
	 */
	public void save(EntityPlayer player);

	/**
	 * Server World Only
	 */
	public void load(EntityPlayer player);

	/**
	 * Server World Only
	 */
	public void onEat(EntityPlayerMP player, ItemStack itemStack, int healAmount);

	/**
	 * Server World Only
	 */
	public void send(EntityPlayerMP player);

}
