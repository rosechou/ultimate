package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.threadtransitiontoolkit;

import de.uni_freiburg.informatik.ultimate.automata.nestedword.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.neverstate.NeverState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadStateTransition;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.CodeBlockExecutor;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.TransitionToolkit;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.TransitionToolkit.AutTypes;

public class ThreadTransitionToolkit extends TransitionToolkit<ThreadState> {
	private final ThreadStateTransition mTrans;
	
	public ThreadTransitionToolkit(final ThreadStateTransition trans, final ThreadState state) {
		mTrans = trans;
		CodeBlock codeBlock = (CodeBlock) ((ThreadStateTransition) trans).getIcfgEdge();
		mCodeBlockExecutor = new CodeBlockExecutor<ThreadState>(codeBlock, state);
	}
	
	/**
	 * For Program Automata
	 * Execute the {@link CodeBlock} on the edge.
	 * @return
	 * 		A new Thread state reached after doing this transition(edge).
	 */
	public ThreadState doTransition() {
		final ThreadState newState = mCodeBlockExecutor.execute();
		final BoogieIcfgLocation correspondingLoc 
					= (BoogieIcfgLocation) mTrans.getIcfgEdge().getTarget();
		newState.setCorrespondingIcfgLoc(correspondingLoc);
		return newState;
	}
}
