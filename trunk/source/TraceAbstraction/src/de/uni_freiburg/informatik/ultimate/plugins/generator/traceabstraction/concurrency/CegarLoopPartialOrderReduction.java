package de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.concurrency;

import java.util.Collection;

import de.uni_freiburg.informatik.ultimate.automata.AutomataOperationCanceledException;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.reduction.DualPartialOrderInclusionCheck;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.reduction.IIndependenceRelation;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.CfgSmtToolkit;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IIcfg;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IIcfgTransition;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgLocation;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.debugidentifiers.DebugIdentifier;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.predicates.IPredicate;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.predicates.PredicateFactory;
import de.uni_freiburg.informatik.ultimate.lib.tracecheckerutils.independencerelation.SemanticIndependenceRelation;
import de.uni_freiburg.informatik.ultimate.lib.tracecheckerutils.singletracecheck.InterpolationTechnique;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.BasicCegarLoop;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.preferences.TAPreferences;

public class CegarLoopPartialOrderReduction extends BasicCegarLoop<IIcfgTransition<?>> {

	private final IIndependenceRelation<IPredicate, IIcfgTransition<?>> mRelation1;
	private final IIndependenceRelation<IPredicate, IIcfgTransition<?>> mRelation2;

	public CegarLoopPartialOrderReduction(final DebugIdentifier name, final IIcfg<?> rootNode,
			final CfgSmtToolkit csToolkit, final PredicateFactory predicateFactory, final TAPreferences taPrefs,
			final Collection<? extends IcfgLocation> errorLocs, final IUltimateServiceProvider services) {
		super(name, rootNode, csToolkit, predicateFactory, taPrefs, errorLocs,
				InterpolationTechnique.Craig_TreeInterpolation, false, services);

		mRelation1 = new SemanticIndependenceRelation(services, csToolkit.getManagedScript(), true, false);
		mRelation2 = mRelation1; // TODO: independence with abstraction
	}

	@Override
	protected boolean isAbstractionEmpty() throws AutomataOperationCanceledException {
		final DualPartialOrderInclusionCheck<IPredicate, IPredicate, IIcfgTransition<?>> check = new DualPartialOrderInclusionCheck<>(
				mRelation1, mRelation2, (INestedWordAutomaton<IIcfgTransition<?>, IPredicate>) mAbstraction,
				mInterpolAutomaton, true);
		final boolean result = check.getResult();
		if (!result) {
			mCounterexample = check.getCounterexample();
		}
		return result;
	}
}
