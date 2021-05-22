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
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.neverstate.NeverState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.TransitionToolkit.AutTypes;

public class CodeBlockExecutor<S> {
	private S mCurrentState;
	private final CodeBlock mCodeBlock;
	private final AutTypes mAutType;
	
	/**
	 * Only for NeverClaim Automata.
	 */
	private ProgramState mCorrespondingProgramState = null;
	/**
	 *  Only for NeverClaim Automata.
	 * If execute successfully, move to this state.
	 */
	private NeverState mTargrtState = null;
	
	@SuppressWarnings("unchecked")
	public CodeBlockExecutor(final CodeBlock codeBlock, final S state, final TransitionToolkit.AutTypes autType) {
		mCodeBlock = codeBlock;
		mAutType = autType;
		
		if(autType == TransitionToolkit.AutTypes.Program) {
			mCurrentState = (S) new ProgramState((ProgramState) state);
		} else if(autType == TransitionToolkit.AutTypes.NeverClaim) {
			mCurrentState = state;
		} else {
			throw new UnsupportedOperationException("Unsupported automata type.");
		}
	}
	
	public void setCorrespondingProgramState(ProgramState correspondingProgramState) {
		mCorrespondingProgramState = correspondingProgramState;
	}
	
	public void setTargrtState(NeverState targetState) {
		mTargrtState = targetState;
	}

	public boolean checkEnable() {
		if(mCodeBlock instanceof StatementSequence) {
			List<Statement> stmts = ((StatementSequence) mCodeBlock).getStatements();
			final StatementsChecker statementChecker = new StatementsChecker(stmts, (ProgramState) mCurrentState);
			return statementChecker.checkStatementsEnable();
		} else if(mCodeBlock instanceof Return) {
			assert mAutType == TransitionToolkit.AutTypes.Program;
			/**
			 * The caller procedure (top-1)
			 * and return destination's procedure should match.
			 */
			String TargetProcName = mCodeBlock.getSucceedingProcedure();
			if(((ProgramState) mCurrentState).getCallerProc().equals(TargetProcName)) {
				return true;
			} else {
				return false;
			}
		} else if(mCodeBlock instanceof Summary) {
			assert mAutType == TransitionToolkit.AutTypes.Program;
			/**
			 * We force the program to execute call and return,
			 * and never execute Summary.
			 */
			return false;
		} else if(mCodeBlock instanceof ParallelComposition) {
			assert mAutType == TransitionToolkit.AutTypes.Program;
			/**
			 * This type of edge will only occur when Size of code block is not set to "SingleStatement"
			 * This case is not yet implemented because I'm lazy.
			 * (one of the preferences in
			 * de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder)
			 */
			throw new NotImplementedException(ParallelComposition.class.getSimpleName()
					+ "is not yet implemented.");
		} else if(mCodeBlock instanceof SequentialComposition) {
			assert mAutType == TransitionToolkit.AutTypes.Program;
			/**
			 * This type of edge will only occur when Size of code block is not set to "SingleStatement"
			 * This case is not yet implemented because I'm lazy.
			 * (one of the preferences in
			 * de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder)
			 */
			throw new NotImplementedException(ParallelComposition.class.getSimpleName()
					+ "is not yet implemented.");
		} else {
			assert mAutType == TransitionToolkit.AutTypes.Program;
			// other edge types are OK.
			return true;
		}
	}
	
	public S execute() {
		if(mCodeBlock instanceof StatementSequence) {
			executeStatementSequence((StatementSequence) mCodeBlock);
		} else if(mCodeBlock instanceof Call) {
			executeCall((Call) mCodeBlock);
		} else if(mCodeBlock instanceof Summary) {
			throw new UnsupportedOperationException("Suppose the type " + mCodeBlock.getClass().getSimpleName()
					+ " is not enable.");
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
	
	private void moveToNewState(final ProgramState newState) {
		if(mAutType == TransitionToolkit.AutTypes.Program) {
			mCurrentState = (S) new ProgramState((ProgramState) newState);
		} else if(mAutType == TransitionToolkit.AutTypes.NeverClaim) {
			mCurrentState = (S) mTargrtState;
		} else {
			throw new UnsupportedOperationException("Unsupported automata type.");
		}
	}
	
	public S getCurrentState() {
		return mCurrentState;
	}

	private void executeStatementSequence(final StatementSequence stmtSeq) {
		final List<Statement> stmts = stmtSeq.getStatements();
		if(mAutType == TransitionToolkit.AutTypes.Program) {
			final StatementsExecutor statementExecutor = new StatementsExecutor(stmts, (ProgramState) mCurrentState);
			moveToNewState(statementExecutor.execute());
		} else if(mAutType == TransitionToolkit.AutTypes.NeverClaim) {
			final StatementsExecutor statementExecutor = new StatementsExecutor(stmts, mCorrespondingProgramState);
			moveToNewState(statementExecutor.execute());
		} else {
			throw new UnsupportedOperationException("Unsupported automata type.");
		}
	}

	private void executeCall(final Call call) {
		assert mAutType == TransitionToolkit.AutTypes.Program;
		final CallStatement callStmt = call.getCallStatement();
		final StatementsExecutor statementExecutor = new StatementsExecutor(callStmt, (ProgramState) mCurrentState);
		moveToNewState(statementExecutor.execute());
	}
	
	/**
	 * not enable transition, no need to implement.
	 */
//	private void executeSummary(final Summary summary) {
//	}

	private void executeReturn(final Return returnn) {
		assert mAutType == TransitionToolkit.AutTypes.Program;
		final CallStatement correspondingCallStmt = returnn.getCallStatement();
		final StatementsExecutor statementExecutor = new StatementsExecutor((ProgramState) mCurrentState);
		final String currentProcName = ((ProgramState) mCurrentState).getCurrentProc();
		final String returnProcName = ((ProgramState) mCurrentState).getCallerProc();
		
		/**
		 * assign return value to lhs(s).
		 */
		final List<String> outParamNames = ((ProgramState) mCurrentState).getProc2OutParams().get(currentProcName);
		final VariableLHS[] lhss = correspondingCallStmt.getLhs();
		assert(lhss.length == outParamNames.size());
		for(int i = 0; i < lhss.length; i++) {
			final String lhsName = lhss[i].getIdentifier();
			final Object v = ((ProgramState) mCurrentState).getValuationCopy().lookUpValue(currentProcName, outParamNames.get(i));
			statementExecutor.updateProgramState(returnProcName, lhsName, v);
		}
		
		/**
		 * set all <code>currentProcName</code>'s local variables to null.
		 */
		final Map<String, Object> id2v = ((ProgramState) mCurrentState).getValuationCopy().getProcOrFuncId2V(currentProcName);
		for(final String varName : id2v.keySet()) {
			statementExecutor.updateProgramState(currentProcName, varName, null);
		}
		
		moveToNewState(statementExecutor.getCurrentState());
		((ProgramState) mCurrentState).popProc();
		
		
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
