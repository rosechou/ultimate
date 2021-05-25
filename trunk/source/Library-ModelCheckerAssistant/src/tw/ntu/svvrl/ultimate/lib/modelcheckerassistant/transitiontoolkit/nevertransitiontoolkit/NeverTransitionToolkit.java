package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.nevertransitiontoolkit;

import de.uni_freiburg.informatik.ultimate.automata.nestedword.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.neverstate.NeverState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.CodeBlockExecutor;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.TransitionToolkit;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.TransitionToolkit.AutTypes;

public class NeverTransitionToolkit extends TransitionToolkit<NeverState> {

	private final OutgoingInternalTransition<CodeBlock, NeverState> mTrans;
	
	public NeverTransitionToolkit(final OutgoingInternalTransition<CodeBlock, NeverState> trans
			, final NeverState state, final ProgramState correspondingProgramState) {
		mTrans = trans;
		mCodeBlockExecutor = new NeverCodeBlockExecutor(trans.getLetter(), state
								, correspondingProgramState);
	}
	

	/**
	 * For NeverClaim Automata, we need to know the current program valuation.
	 * @param correspondingThreadState
	 * 		Current thread State which contains the valuation.
	 * @return
	 * 		A new Never state reached after doing this transition(edge).
	 */
	@Override
	public NeverState doTransition() {
		return mTrans.getSucc();
	}
}
