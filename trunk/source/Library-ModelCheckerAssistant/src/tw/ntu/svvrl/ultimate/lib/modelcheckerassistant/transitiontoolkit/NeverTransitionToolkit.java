package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import de.uni_freiburg.informatik.ultimate.automata.nestedword.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.neverstate.NeverState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.TransitionToolkit.AutTypes;

public class NeverTransitionToolkit {

	private final OutgoingInternalTransition<CodeBlock, NeverState> mTrans;
	private CodeBlockExecutor<NeverState> mCodeBlockExecutor = null;
	private final AutTypes mAutType;
	
	public NeverTransitionToolkit(final OutgoingInternalTransition<CodeBlock, NeverState> trans, final NeverState state) {
		mTrans = trans;
		mAutType = TransitionToolkit.AutTypes.NeverClaim;
		mCodeBlockExecutor = new CodeBlockExecutor<NeverState>(trans.getLetter(), state, mAutType);
	}
	
	/**
	 * For NeverClaim Automata, we need to know the current program valuation.
	 * @param correspondingThreadState
	 * 		Current program State which contains the valuation.
	 * @return
	 * 		True if this trans is enable for correspondingThreadState, false if not.
	 */
	public boolean checkTransEnable(ProgramState correspondingProgramState) {
		mCodeBlockExecutor.setCorrespondingProgramState(correspondingProgramState);
		NeverState targetState = mTrans.getSucc();
		mCodeBlockExecutor.setTargrtState(targetState);
		return mCodeBlockExecutor.checkEnable();
	}
	

	/**
	 * For NeverClaim Automata, we need to know the current program valuation.
	 * @param correspondingThreadState
	 * 		Current thread State which contains the valuation.
	 * @return
	 * 		A new Never state reached after doing this transition(edge).
	 */
	public NeverState doTransition(ProgramState correspondingProgramState) {
		mCodeBlockExecutor.setCorrespondingProgramState(correspondingProgramState);
		NeverState targetState = mTrans.getSucc();
		mCodeBlockExecutor.setTargrtState(targetState);
		NeverState newState = mCodeBlockExecutor.execute();
		return newState;
	}
}
