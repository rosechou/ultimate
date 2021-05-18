package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import org.apache.commons.lang3.NotImplementedException;

import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Call;
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
	
	public CodeBlockExecutor(final ProgramState programState) {
		mCurrentProgramState = new ProgramState(programState);
	}
	

	public boolean checkEnable(final IcfgEdge edge) {
		final StatementsChecker statementChecker = new StatementsChecker(mCurrentProgramState);
		if(edge instanceof StatementSequence) {
			return statementChecker.checkStatementsEnable(((StatementSequence) edge).getStatements());
		} else if(edge instanceof ParallelComposition) {
			/**
			 * This type of edge will only occur when Size of code block is not set to "SingleStatement"
			 * This case is not yet implemented because I'm lazy.
			 * (one of the preferences in
			 * de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder)
			 */
			throw new NotImplementedException(ParallelComposition.class.getSimpleName()
					+ "is not yet implemented.");
		} else if(edge instanceof SequentialComposition) {
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
	
	public ProgramState execute(final IcfgEdge mEdge) {
		if(mEdge instanceof StatementSequence) {
			executeStatementSequence((StatementSequence) mEdge);
		} else if(mEdge instanceof Call) {
			executeCall((Call) mEdge);
		} else if(mEdge instanceof Summary) {
			executeSummary((Summary) mEdge);
		} else if(mEdge instanceof Return) {
			executeReturn((Return) mEdge);
		} else if(mEdge instanceof ForkThreadCurrent) {
			executeForkThreadCurrent((ForkThreadCurrent) mEdge);
		} else if(mEdge instanceof ForkThreadOther) {
			executeForkThreadOther((ForkThreadOther) mEdge);
		} else if(mEdge instanceof JoinThreadCurrent) {
			executeJoinThreadCurrent((JoinThreadCurrent) mEdge);
		} else if(mEdge instanceof JoinThreadOther) {
			executeJoinThreadOther((JoinThreadOther) mEdge);
		} else if(mEdge instanceof ParallelComposition) {
			/**
			 * This type of edge will only occur when Size of code block is not set to "SingleStatement"
			 * This case is not yet implemented because I'm lazy.
			 * (one of the preferences in
			 * de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder)
			 */
			throw new NotImplementedException(ParallelComposition.class.getSimpleName()
					+ "is not yet implemented.");
		} else if(mEdge instanceof SequentialComposition) {
			/**
			 * This type of edge will only occur when Size of code block is not set to "SingleStatement"
			 * This case is not yet implemented because I'm lazy.
			 * (one of the preferences in
			 * de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder)
			 */
			throw new NotImplementedException(ParallelComposition.class.getSimpleName()
					+ "is not yet implemented.");
		} else if(mEdge instanceof GotoEdge) {
			throw new UnsupportedOperationException("Suppose the type " + mEdge.getClass().getSimpleName()
					+ " should not appear in the resulting CFG");
		} else {
			throw new UnsupportedOperationException("Error: " + mEdge.getClass().getSimpleName()
					+ " is not supported.");
		}
		return mCurrentProgramState;
	}
	
	private void moveToNewState(final ProgramState newState) {
		mCurrentProgramState = newState;
	}
	
	public ProgramState getCurrentState() {
		return mCurrentProgramState;
	}

	private void executeStatementSequence(final StatementSequence stmtSeq) {
		final StatementsExecutor statementExecutor = new StatementsExecutor(mCurrentProgramState);
		statementExecutor.execute(stmtSeq.getStatements());
		moveToNewState(statementExecutor.getCurrentState());
	}

	private void executeCall(final Call call) {
		// TODO Auto-generated method stub
	}

	private void executeSummary(final Summary summary) {
		// TODO Auto-generated method stub
	}

	private void executeReturn(final Return returnn) {
		// TODO Auto-generated method stub
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
