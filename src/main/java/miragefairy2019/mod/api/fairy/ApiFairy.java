package miragefairy2019.mod.api.fairy;

import miragefairy2019.mod.lib.EventRegistryMod;

public class ApiFairy
{

	public static void init(EventRegistryMod erMod)
	{
		miragefairy2019.mod.modules.fairy.ModuleFairy.init(erMod);
	}

	public static IComponentAbilityType getComponentAbilityType(IAbilityType abilityType)
	{
		return miragefairy2019.mod.modules.fairy.ComponentsAbilityType.getComponentAbilityType(abilityType);
	}

}
