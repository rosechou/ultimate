package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.threadtransitiontoolkit;

import java.util.List;

import de.uni_freiburg.informatik.ultimate.boogie.ast.AssertStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssignmentStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssumeStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.HavocStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.ExprEvaluator;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.StatementsChecker;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.StatementsExecutor;

public class ThreadStatementsChecker extends StatementsChecker<ThreadState> {
	
	public ThreadStatementsChecker(final List<Statement> statements, final ThreadState state) {
		mStatements = statements;
		mState = new ThreadState(state);
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
				final ThreadStatementsExecutor stmtsExecutor = new ThreadStatementsExecutor(stmt, mState);
				moveToNewState(stmtsExecutor.execute());
				ThreadStatementsChecker nextStatementsChecker 
						= new ThreadStatementsChecker(mStatements.subList(i+1, mStatements.size()), mState);
				return nextStatementsChecker.checkStatementsEnable();
			}
		}
		return true;
	}
	
	protected boolean checkAssumeStatement(final AssumeStatement assumeStmt) {
		final ExprEvaluator<ThreadState> exprEvaluator = new ExprEvaluator<>(mState);
		return (boolean) exprEvaluator.evaluate(assumeStmt.getFormula());
	}
	
	private void moveToNewState(final ThreadState newState) {
		mState = new ThreadState(newState);
	}
}
