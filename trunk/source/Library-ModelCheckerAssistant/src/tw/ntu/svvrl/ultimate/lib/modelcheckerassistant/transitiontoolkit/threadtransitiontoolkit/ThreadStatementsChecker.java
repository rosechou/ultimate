package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.threadtransitiontoolkit;

import java.util.List;

import de.uni_freiburg.informatik.ultimate.boogie.ast.AssertStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssignmentStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssumeStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.HavocStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.explorer.ProgramStateExplorer;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.StatementsChecker;

public class ThreadStatementsChecker extends StatementsChecker<ThreadState> {
	private final ProgramStateExplorer mProgramStateExplorer;
	public ThreadStatementsChecker(final List<Statement> statements, final ThreadState state
									, final ProgramStateExplorer pe) {
		super(statements);
		mState = state;
		mProgramStateExplorer = pe;
	}
	
	/**
	 * Check whether the given statements(from Icfg edge) is enable.
	 * Assignment and Havoc statements should be considered because these statement will
	 * make the origin state move to a new program state. Whether an assignment statement
	 * is enable is equal to asking whether the new state is enable after executing the 
	 * rest statements. (use recursion) 
	 * @param stmts
	 * 		list of statements
	 * @return
	 * 		true if no assume statement is violated
	 */
	public boolean checkStatementsEnable() {
		for(int i = 0; i < mStatements.size(); i++) {
			final Statement stmt = mStatements.get(i);
			if(stmt instanceof AssumeStatement) {
				// if the formula assumed is not hold, then not enable. 
				if(!checkAssumeStatement((AssumeStatement) stmt)) {
					return false;
				}
			} else if(stmt instanceof AssertStatement) {
				/**
				 * We don't check whether the assertion is satisfied or not here.
				 * Instead, we leave this check in the doTransition function.
				 * So assert statement will be skipped here.
				 */
			} else if(stmt instanceof AssignmentStatement
					|| stmt instanceof HavocStatement) {
				assert mState instanceof ThreadState;
				final ThreadStatementsExecutor stmtsExecutor
					= new ThreadStatementsExecutor(stmt, mState, ThreadStatementsExecutor.execType.check, mProgramStateExplorer);
				moveToNewState(stmtsExecutor.execute());
				ThreadStatementsChecker nextStatementsChecker 
						= new ThreadStatementsChecker(mStatements.subList(i+1, mStatements.size()), mState, mProgramStateExplorer);
				return nextStatementsChecker.checkStatementsEnable();
			}
		}
		return true;
	}
	
	protected boolean checkAssumeStatement(final AssumeStatement assumeStmt) {
		final ThreadExprEvaluator exprEvaluator = new ThreadExprEvaluator(mState, mProgramStateExplorer);
		return (boolean) exprEvaluator.evaluate(assumeStmt.getFormula());
	}
	
	private void moveToNewState(final ThreadState newState) {
		mState = newState;
	}

	public boolean checkStatementsAccessOnlyLocalVar() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean checkReturnAccessOnlyLocalVar() {
		assert mStatements.size() == 1;
		// TODO Auto-generated method stub
		return false;
	}

	public boolean checkCallAccessOnlyLocalVar() {
		assert mStatements.size() == 1;
		// TODO Auto-generated method stub
		return false;
	}

	public boolean checkForkAccessOnlyLocalVar() {
		assert mStatements.size() == 1;
		// TODO Auto-generated method stub
		return false;
	}

	public boolean checkJoinAccessOnlyLocalVar() {
		assert mStatements.size() == 1;
		// TODO Auto-generated method stub
		return false;
	}
}
