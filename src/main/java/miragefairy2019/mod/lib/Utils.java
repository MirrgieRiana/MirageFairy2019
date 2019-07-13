package miragefairy2019.mod.lib;

import java.util.Random;

// TODO mirrg.boron
public class Utils
{

	/**
	 * 期待値がdになるように整数の乱数を生成します。
	 */
	public static int randomInt(double d, Random random)
	{
		int i = (int) Math.floor(d);
		double mod = d - i;
		if (random.nextDouble() < mod) i++;
		return i;
	}

	/**
	 * 先頭だけを大文字にし、残りは変えません。
	 */
	public static String toUpperCaseHead(String string)
	{
		return string.substring(0, 1).toUpperCase() + string.substring(1);
	}

}