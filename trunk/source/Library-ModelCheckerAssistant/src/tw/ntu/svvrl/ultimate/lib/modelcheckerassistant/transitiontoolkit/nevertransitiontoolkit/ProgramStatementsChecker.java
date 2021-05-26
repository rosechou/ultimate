package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.nevertransitiontoolkit;

import java.util.List;

import de.uni_freiburg.informatik.ultimate.boogie.ast.AssumeStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.StatementsChecker;

public class ProgramStatementsChecker extends StatementsChecker<ProgramState> {

	public ProgramStatementsChecker(final List<Statement> statements, final ProgramState state) {
		super(statements);
		mState = state;
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
			} else {
				throw new UnsupportedOperationException("Suppose the Statement Type: "
						+ stmt.getClass().getSimpleName() + " should not be in Never Claim"
								+ " Automata.");
			}
		}
		return true;
	}
	
	protected boolean checkAssumeStatement(final AssumeStatement assumeStmt) {
		final ProgramExprEvaluator exprEvaluator = new ProgramExprEvaluator(mState);
		return (boolean) exprEvaluator.evaluate(assumeStmt.getFormula());
	}


}
