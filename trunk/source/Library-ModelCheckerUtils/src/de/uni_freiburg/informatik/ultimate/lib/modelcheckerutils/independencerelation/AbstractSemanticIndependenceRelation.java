/*
 * Copyright (C) 2019 Dominik Klumpp (klumpp@informatik.uni-freiburg.de)
 * Copyright (C) 2019 University of Freiburg
 *
 * This file is part of the ULTIMATE ModelCheckerUtils Library.
 *
 * The ULTIMATE ModelCheckerUtils Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ULTIMATE ModelCheckerUtils Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE ModelCheckerUtils Library. If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE ModelCheckerUtils Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP),
 * containing parts covered by the terms of the Eclipse Public License, the
 * licensors of the ULTIMATE ModelCheckerUtils Library grant you additional permission
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.independencerelation;

import java.util.Arrays;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.ModelCheckerUtils;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IIcfgTransition;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IInternalAction;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.transitions.TransFormulaBuilder;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.transitions.TransFormulaUtils;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.transitions.UnmodifiableTransFormula;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.hoaretriple.IHoareTripleChecker;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.SmtUtils.SimplificationTechnique;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.SmtUtils.XnfConversionTechnique;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.managedscript.ManagedScript;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.predicates.IPredicate;
import de.uni_freiburg.informatik.ultimate.logic.Script.LBool;

/**
 * Independence relation that considers semantic independence of abstractions of
 * the given actions. The abstraction is defined by the set of valid pre- /
 * post-condition pairs in a given set of predicates.
 *
 * It is recommended to wrap this relation in a
 * {@link CachedIndependenceRelation} for better performance.
 *
 * @author Dominik Klumpp (klumpp@informatik.uni-freiburg.de)
 */
@Deprecated
public class AbstractSemanticIndependenceRelation implements IIndependenceRelation<IPredicate, IIcfgTransition<?>> {

	private final IUltimateServiceProvider mServices;
	private final ManagedScript mManagedScript;
	private final ILogger mLogger;

	private static final SimplificationTechnique mSimplificationTechnique = SimplificationTechnique.SIMPLIFY_DDA;
	private static final XnfConversionTechnique mXnfConversionTechnique = XnfConversionTechnique.BOTTOM_UP_WITH_LOCAL_SIMPLIFICATION;

	private final boolean mConditional;
	private final boolean mSymmetric;
	private final HoareAbstraction mAbstraction;

	/**
	 * Create a new variant of the abstract semantic independence relation.
	 *
	 * @param conditional
	 *            If set to true, the relation will be conditional: It will use the
	 *            given {@link IPredicate} as context to enable additional
	 *            commutativity. This subsumes the non-conditional variant.
	 * @param symmetric
	 *            If set to true, the relation will check for full commutativity. If
	 *            false, only semicommutativity is checked, which subsumes full
	 *            commutativity.
	 */
	public AbstractSemanticIndependenceRelation(final IUltimateServiceProvider services, final ManagedScript mgdScript,
			final IHoareTripleChecker checker, final Set<IPredicate> predicates, final Set<IProgramVar> allVariables,
			final boolean conditional, final boolean symmetric) {
		mServices = services;
		mManagedScript = mgdScript;
		mLogger = services.getLoggingService().getLogger(ModelCheckerUtils.PLUGIN_ID);

		mConditional = conditional;
		mSymmetric = symmetric;

		mAbstraction = new HoareAbstraction(mgdScript, checker, predicates, allVariables);
	}

	@Override
	public boolean isSymmetric() {
		return mSymmetric;
	}

	@Override
	public boolean isConditional() {
		return mConditional;
	}

	@Override
	public boolean contains(final IPredicate state, final IIcfgTransition<?> a, final IIcfgTransition<?> b) {
		assert a instanceof IInternalAction && b instanceof IInternalAction;

		final IPredicate context = mConditional ? state : null;
		final UnmodifiableTransFormula tfA = mAbstraction.computeAbstraction(a);
		final UnmodifiableTransFormula tfB = mAbstraction.computeAbstraction(b);

		assert TransFormulaUtils.checkImplication(a.getTransformula(), tfA,
				mManagedScript) != LBool.SAT : "Abstraction is not a superset of concrete relation";
		assert TransFormulaUtils.checkImplication(b.getTransformula(), tfB,
				mManagedScript) != LBool.SAT : "Abstraction is not a superset of concrete relation";

		final boolean subset = performInclusionCheck(context, tfA, tfB);
		if (mSymmetric) {
			final boolean superset = performInclusionCheck(context, tfB, tfA);
			return subset && superset;
		} else {
			return subset;
		}
	}

	// This method and the next are duplicated in SemanticIndependenceRelation.
	// TODO: Avoid that.
	private final boolean performInclusionCheck(final IPredicate context, final UnmodifiableTransFormula a,
			final UnmodifiableTransFormula b) {
		UnmodifiableTransFormula transFormula1 = compose(a, b);
		final UnmodifiableTransFormula transFormula2 = compose(b, a);

		if (context != null) {
			// TODO: This represents conjunction with guard (precondition) as composition
			// with assume. Is this a good way?
			final UnmodifiableTransFormula guard = TransFormulaBuilder.constructTransFormulaFromPredicate(context,
					mManagedScript);
			transFormula1 = compose(guard, transFormula1);
		}

		final LBool result = TransFormulaUtils.checkImplication(transFormula1, transFormula2, mManagedScript);
		return result == LBool.UNSAT;
	}

	private final UnmodifiableTransFormula compose(final UnmodifiableTransFormula first,
			final UnmodifiableTransFormula second) {
		// no need to do simplification, result is only used in one implication check
		final boolean simplify = false;

		// try to eliminate auxiliary variables to avoid quantifier alternation in
		// implication check
		final boolean tryAuxVarElimination = true;

		return TransFormulaUtils.sequentialComposition(mLogger, mServices, mManagedScript, simplify,
				tryAuxVarElimination, false, mXnfConversionTechnique, mSimplificationTechnique,
				Arrays.asList(first, second));
	}
}
