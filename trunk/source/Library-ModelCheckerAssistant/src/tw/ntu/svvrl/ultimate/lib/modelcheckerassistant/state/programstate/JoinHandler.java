package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate;

import java.util.List;

import de.uni_freiburg.informatik.ultimate.boogie.ast.ForkStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.JoinStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.VariableLHS;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ForkThreadCurrent;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.JoinThreadCurrent;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadStateTransition;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.threadtransitiontoolkit.ThreadCodeBlockExecutor;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.threadtransitiontoolkit.ThreadExprEvaluator;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.threadtransitiontoolkit.ThreadStatementsExecutor;

public class JoinHandler {
	final ProgramState mProgramState;
	final ThreadStateTransition mTrans;
	
	public JoinHandler(final ProgramState programState, final ThreadStateTransition trans) {
		mProgramState = new ProgramState(programState);
		mTrans = trans;
	}
	

	public ProgramState doJoin() {
		final long currentThreadID = mTrans.getThreadID();
		final ThreadState currentThreadState = mProgramState.getThreadStateByID(currentThreadID);
		final ProcInfo currentProcInfo = currentThreadState.getCurrentProc();
		final JoinStatement joinStmt = ((JoinThreadCurrent) mTrans.getIcfgEdge()).getJoinStatement();
		
		
		/**
		 * Check whether the target thread is terminate.
		 */
		final ThreadExprEvaluator exprEvaluator = new ThreadExprEvaluator(currentThreadState);
		final long targetThreadID = (long) exprEvaluator.evaluate(joinStmt.getThreadID()[0]);
		final ThreadState targetState = mProgramState.getThreadStateByID(targetThreadID);
		final String targetProcName = targetState.getCurrentProc().getProcName();
		/**
		 * If the target thread is not at the exit node, block current thread.
		 */
		if(!mProgramState.getExitNode(targetProcName).equals(targetState.getCorrespondingIcfgLoc())) {
			currentThreadState.block();
		} else {

			final ThreadState currentNextState
				= ThreadCodeBlockExecutor.doReturnRoutines(targetState, currentProcInfo, joinStmt.getLhs());
			currentNextState.setCorrespondingIcfgLoc((BoogieIcfgLocation) mTrans.getIcfgEdge().getTarget());
			
			/**
			 * update the original thread state
			 */
			mProgramState.updateThreadState(currentNextState.getThreadID(), currentNextState);
			/**
			 * remove the forked thread state
			 */
			mProgramState.removeThreadState(targetThreadID);
		}
		
		return mProgramState;
	}
}

