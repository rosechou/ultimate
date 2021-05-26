package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.ValuationState;

import java.util.List;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssumeStatement;

public abstract class StatementsExecutor<S extends ValuationState<S>> {
	protected List<Statement> mStatements;
	protected Statement mStatement;
	protected S mCurrentState;
	
	/**
	 * For many statements.
	 */
	protected StatementsExecutor(final List<Statement> statements) {
		mStatements = statements;
		mStatement = null;
	}
	
	/**
	 * For only one statement.
	 */
	protected StatementsExecutor(final Statement statement) {
		mStatements = null;
		mStatement = statement;
	}
	
	/**
	 * For no statement. ({@link Return} code block)
	 * @param threadState
	 */
	public StatementsExecutor() {
		mStatements = null;
		mStatement = null;
	}
	
	/**
	 * Execute one or many statements
	 * @return
	 * 		the new state reached after executing
	 */
	public S execute() {
		if(mStatements != null) {
			for(final Statement stmt : mStatements) {
				executeOne(stmt);
			}
		} else {
			executeOne(mStatement);
		}
		return mCurrentState;
	}
	
	protected abstract void executeOne(final Statement stmt);
	

	protected void executeAssumeStatement(AssumeStatement stmt) {
		ExprEvaluator<S> exprEvaluator = new ExprEvaluator<>(mCurrentState);
		assert((boolean) exprEvaluator.evaluate(stmt.getFormula()) == true);
	}

}
