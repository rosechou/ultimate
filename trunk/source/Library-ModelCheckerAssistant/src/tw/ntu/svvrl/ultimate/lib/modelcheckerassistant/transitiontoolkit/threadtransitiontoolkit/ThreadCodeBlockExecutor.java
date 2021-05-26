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
			if(mCurrentState.getCallerProc().equals(TargetProcName)) {
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
	
	private boolean containsCall(List<IcfgEdge> otherEdges) {
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
		mCurrentState = new ThreadState(newState);
	}
	
	public ThreadState getCurrentState() {
		return mCurrentState;
	}

	private void executeStatementSequence(final StatementSequence stmtSeq) {
		final List<Statement> stmts = stmtSeq.getStatements();
		final ThreadStatementsExecutor statementExecutor = new ThreadStatementsExecutor(stmts, (ThreadState) mCurrentState);
		moveToNewState(statementExecutor.execute());
	}

	private void executeCall(final Call call) {
		final CallStatement callStmt = call.getCallStatement();
		final ThreadStatementsExecutor statementExecutor = new ThreadStatementsExecutor(callStmt, (ThreadState) mCurrentState);
		moveToNewState(statementExecutor.execute());
	}

	private void executeSummary(final Summary summary) {
		if(summary.calledProcedureHasImplementation()) {
			throw new UnsupportedOperationException("Error: Summary with"
					+ " implementation is not supported.");
		}
	}

	private void executeReturn(final Return returnn) {
		final CallStatement correspondingCallStmt = returnn.getCallStatement();
		final ThreadStatementsExecutor statementExecutor = new ThreadStatementsExecutor((ThreadState) mCurrentState);
		final String currentProcName = ((ThreadState) mCurrentState).getCurrentProc();
		final String returnProcName = ((ThreadState) mCurrentState).getCallerProc();
		
		/**
		 * assign return value to lhs(s).
		 */
		final List<String> outParamNames = ((ThreadState) mCurrentState).getProc2OutParams().get(currentProcName);
		final VariableLHS[] lhss = correspondingCallStmt.getLhs();
		assert(lhss.length == outParamNames.size());
		for(int i = 0; i < lhss.length; i++) {
			final String lhsName = lhss[i].getIdentifier();
			final Object v = ((ThreadState) mCurrentState).getValuationCopy().lookUpValue(currentProcName, outParamNames.get(i));
			statementExecutor.updateThreadState(returnProcName, lhsName, v);
		}
		
		/**
		 * set all <code>currentProcName</code>'s local variables to null.
		 */
		final Map<String, Object> id2v = ((ThreadState) mCurrentState).getValuationCopy().getProcOrFuncId2V(currentProcName);
		for(final String varName : id2v.keySet()) {
			statementExecutor.updateThreadState(currentProcName, varName, null);
		}
		
		moveToNewState(statementExecutor.getCurrentState());
		((ThreadState) mCurrentState).popProc();
	}

	private void executeForkThreadCurrent(final ForkThreadCurrent forkThreadCurrent) {
		// TODO Auto-generated method stub
	}

	private void executeForkThreadOther(final ForkThreadOther forkThreadOther) {
		// TODO Auto-generated method stub
	}

	private void executeJoinThreadCurrent(final JoinThreadCurrent joinThreadCurrent) {
		// TODO Auto-generated method stub
	}

	private void executeJoinThreadOther(final JoinThreadOther joinThreadOther) {
		// TODO Auto-generated method stub
	}

}
