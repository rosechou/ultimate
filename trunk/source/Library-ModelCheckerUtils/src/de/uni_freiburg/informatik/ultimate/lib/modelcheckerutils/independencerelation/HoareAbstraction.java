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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IIcfgTransition;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IInternalAction;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.transitions.TransFormulaBuilder;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.transitions.UnmodifiableTransFormula;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramConst;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.hoaretriple.IHoareTripleChecker;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.hoaretriple.IHoareTripleChecker.Validity;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.SmtUtils;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.Substitution;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.managedscript.ManagedScript;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.predicates.IPredicate;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.util.datastructures.relation.HashRelation;

/**
 * An abstraction that abstracts a statement by the set of all valid
 * pre-/postcondition pairs in a given set of predicates.
 *
 * @author Dominik Klumpp (klumpp@informatik.uni-freiburg.de)
 */
public class HoareAbstraction {

	private final IHoareTripleChecker mChecker;
	private final Set<IPredicate> mPredicates;
	private final Set<IProgramVar> mAllVariables;

	private final Map<IInternalAction, HashRelation<IPredicate, IPredicate>> mAbstractionCache = new HashMap<>();

	private final ManagedScript mManagedScript;

	public HoareAbstraction(final ManagedScript mgdScript, final IHoareTripleChecker checker,
			final Collection<IPredicate> predicates, final Set<IProgramVar> allVariables) {
		mManagedScript = mgdScript;
		mChecker = checker;
		mPredicates = new HashSet<IPredicate>(predicates);
		mAllVariables = allVariables;
	}

	public UnmodifiableTransFormula computeAbstraction(IIcfgTransition<?> transition) {
		assert transition instanceof IInternalAction : "Cannot abstract transitions of type " + transition.getClass();
		return concretizeAbstraction(computePrePostPairs((IInternalAction) transition));
	}

	private HashRelation<IPredicate, IPredicate> computePrePostPairs(IInternalAction action) {
		if (mAbstractionCache.containsKey(action)) {
			return mAbstractionCache.get(action);
		}

		final HashRelation<IPredicate, IPredicate> abstraction = new HashRelation<>();
		for (final IPredicate pre : mPredicates) {
			for (final IPredicate post : mPredicates) {
				final IHoareTripleChecker.Validity result = mChecker.checkInternal(pre, action, post);
				assert result == Validity.VALID || result == Validity.INVALID : "Could not determine abstraction";
				if (result == Validity.VALID) {
					abstraction.addPair(pre, post);
				}

			}
		}
		mAbstractionCache.put(action, abstraction);

		return abstraction;
	}

	private UnmodifiableTransFormula concretizeAbstraction(HashRelation<IPredicate, IPredicate> prePostPairs) {
		final TransFormulaBuilder tfb = new TransFormulaBuilder(null, null, true, null, true, null, true);

		// Construct a substitution to apply to postconditions.
		final Map<TermVariable, Term> substitutionMap = new HashMap<>(mAllVariables.size());
		for (final IProgramVar variable : mAllVariables) {
			final TermVariable original = variable.getTermVariable();
			final TermVariable replacement = mManagedScript.constructFreshCopy(original);
			substitutionMap.put(original, replacement);

			// All variables are output variables (may change arbitrarily, unless
			// constrained below).
			tfb.addOutVar(variable, replacement);
		}
		final Substitution postSubstitution = new Substitution(mManagedScript.getScript(), substitutionMap);

		final List<Term> conjuncts = new ArrayList<>(prePostPairs.size());

		for (final Map.Entry<IPredicate, IPredicate> pair : prePostPairs) {
			final IPredicate pre = pair.getKey();
			final IPredicate post = pair.getValue();

			// Add program constants.
			for (final IProgramConst constant : pre.getConstants()) {
				tfb.addProgramConst(constant);
			}
			for (final IProgramConst constant : post.getConstants()) {
				tfb.addProgramConst(constant);
			}

			// Free variables of the precondition are input variables.
			for (final IProgramVar variable : pre.getVars()) {
				tfb.addInVar(variable, variable.getTermVariable());
			}

			final Term postFormula = postSubstitution.transform(post.getFormula());
			final Term conjunct = SmtUtils.implies(mManagedScript.getScript(), pre.getFormula(), postFormula);
			conjuncts.add(conjunct);
		}

		tfb.setFormula(SmtUtils.and(mManagedScript.getScript(), conjuncts));

		return tfb.finishConstruction(mManagedScript);
	}
}
