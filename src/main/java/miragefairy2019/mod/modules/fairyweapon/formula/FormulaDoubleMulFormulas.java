package miragefairy2019.mod.modules.fairyweapon.formula;

import miragefairy2019.mod.api.fairy.IFairyType;
import miragefairy2019.mod.api.fairyweapon.formula.IFormula;
import miragefairy2019.mod.api.fairyweapon.formula.ISource;
import mirrg.boron.util.struct.ImmutableArray;
import mirrg.boron.util.suppliterator.ISuppliterator;

public class FormulaDoubleMulFormulas implements IFormula<Double>
{

	private ImmutableArray<IFormula<Double>> formulas;

	/**
	 * @param formulas
	 *            すべての式は非負でなければなりません。
	 */
	public FormulaDoubleMulFormulas(ImmutableArray<IFormula<Double>> formulas)
	{
		this.formulas = formulas;
		for (IFormula<Double> formula : formulas) {
			if (formula.getMin() < 0) throw new IllegalArgumentException("" + formula + ", " + formula.getMin());
		}
	}

	@Override
	public Double get(IFairyType fairyType)
	{
		double a = 0;
		for (IFormula<Double> formula : formulas) {
			a += formula.get(fairyType);
		}
		return a;
	}

	@Override
	public Double getMax()
	{
		double a = 0;
		for (IFormula<Double> formula : formulas) {
			a += formula.getMax();
		}
		return a;
	}

	@Override
	public Double getMin()
	{
		double a = 0;
		for (IFormula<Double> formula : formulas) {
			a += formula.getMin();
		}
		return a;
	}

	@Override
	public ISuppliterator<ISource> getSources()
	{
		return formulas.suppliterator()
			.flatMap(f -> f.getSources());
	}

}