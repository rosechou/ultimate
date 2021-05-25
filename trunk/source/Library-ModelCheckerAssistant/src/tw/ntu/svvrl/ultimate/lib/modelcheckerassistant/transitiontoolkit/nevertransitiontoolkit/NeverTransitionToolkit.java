package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.nevertransitiontoolkit;

import de.uni_freiburg.informatik.ultimate.automata.nestedword.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.neverstate.NeverState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.CodeBlockExecutor;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.ITransitionToolkit;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.ITransitionToolkit.AutTypes;

public class NeverTransitionToolkit implements ITransitionToolkit<NeverState> {

	private final OutgoingInternalTransition<CodeBlock, NeverState> mTrans;
	private final CodeBlockExecutor<NeverState> mCodeBlockExecutor;
	private final AutTypes mAutType;
	
	public NeverTransitionToolkit(final OutgoingInternalTransition<CodeBlock, NeverState> trans
			, final NeverState state, final ProgramState correspondingProgramState) {
		mTrans = trans;
		mAutType = ITransitionToolkit.AutTypes.NeverClaim;
		NeverState targetState = mTrans.getSucc();
		mCodeBlockExecutor = new CodeBlockExecutor<NeverState>(trans.getLetter(), state, mAutType
								, correspondingProgramState, targetState);
	}
	
	/**
	 * For NeverClaim Automata, we need to know the current program valuation.
	 * @param correspondingThreadState
	 * 		Current program State which contains the valuation.
	 * @return
	 * 		True if this trans is enable for correspondingThreadState, false if not.
	 */
	public boolean checkTransEnable() {
		return mCodeBlockExecutor.checkEnable();
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
		NeverState newState = mCodeBlockExecutor.execute();
		return newState;
	}
}
