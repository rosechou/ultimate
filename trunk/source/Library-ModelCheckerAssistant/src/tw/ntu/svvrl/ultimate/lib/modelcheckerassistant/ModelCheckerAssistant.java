package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant;

import de.uni_freiburg.informatik.ultimate.automata.nestedword.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgContainer;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.explorer.StateExplorer;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.explorer.NeverClaimAutExplorer;

/**
 * This is a helper for the SPIN-like model checker.
 * It mainly consists of two component. One is for the exploration of
 * Boogie program states, another is for the exploration of the
 * never claim automaton which is originally represented as an LTL formula.
 * 
 * One can call the methods this class provides to imitate SPIN and
 * implement a "On the fly" automata-based model checker. 
 * 
 * @author Hong-Yang Lin 
 */

public class ModelCheckerAssistant {
	
	private final ILogger mLogger;
	private final IUltimateServiceProvider mServices;
	private final BoogieIcfgContainer mRcfgRoot;
	private final INestedWordAutomaton<CodeBlock, String> mNWA;
	
	/**
	 * The first main component, which generates program states on-the-fly and 
	 * keep exploring.
	 */
	private final StateExplorer mStateExplorer;
	
	public ModelCheckerAssistant(final INestedWordAutomaton<CodeBlock, String> nwa, final BoogieIcfgContainer rcfg,
			final ILogger logger, final IUltimateServiceProvider services) {
		
		// services and logger
		mServices = services;
		mLogger = logger;
		mRcfgRoot = rcfg;
		mNWA = nwa;
		
		mStateExplorer = new StateExplorer(rcfg);
	}
	
	public StateExplorer getStateExplorer() {
		return mStateExplorer;
	}
}
