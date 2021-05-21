package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;

import de.uni_freiburg.informatik.ultimate.boogie.ast.CallStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.VariableLHS;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
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
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.programstate.ProgramState;

public class CodeBlockExecutor {
	private ProgramState mCurrentProgramState;
	private final CodeBlock mCodeBlock;
	
	public CodeBlockExecutor(final CodeBlock codeBlock, final ProgramState programState) {
		mCodeBlock = codeBlock;
		mCurrentProgramState = new ProgramState(programState);
	}
	

	public boolean checkEnable() {
		if(mCodeBlock instanceof StatementSequence) {
			List<Statement> stmts = ((StatementSequence) mCodeBlock).getStatements();
			final StatementsChecker statementChecker = new StatementsChecker(stmts, mCurrentProgramState);
			return statementChecker.checkStatementsEnable();
		} else if(mCodeBlock instanceof Return) {
			/**
			 * The caller procedure (top-1)
			 * and return destination's procedure should match.
			 */
			String TargetProcName = mCodeBlock.getSucceedingProcedure();
			if(mCurrentProgramState.getCallerProc().equals(TargetProcName)) {
				return true;
			} else {
				return false;
			}
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
	
	public ProgramState execute() {
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
		return mCurrentProgramState;
	}
	
	private void moveToNewState(final ProgramState newState) {
		mCurrentProgramState = new ProgramState(newState);
	}
	
	public ProgramState getCurrentState() {
		return mCurrentProgramState;
	}

	private void executeStatementSequence(final StatementSequence stmtSeq) {
		final List<Statement> stmts = stmtSeq.getStatements();
		final StatementsExecutor statementExecutor = new StatementsExecutor(stmts, mCurrentProgramState);
		moveToNewState(statementExecutor.execute());
	}

	private void executeCall(final Call call) {
		final CallStatement callStmt = call.getCallStatement();
		final StatementsExecutor statementExecutor = new StatementsExecutor(callStmt, mCurrentProgramState);
		moveToNewState(statementExecutor.execute());
	}
	
	private void executeSummary(final Summary summary) {
	}

	private void executeReturn(final Return returnn) {
		final CallStatement correspondingCallStmt = returnn.getCallStatement();
		final StatementsExecutor statementExecutor = new StatementsExecutor(mCurrentProgramState);
		final String currentProcName = mCurrentProgramState.getCurrentProc();
		final String returnProcName = mCurrentProgramState.getCallerProc();
		
		/**
		 * assign return value to lhs(s).
		 */
		final List<String> outParamNames = mCurrentProgramState.getProc2OutParams().get(currentProcName);
		final VariableLHS[] lhss = correspondingCallStmt.getLhs();
		assert(lhss.length == outParamNames.size());
		for(int i = 0; i < lhss.length; i++) {
			final String lhsName = lhss[i].getIdentifier();
			final Object v = mCurrentProgramState.getValuationCopy().lookUpValue(currentProcName, outParamNames.get(i));
			statementExecutor.updateProgramState(returnProcName, lhsName, v);
		}
		
		/**
		 * set all <code>currentProcName</code>'s local variables to null.
		 */
		final Map<String, Object> id2v = mCurrentProgramState.getValuationCopy().getProcOrFuncId2V(currentProcName);
		for(final String varName : id2v.keySet()) {
			statementExecutor.updateProgramState(currentProcName, varName, null);
		}
		
		moveToNewState(statementExecutor.getCurrentState());
		mCurrentProgramState.popProc();
		
		
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
