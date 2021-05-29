package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate;

import de.uni_freiburg.informatik.ultimate.boogie.ast.ForkStatement;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ForkThreadCurrent;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadStateTransition;

public class ForkHandler {
	final ProgramState mProgramState;
	final ThreadStateTransition mTrans;
	
	public ForkHandler(final ProgramState programState, final ThreadStateTransition trans) {
		mProgramState = programState;
		mTrans = trans;
	}
	
	/**
	 * Fork to two thread states.
	 * @return
	 * 		a new ProgramState whose <code>mThreadStates</code>
	 * 		are updated. (add a new (forked) thread state
	 * 		and update the original thread state.)
	 */
	public ProgramState doFork() {
		final int currentThreadID = mTrans.getThreadID();
		final ThreadState currentThreadState = mProgramState.getThreadStateByID(currentThreadID);
		final ForkStatement forkStmt = ((ForkThreadCurrent) mTrans.getIcfgEdge()).getForkStatement();
		
		/**
		 * Next state in the original thread.
		 * Their valuation are equal, so just pass the original valuation to construct a new one.
		 * After that, the next state's Icfg location needs to be updated.
		 */
		final ThreadState currentNextState 
				= new ThreadState(currentThreadState.getValuation(), currentThreadState);
		currentNextState.setCorrespondingIcfgLoc((BoogieIcfgLocation) mTrans.getIcfgEdge().getTarget());
		
		
		
		
		return null;
	}
}
