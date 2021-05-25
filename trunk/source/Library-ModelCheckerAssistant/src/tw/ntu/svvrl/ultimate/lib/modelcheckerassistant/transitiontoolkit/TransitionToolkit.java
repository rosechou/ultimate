package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.IState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.neverstate.NeverState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;

/**
 * This class handle all issues about a transition(edge) and the statements on it.
 *
 */
public abstract class TransitionToolkit<S extends IState<S>> {
	protected CodeBlockExecutor<S> mCodeBlockExecutor = null;
	
	public boolean checkTransEnable() {
		return mCodeBlockExecutor.checkEnable();
	}
	
	public abstract S doTransition();
}
