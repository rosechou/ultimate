package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.threadtransitiontoolkit;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;

import de.uni_freiburg.informatik.ultimate.boogie.ast.CallStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.VariableLHS;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Call;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ForkThreadCurrent;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ForkThreadOther;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.GotoEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.JoinThreadCurrent;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.JoinThreadOther;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ParallelComposition;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Return;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.SequentialComposition;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.StatementSequence;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Summary;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProcInfo;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.CodeBlockExecutor;

public class ThreadCodeBlockExecutor extends CodeBlockExecutor<ThreadState> {
	/**
	 * For Program Automata.
	 * @param codeBlock
	 * @param state
	 * @param autType
	 */
	public ThreadCodeBlockExecutor(final CodeBlock codeBlock, final ThreadState state) {
		mCodeBlock = codeBlock;
		mCurrentState = state;
	}
	


	public boolean checkEnable() {
		/**
		 * Waiting for other thread's termination.
		 */
		if(mCurrentState.isBlocked()) {
			return false;
		}
		
		if(mCodeBlock instanceof StatementSequence) {
			List<Statement> stmts = ((StatementSequence) mCodeBlock).getStatements();
			final ThreadStatementsChecker statementChecker = new ThreadStatementsChecker(stmts, mCurrentState);
			return statementChecker.checkStatementsEnable();
		} else if(mCodeBlock instanceof Return) {
			/**
			 * The caller procedure (top-1)
			 * and return destination's procedure should match.
			 */
			String TargetProcName = mCodeBlock.getSucceedingProcedure();
			if(mCurrentState.getCallerProc().getProcName().equals(TargetProcName)) {
				return true;
			} else {
				return false;
			}
		} else if(mCodeBlock instanceof Summary) {
			/**
			 * If there are {@link Call} and {@link Summary}
			 * at the same CFG location, then block the {@link Summary}.
			 * (Force the execution to execute {@link Call} and {@link Return})
			 * later.)
			 */
			List<IcfgEdge> otherEdges = mCodeBlock.getSource().getOutgoingEdges();
			if(containsCall(otherEdges)) {
				return false;
			}
			return true;
		} else if(mCodeBlock instanceof ParallelComposition) {
			/**
			 * This type of edge will only occur when Size of code block is not set to "SingleStatement"
			 * This case is not yet implemented because I'm lazy.
			 * (one of the preferences in
			 * de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder)
			 */
			throw new NotImplementedException(ParallelComposition.class.getSimpleName()
					+ "is not yet implemented.");
		} else if(mCodeBlock instanceof SequentialComposition) {
			/**
			 * This type of edge will only occur when Size of code block is not set to "SingleStatement"
			 * This case is not yet implemented because I'm lazy.
			 * (one of the preferences in
			 * de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder)
			 */
			throw new NotImplementedException(ParallelComposition.class.getSimpleName()
					+ "is not yet implemented.");
		} else {
			// other edge types are OK.
			return true;
		}
	}
	
	/**
	 * Whether other edges(if exist) contain {@link Call} statement.
	 * It is used for {@link Summary} enable check.
	 * @param otherEdges
	 * @return true if yes, false if no.
	 */
	private boolean containsCall(final List<IcfgEdge> otherEdges) {
		for(final IcfgEdge edge : otherEdges) {
			if(edge instanceof Call) {
				return true;
			}
		}
		return false;
	}

	public ThreadState execute() {
		if(mCodeBlock instanceof StatementSequence) {
			executeStatementSequence((StatementSequence) mCodeBlock);
		} else if(mCodeBlock instanceof Call) {
			executeCall((Call) mCodeBlock);
		} else if(mCodeBlock instanceof Summary) {
			executeSummary((Summary) mCodeBlock);
		} else if(mCodeBlock instanceof Return) {
			executeReturn((Return) mCodeBlock);
		} else if(mCodeBlock instanceof ForkThreadCurrent) {
			executeForkThreadCurrent((ForkThreadCurrent) mCodeBlock);
		} else if(mCodeBlock instanceof ForkThreadOther) {
			executeForkThreadOther((ForkThreadOther) mCodeBlock);
		} else if(mCodeBlock instanceof JoinThreadCurrent) {
			executeJoinThreadCurrent((JoinThreadCurrent) mCodeBlock);
		} else if(mCodeBlock instanceof JoinThreadOther) {
			executeJoinThreadOther((JoinThreadOther) mCodeBlock);
		} else if(mCodeBlock instanceof ParallelComposition) {
			/**
			 * This type of edge will only occur when Size of code block is not set to "SingleStatement"
			 * This case is not yet implemented because I'm lazy.
			 * (one of the preferences in
			 * de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder)
			 */
			throw new NotImplementedException(ParallelComposition.class.getSimpleName()
					+ "is not yet implemented.");
		} else if(mCodeBlock instanceof SequentialComposition) {
			/**
			 * This type of edge will only occur when Size of code block is not set to "SingleStatement"
			 * This case is not yet implemented because I'm lazy.
			 * (one of the preferences in
			 * de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder)
			 */
			throw new NotImplementedException(ParallelComposition.class.getSimpleName()
					+ "is not yet implemented.");
		} else if(mCodeBlock instanceof GotoEdge) {
			throw new UnsupportedOperationException("Suppose the type " + mCodeBlock.getClass().getSimpleName()
					+ " should not appear in the resulting CFG");
		} else {
			throw new UnsupportedOperationException("Error: " + mCodeBlock.getClass().getSimpleName()
					+ " is not supported.");
		}
		return mCurrentState;
	}
	
	private void moveToNewState(final ThreadState newState) {
		mCurrentState = newState;
	}

	private void executeStatementSequence(final StatementSequence stmtSeq) {
		final List<Statement> stmts = stmtSeq.getStatements();
		final ThreadStatementsExecutor statementExecutor 
			= new ThreadStatementsExecutor(stmts, mCurrentState, ThreadStatementsExecutor.execType.realExec);
		moveToNewState(statementExecutor.execute());
	}

	private void executeCall(final Call call) {
		final CallStatement callStmt = call.getCallStatement();
		final ThreadStatementsExecutor statementExecutor 
			= new ThreadStatementsExecutor(callStmt, mCurrentState, ThreadStatementsExecutor.execType.realExec);
		moveToNewState(statementExecutor.execute());
	}

	private void executeSummary(final Summary summary) {
		/**
		 * If called Procedure Has Implementation,
		 * Force it to execute {@link CallStatement}
		 * and never reach here.
		 */
		if(summary.calledProcedureHasImplementation()) {
			throw new UnsupportedOperationException("Error: Summary with"
					+ " implementation is not supported.");
		}
		/**
		 * If called Procedure Has no Implementation,
		 * Do nothing.
		 */
	}

	private void executeReturn(final Return returnn) {
		final CallStatement correspondingCallStmt = returnn.getCallStatement();
		final ProcInfo returnDestinationProc = mCurrentState.getCallerProc();
		
		moveToNewState(doReturnRoutines(mCurrentState, returnDestinationProc
				, correspondingCallStmt.getLhs()));
	}

	private void executeForkThreadCurrent(final ForkThreadCurrent forkThreadCurrent) {
		throw new UnsupportedOperationException(ForkThreadCurrent.class.getSimpleName()
				+ "is handled at other place.");
	}

	private void executeForkThreadOther(final ForkThreadOther forkThreadOther) {
		/**
		 * Cannot produce this case.
		 */
		throw new NotImplementedException(JoinThreadOther.class.getSimpleName()
				+ "is not yet implemented.");
	}

	private void executeJoinThreadCurrent(final JoinThreadCurrent joinThreadCurrent) {
		throw new UnsupportedOperationException(JoinThreadCurrent.class.getSimpleName()
				+ "is handled at other place.");
	}

	private void executeJoinThreadOther(final JoinThreadOther joinThreadOther) {
		/**
		 * Cannot produce this case.
		 */
		throw new NotImplementedException(JoinThreadOther.class.getSimpleName()
				+ "is not yet implemented.");
	}

	public static ThreadState doReturnRoutines(final ThreadState fromState, final ProcInfo toInfo
			, final VariableLHS[] stmtLhss) {

		final ProcInfo fromProc = fromState.getCurrentProc();
		final String fromProcName = fromProc.getProcName();
		final String toProcName = toInfo.getProcName();
		
		/**
		 * Retrieve return values
		 */
		final List<String> outParamNames = fromState.getProc2OutParams().get(fromProcName);
		final VariableLHS[] lhss = stmtLhss;
		final Object[] values = new Object[lhss.length];
		assert(lhss.length == outParamNames.size());
		for(int i = 0; i < lhss.length; i++) {
			final Object v = fromState.getValuationLocalCopy().lookUpValue(fromProcName, outParamNames.get(i));
			values[i] = v;
		}
		
		/**
		 * Reset the return procedure valuation.
		 */
		fromState.resetLocalValuation(toInfo.getValuationRecord());
		
		final ThreadStatementsExecutor statementExecutor
		= new ThreadStatementsExecutor(fromState, ThreadStatementsExecutor.execType.realExec);
		
		/**
		 * assign return value(s) to lhs(s).
		 */
		for(int i = 0; i < lhss.length; i++) {
			final String lhsName = lhss[i].getIdentifier();
			statementExecutor.updateThreadState(toProcName, lhsName, values[i]);
		}
		
		/**
		 * This will lead to a bug in the context of recursive procedure call.
		 * Move it to {@link CallStatement}.
		 */
//		/**
//		 * set all <code>currentProcName</code>'s local variables to null.
//		 */
//		final Map<String, Object> id2v = mCurrentState.getValuation().getProcOrFuncId2V(currentProcName);
//		for(final String varName : id2v.keySet()) {
//			statementExecutor.updateThreadState(currentProcName, varName, null);
//		}
		
		statementExecutor.getCurrentState().popProc();
		return statementExecutor.getCurrentState();
	}
}
