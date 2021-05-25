package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.State;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;

/**
 * This class handle all issues about a transition(edge) and the statements on it.
 *
 */
public interface TransitionToolkit<S extends State<S>> {
	public static enum AutTypes{
		Program, NeverClaim
	}
	public boolean checkTransEnable();
	public S doTransition();
}
