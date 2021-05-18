package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.boogie.ast.ArrayLHS;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssertStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssignmentStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssumeStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.HavocStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.LeftHandSide;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.StructLHS;
import de.uni_freiburg.informatik.ultimate.boogie.ast.VariableLHS;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.programstate.ProgramState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.StatementsExecutor;

/**
 * This class check whether the statements is able to execute (enable).
 */
public class StatementsChecker {
	/**
	 * the program state may change due to assignment and havoc statement.
	 */
	private ProgramState mProgramState;
	
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
	public boolean checkStatementsEnable(final List<Statement> stmts) {
		for(int i = 0; i < stmts.size(); i++) {
			final Statement stmt = stmts.get(i);
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
				final StatementsExecutor stmtsExecutor = new StatementsExecutor(mProgramState);
				stmtsExecutor.execute(stmt);
				moveToNewState(stmtsExecutor.getCurrentState());
				return checkStatementsEnable(stmts.subList(i+1, stmts.size()));
			}
		}
		return true;
	}
	
	private boolean checkAssumeStatement(final AssumeStatement assumeStmt) {
		final ExprEvaluator exprEvaluator = new ExprEvaluator(mProgramState);
		return (boolean) exprEvaluator.evaluate(assumeStmt.getFormula());
	}
	
	public StatementsChecker(final ProgramState programState) {
		mProgramState = new ProgramState(programState);
	}
	
	private void moveToNewState(final ProgramState newState) {
		mProgramState = new ProgramState(newState);
	}
}
