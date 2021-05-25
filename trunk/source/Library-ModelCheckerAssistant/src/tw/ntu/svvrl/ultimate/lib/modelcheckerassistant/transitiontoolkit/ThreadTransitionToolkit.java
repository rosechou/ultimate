package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import de.uni_freiburg.informatik.ultimate.automata.nestedword.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.neverstate.NeverState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadStateTransition;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.TransitionToolkit.AutTypes;

public class ThreadTransitionToolkit implements TransitionToolkit {
	private final ThreadStateTransition mTrans;
	private CodeBlockExecutor<ThreadState> mCodeBlockExecutor = null;
	private final AutTypes mAutType;
	
	public ThreadTransitionToolkit(final ThreadStateTransition trans, final ThreadState state) {
		mTrans = trans;
		mAutType = TransitionToolkit.AutTypes.Program;
		CodeBlock codeBlock = (CodeBlock) ((ThreadStateTransition) trans).getIcfgEdge();
		mCodeBlockExecutor = new CodeBlockExecutor<ThreadState>(codeBlock, state, mAutType);
	}
	
	public boolean checkTransEnable() {
		if(mCodeBlockExecutor != null) {
			return mCodeBlockExecutor.checkEnable();
		} else {
			throw new UnsupportedOperationException("No CodeBlockExecutor");
		}
	}
	
	/**
	 * For Program Automata
	 * Execute the {@link CodeBlock} on the edge.
	 * @return
	 * 		A new Thread state reached after doing this transition(edge).
	 */
	public ThreadState doTransition() {
		if(mAutType == AutTypes.Program) {
			if(mCodeBlockExecutor != null) {
				final ThreadState newState = mCodeBlockExecutor.execute();
				final BoogieIcfgLocation correspondingLoc 
					= (BoogieIcfgLocation) ((ThreadStateTransition) mTrans).getIcfgEdge().getTarget();
				((ThreadState) newState).setCorrespondingIcfgLoc(correspondingLoc);
				return newState;
			} else {
				throw new UnsupportedOperationException("No CodeBlockExecutor");
			}
		} else {
			throw new UnsupportedOperationException("This doTransition function is for ThreadState");
		}
	}
}
