package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.threadtransitiontoolkit;

import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.explorer.ProgramStateExplorer;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadStateTransition;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.TransitionToolkit;

public class ThreadTransitionToolkit extends TransitionToolkit<ThreadState> {
	private final ThreadStateTransition mTrans;
	
	public ThreadTransitionToolkit(final ThreadStateTransition trans, final ThreadState state
								, final ProgramStateExplorer pe) {
		mTrans = trans;
		IcfgEdge edge = trans.getIcfgEdge();
		if(edge instanceof CodeBlock) {
			final CodeBlock codeBlock = (CodeBlock) edge;
			mCodeBlockExecutor = new ThreadCodeBlockExecutor(codeBlock, state, pe);
		} else {
			throw new UnsupportedOperationException("RCFG edge is not in type CodeBlock.");
		}
	}
	
	/**
	 * For Program Automata
	 * Execute the {@link CodeBlock} on the edge.
	 * @return
	 * 		A new Thread state reached after doing this transition(edge).
	 */
	@Override
	public ThreadState doTransition() {
		final ThreadState newState = ((ThreadCodeBlockExecutor) mCodeBlockExecutor).execute();
		final BoogieIcfgLocation correspondingLoc 
					= (BoogieIcfgLocation) mTrans.getIcfgEdge().getTarget();
		newState.setCorrespondingIcfgLoc(correspondingLoc);
		return newState;
	}
	
	/**
	 * For partial order reduction.
	 * @see Holzmann G.J., Peled D. (1995) An Improvement in Formal Verification.
	 * 		In: Hogrefe D., Leue S. (eds) Formal Description Techniques VII.
	 * 		IFIP Advances in Information and Communication Technology. Springer, Boston, MA.
	 * 		https://doi.org/10.1007/978-0-387-34878-0_13
	 * @return true if this transition do not read or write global variable, false else.
	 */
	public boolean checkAccessOnlyLocalVar() {
		return ((ThreadCodeBlockExecutor) mCodeBlockExecutor).checkAccessOnlyLocalVar();
	}
}
