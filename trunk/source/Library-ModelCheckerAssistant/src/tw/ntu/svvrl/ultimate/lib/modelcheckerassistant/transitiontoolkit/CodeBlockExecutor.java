package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import org.apache.commons.lang3.NotImplementedException;

import de.uni_freiburg.informatik.ultimate.blockencoding.converter.ShortcutCodeBlock;
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
	private final ExprEvaluator mExprEvaluator;
	
	public CodeBlockExecutor(final ExprEvaluator exprEvaluator) {
		mExprEvaluator = exprEvaluator;
	}
	

	public boolean checkEnable(final IcfgEdge edge) {
		StatementChecker statementChecker = new StatementChecker(mExprEvaluator);
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
		ProgramState newState;
		if(mEdge instanceof StatementSequence) {
			newState = executeStatementSequence((StatementSequence) mEdge);
		} else if(mEdge instanceof Call) {
			newState = executeCall((Call) mEdge);
		} else if(mEdge instanceof Summary) {
			newState = executeSummary((Summary) mEdge);
		} else if(mEdge instanceof Return) {
			newState = executeReturn((Return) mEdge);
		} else if(mEdge instanceof ForkThreadCurrent) {
			newState = executeForkThreadCurrent((ForkThreadCurrent) mEdge);
		} else if(mEdge instanceof ForkThreadOther) {
			newState = executeForkThreadOther((ForkThreadOther) mEdge);
		} else if(mEdge instanceof JoinThreadCurrent) {
			newState = executeJoinThreadCurrent((JoinThreadCurrent) mEdge);
		} else if(mEdge instanceof JoinThreadOther) {
			newState = executeJoinThreadOther((JoinThreadOther) mEdge);
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
		} else if(mEdge instanceof ShortcutCodeBlock) {
			throw new UnsupportedOperationException("Error: " + mEdge.getClass().getSimpleName()
					+ " is not supported.");
		} else {
			throw new UnsupportedOperationException("Error: " + mEdge.getClass().getSimpleName()
					+ " is not supported.");
		}
		return null;
	}

	private ProgramState executeStatementSequence(final StatementSequence stmtSeq) {
		StatementExecutor statementExecutor = new StatementExecutor(mExprEvaluator);
		return null;
	}

	private ProgramState executeCall(final Call call) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeSummary(final Summary summary) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeReturn(final Return returnn) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeForkThreadCurrent(final ForkThreadCurrent forkThreadCurrent) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeForkThreadOther(final ForkThreadOther forkThreadOther) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeJoinThreadCurrent(final JoinThreadCurrent joinThreadCurrent) {
		// TODO Auto-generated method stub
		return null;
	}

	private ProgramState executeJoinThreadOther(final JoinThreadOther joinThreadOther) {
		// TODO Auto-generated method stub
		return null;
	}


	




}
