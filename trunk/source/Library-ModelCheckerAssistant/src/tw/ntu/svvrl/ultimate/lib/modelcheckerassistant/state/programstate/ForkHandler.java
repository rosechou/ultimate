package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate;

import java.util.Map;

import de.uni_freiburg.informatik.ultimate.boogie.ast.ForkStatement;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ForkThreadCurrent;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.explorer.ProgramStateExplorer;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadStateTransition;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.threadtransitiontoolkit.ThreadStatementsExecutor;

public class ForkHandler {
	final ProgramState mProgramState;
	final ThreadStateTransition mTrans;
	final ProgramStateExplorer mProgramStateExplorer;
	
	public ForkHandler(final ProgramState programState, final ThreadStateTransition trans
						, final ProgramStateExplorer pe) {
		mProgramState = new ProgramState(programState);
		mTrans = trans;
		mProgramStateExplorer = pe;
	}

	/**
	 * Fork to two thread states.
	 * @return
	 * 		a new ProgramState whose <code>mThreadStates</code>
	 * 		are updated. (add a new (forked) thread state
	 * 		and update the original thread state.)
	 */
	public ProgramState doFork() {
		final long currentThreadID = mTrans.getThreadID();
		final ThreadState currentThreadState = mProgramState.getThreadStateByID(currentThreadID);
		final ForkStatement forkStmt = ((ForkThreadCurrent) mTrans.getIcfgEdge()).getForkStatement();
		final String forkProcName = forkStmt.getProcedureName();
		
		/**
		 * Next state in the original thread.
		 * Their valuation are equal, so just pass the original valuation to construct a new one.
		 * After that, the next state's Icfg location needs to be updated.
		 */
		final ThreadState currentNextState 
				= new ThreadState(currentThreadState.getValuation(), currentThreadState);
		currentNextState.setCorrespondingIcfgLoc((BoogieIcfgLocation) mTrans.getIcfgEdge().getTarget());
		
		
		/**
		 * Next state in the new thread.
		 * Generate it by using {@link ThreadStatementsExecutor}.
		 * After that, set the resulting state's Icfg location to the
		 * new thread's entry node.
		 */
		final ThreadStatementsExecutor stmtExecutor = new ThreadStatementsExecutor(
				forkStmt, currentThreadState, ThreadStatementsExecutor.execType.realExec, mProgramStateExplorer);
		final ThreadState otherNextState =  stmtExecutor.execute();
		BoogieIcfgLocation threadEntryNode = mProgramStateExplorer.getEntryNode(forkProcName);
		otherNextState.setCorrespondingIcfgLoc(threadEntryNode);
		
		
		/**
		 * update the original thread state
		 */
		mProgramState.updateThreadState(currentNextState.getThreadID(), currentNextState);
		/**
		 * add a new (forked) thread state
		 */
		mProgramState.updateThreadState(otherNextState.getThreadID(), otherNextState);
		
		return mProgramState;
	}
}
