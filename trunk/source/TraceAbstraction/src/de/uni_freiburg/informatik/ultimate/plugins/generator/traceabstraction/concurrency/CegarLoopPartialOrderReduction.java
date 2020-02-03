package de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.concurrency;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryException;
import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryServices;
import de.uni_freiburg.informatik.ultimate.automata.AutomataOperationCanceledException;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.INwaOutgoingLetterAndTransitionProvider;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.IsEmpty;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.Union;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.reduction.CachedIndependenceRelation;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.reduction.DualPartialOrderInclusionCheck;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.reduction.IIndependenceRelation;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.CfgSmtToolkit;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.IcfgUtils;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IIcfg;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IIcfgTransition;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgLocation;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.debugidentifiers.DebugIdentifier;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.hoaretriple.IHoareTripleChecker;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.SmtUtils.SimplificationTechnique;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.SmtUtils.XnfConversionTechnique;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.predicates.IPredicate;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.predicates.IPredicateUnifier;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.predicates.PredicateFactory;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.predicates.PredicateUnifier;
import de.uni_freiburg.informatik.ultimate.lib.tracecheckerutils.independencerelation.HoareAbstraction;
import de.uni_freiburg.informatik.ultimate.lib.tracecheckerutils.independencerelation.SemanticIndependenceRelation;
import de.uni_freiburg.informatik.ultimate.lib.tracecheckerutils.singletracecheck.InterpolationTechnique;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.BasicCegarLoop;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.CegarLoopStatisticsDefinitions;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.PredicateFactoryForInterpolantAutomata;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.TraceAbstractionUtils;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.interpolantautomata.transitionappender.DeterministicInterpolantAutomaton;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.preferences.TAPreferences;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.preferences.TAPreferences.InterpolantAutomatonEnhancement;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.preferences.TraceAbstractionPreferenceInitializer.HoareTripleChecks;

public class CegarLoopPartialOrderReduction<LETTER extends IIcfgTransition<?>> extends BasicCegarLoop<LETTER> {

	private final IIndependenceRelation<IPredicate, LETTER> mRelation1;
	private final CachedIndependenceRelation<IPredicate, LETTER> mRelation2;
	private final HoareAbstraction mHoareAbstraction;

	private final IHoareTripleChecker mHoareChecker;
	private final IPredicateUnifier mPredUnifier;

	private INwaOutgoingLetterAndTransitionProvider<LETTER, IPredicate> mProof = null;

	public CegarLoopPartialOrderReduction(final DebugIdentifier name, final IIcfg<?> rootNode,
			final CfgSmtToolkit csToolkit, final PredicateFactory predicateFactory, final TAPreferences taPrefs,
			final Collection<? extends IcfgLocation> errorLocs, final IUltimateServiceProvider services) {
		super(name, rootNode, csToolkit, predicateFactory, taPrefs, errorLocs,
				InterpolationTechnique.Craig_TreeInterpolation, false, services);

		mPredUnifier = new PredicateUnifier(mLogger, mServices, mCsToolkit.getManagedScript(), predicateFactory,
				mCsToolkit.getSymbolTable(), SimplificationTechnique.SIMPLIFY_DDA,
				XnfConversionTechnique.BOTTOM_UP_WITH_LOCAL_SIMPLIFICATION);
		mHoareChecker = TraceAbstractionUtils.constructEfficientHoareTripleCheckerWithCaching(mServices,
				HoareTripleChecks.MONOLITHIC, mCsToolkit, mPredUnifier);

		final Set<IProgramVar> allVars = IcfgUtils.collectAllProgramVars(rootNode.getCfgSmtToolkit());
		mHoareAbstraction = new HoareAbstraction(csToolkit.getManagedScript(), mHoareChecker, Collections.emptySet(),
				allVars);

		mRelation1 = new CachedIndependenceRelation<>(
				new SemanticIndependenceRelation<>(services, csToolkit.getManagedScript(), true, false));
		mRelation2 = new CachedIndependenceRelation<>(new SemanticIndependenceRelation<>(services,
				csToolkit.getManagedScript(), true, false, mHoareAbstraction));
	}

	@Override
	protected boolean isAbstractionEmpty() throws AutomataOperationCanceledException {
		if (mProof == null) {
			mCounterexample = new IsEmpty<LETTER, IPredicate>(new AutomataLibraryServices(mServices),
					(INwaOutgoingLetterAndTransitionProvider<LETTER, IPredicate>) mAbstraction).getNestedRun();
			return mCounterexample == null;
		}

		final DualPartialOrderInclusionCheck<IPredicate, IPredicate, LETTER> check = new DualPartialOrderInclusionCheck<>(
				mRelation1, mRelation2, (INwaOutgoingLetterAndTransitionProvider<LETTER, IPredicate>) mAbstraction,
				mProof, true);
		final boolean result = check.getResult();
		if (!result) {
			mCounterexample = check.getCounterexample();
		}
		return result;
	}

	@Override
	protected boolean refineAbstraction() throws AutomataLibraryException {
		mCegarLoopBenchmark.start(CegarLoopStatisticsDefinitions.AutomataDifference.toString());

		try {
			final boolean conservativeSuccessorCandidateSelection = mPref
					.interpolantAutomatonEnhancement() == InterpolantAutomatonEnhancement.PREDICATE_ABSTRACTION_CONSERVATIVE;
			final boolean cannibalize = mPref
					.interpolantAutomatonEnhancement() == InterpolantAutomatonEnhancement.PREDICATE_ABSTRACTION_CANNIBALIZE;

			final IHoareTripleChecker htc;
			if (mRefinementEngine.getHoareTripleChecker() != null) {
				htc = mRefinementEngine.getHoareTripleChecker();
			} else {
				htc = TraceAbstractionUtils.constructEfficientHoareTripleCheckerWithCaching(mServices,
						HoareTripleChecks.MONOLITHIC, mCsToolkit, mRefinementEngine.getPredicateUnifier());
			}

			final INwaOutgoingLetterAndTransitionProvider<LETTER, IPredicate> detInterpolAutomaton = new DeterministicInterpolantAutomaton<LETTER>(
					mServices, mCsToolkit, htc, mInterpolAutomaton, mRefinementEngine.getPredicateUnifier(),
					conservativeSuccessorCandidateSelection, cannibalize);

			if (mProof == null) {
				mProof = detInterpolAutomaton;
			} else {
				mProof = new Union<LETTER, IPredicate>(new AutomataLibraryServices(mServices),
						new PredicateFactoryForInterpolantAutomata(null, mPredicateFactory, false), mProof,
						detInterpolAutomaton).getResult();
			}

			mRelation2.clearCache();
			final Set<IPredicate> unifiedPredicates = mInterpolAutomaton.getStates().stream()
					.map(mPredUnifier::getOrConstructPredicate).collect(Collectors.toSet());
			mHoareAbstraction.addPredicates(unifiedPredicates);
		} finally {
			mCegarLoopBenchmark.stop(CegarLoopStatisticsDefinitions.AutomataDifference.toString());
		}

		return true;
	}
}
