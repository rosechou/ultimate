package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.IState;

/**
 * This class handle all issues about a transition(edge) and the statements on it.
 *
 */
public abstract class TransitionToolkit<S extends IState<S>> {
	protected CodeBlockExecutor<S> mCodeBlockExecutor;
	
	public boolean checkTransEnable() {
		return mCodeBlockExecutor.checkEnable();
	}
	
	protected abstract S doTransition();
}
