package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.threadtransitiontoolkit;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.NotImplementedException;

import de.uni_freiburg.informatik.ultimate.boogie.ast.ArrayLHS;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssertStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssignmentStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssumeStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AtomicStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.BreakStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.CallStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.ForkStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.GotoStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.HavocStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.IfStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.JoinStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Label;
import de.uni_freiburg.informatik.ultimate.boogie.ast.LeftHandSide;
import de.uni_freiburg.informatik.ultimate.boogie.ast.ReturnStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.StructLHS;
import de.uni_freiburg.informatik.ultimate.boogie.ast.VariableLHS;
import de.uni_freiburg.informatik.ultimate.boogie.ast.WhileStatement;
import de.uni_freiburg.informatik.ultimate.boogie.type.BoogiePrimitiveType;
import de.uni_freiburg.informatik.ultimate.core.model.models.IBoogieType;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.Valuation;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.StatementsExecutor;

public class ThreadStatementsExecutor extends StatementsExecutor<ThreadState>  {
	public static enum execType{
		check, realExec
	}
	
	private final execType mExecType;
	
	/**
	 * For many statements.
	 * @param statements
	 * 		List of statements
	 * @param threadState
	 */
	public ThreadStatementsExecutor(final List<Statement> statements, final ThreadState state
									, final execType t) {
		super(statements);
		mCurrentState = new ThreadState(state);
		mExecType = t;
	}
	
	/**
	 * For only one statement.
	 * @param statement
	 * @param threadState
	 */
	public ThreadStatementsExecutor(final Statement statement, final ThreadState state
									, final execType t) {
		super(statement);
		mCurrentState = new ThreadState(state);
		mExecType = t;
	}
	
	/**
	 * For no statement. ({@link Return} code block)
	 * @param threadState
	 */
	public ThreadStatementsExecutor(final ThreadState state, final execType t) {
		super();
		mCurrentState = new ThreadState(state);
		mExecType = t;
	}
	
	public void executeOne(final Statement stmt) {
		if(stmt instanceof AssertStatement) {
			executeAssertStatement((AssertStatement) stmt);
		} else if(stmt instanceof AssignmentStatement) {
			executeAssignmentStatement((AssignmentStatement) stmt);
		} else if(stmt instanceof AssumeStatement) {
			executeAssumeStatement((AssumeStatement) stmt);
		} else if(stmt instanceof AtomicStatement) {
			executeAtomicStatement((AtomicStatement) stmt);
		} else if(stmt instanceof BreakStatement) {
			executeBreakStatement((BreakStatement) stmt);
		} else if(stmt instanceof CallStatement) {
			executeCallStatement((CallStatement) stmt);
		} else if(stmt instanceof ForkStatement) {
			executeForkStatement((ForkStatement) stmt);
		} else if(stmt instanceof GotoStatement) {
			executeGotoStatement((GotoStatement) stmt);
		} else if(stmt instanceof HavocStatement) {
			executeHavocStatement((HavocStatement) stmt);
		} else if(stmt instanceof IfStatement) {
			executeIfStatement((IfStatement) stmt);
		} else if(stmt instanceof JoinStatement) {
			executeJoinStatement((JoinStatement) stmt);
		} else if(stmt instanceof Label) {
			executeLabel((Label) stmt);
		} else if(stmt instanceof ReturnStatement) {
			executeReturnStatement((ReturnStatement) stmt);
		} else if(stmt instanceof WhileStatement) {
			executeWhileStatement((WhileStatement) stmt);
		} else {
			throw new UnsupportedOperationException("Error: " + stmt.getClass().getSimpleName()
					+ " is not supported.");
		}
	}


	private void executeAssertStatement(AssertStatement stmt) {
		ThreadExprEvaluator exprEvaluator = new ThreadExprEvaluator(mCurrentState);
		if(!(boolean) exprEvaluator.evaluate(stmt.getFormula())) {
			throw new UnsupportedOperationException("Assertion is violated during"
					+ " the statement execution.");
		}
	}

	/**
	 * AssignmentStatement will change the valuation and move to a new state.
	 * @param stmt
	 * 		an assignment statement.
	 * @return
	 * 		new thread state.
	 */
	private void executeAssignmentStatement(final AssignmentStatement stmt) {
		final ThreadExprEvaluator exprEvaluator = new ThreadExprEvaluator(mCurrentState);
		
		final LeftHandSide[] lhs = stmt.getLhs();
		final Expression[] rhs = stmt.getRhs();
		assert(lhs.length == rhs.length);
		
		/**
		 * Handle multi-assignment
		 * For example
		 * int a, b, c := 1, 2, 3;
		 */
		for(int i = 0; i < lhs.length; i++) {
			if(lhs[i] instanceof VariableLHS) {
				final String procName = ((VariableLHS) lhs[i]).getDeclarationInformation().getProcedure();
				final String varName = ((VariableLHS) lhs[i]).getIdentifier();
				final Object value = exprEvaluator.evaluate(rhs[i]);
				
				updateThreadState(procName, varName, value);
			} else if(lhs[i] instanceof ArrayLHS) {
				/**
				 * I don't know how to produce these case.
				 * It seems no chance to occur. (?)
				 */
				throw new UnsupportedOperationException(StructLHS.class.getSimpleName() 
						+ "is not yet supported.");
			} else if(lhs[i] instanceof StructLHS) {
				throw new UnsupportedOperationException(StructLHS.class.getSimpleName() 
						+ "is not yet supported.");
			}
		}
	}

	private void executeAtomicStatement(AtomicStatement stmt) {
		/**
		 * Suppose this statement would not occur here because
		 * this statement is handled during the
		 * preprocessing of Boogie and the building of RCFG.
		 * The body of atomic statement will form a {@link StatementSequence}
		 * and thus this issue is solved.
		 * So we just follow the transition of the resulting RCFG.
		 */
		throw new UnsupportedOperationException(stmt.getClass().getSimpleName()
				+ "should not appear.");
	}

	private void executeBreakStatement(BreakStatement stmt) {
		/**
		 * Suppose this statement would not occur here because
		 * this statement is translated to {@link GotoEdge} during the
		 * preprocessing of Boogie and the building of RCFG.
		 */
		throw new UnsupportedOperationException(stmt.getClass().getSimpleName()
				+ "should not appear.");
	}

	private void executeCallStatement(CallStatement stmt) {
		assert mCurrentState instanceof ThreadState;
		/**
		 * {@link CallStatement#isForall} is not yet implemented.
		 */
		String procName = stmt.getMethodName();
		Expression[] args = stmt.getArguments();
		ThreadExprEvaluator exprEvaluator = new ThreadExprEvaluator(mCurrentState);
		
		List<String> argsName = mCurrentState.getProc2InParams().get(procName);
		assert(args.length == argsName.size());
		/**
		 * assign values to in params
		 */
		for(int i = 0; i < args.length; i++) {
			updateThreadState(procName, argsName.get(i), exprEvaluator.evaluate(args[i]));
		}
		mCurrentState.pushProc(procName);
	}

	private void executeForkStatement(ForkStatement stmt) {
		assert mCurrentState instanceof ThreadState;
	}

	private void executeGotoStatement(GotoStatement stmt) {
		/**
		 * Suppose this statement would not occur here because
		 * this statement is translated to {@link GotoEdge} during the
		 * preprocessing of Boogie and the building of RCFG.
		 */
		throw new UnsupportedOperationException(stmt.getClass().getSimpleName()
				+ "should not appear.");
	}

	private void executeHavocStatement(HavocStatement stmt) {
		assert mCurrentState instanceof ThreadState;
		VariableLHS[] lhs = stmt.getIdentifiers();
		for(int i = 0; i < lhs.length; i++) {
			final String procName = lhs[i].getDeclarationInformation().getProcedure();
			final String varName = lhs[i].getIdentifier();
			IBoogieType bt = lhs[i].getType();
			if (bt instanceof BoogiePrimitiveType) {
				final Random r = new Random();
				Object value;
				switch(((BoogiePrimitiveType) bt).getTypeCode()) {
					case BoogiePrimitiveType.BOOL:
						value = r.nextBoolean();
						updateThreadState(procName, varName, value);
						break;
					case BoogiePrimitiveType.INT:
						value = r.nextInt();
						updateThreadState(procName, varName, value);
						break;
					case BoogiePrimitiveType.REAL:
						throw new NotImplementedException("Boogie variable with type"
								+ ((BoogiePrimitiveType) bt).toString() + " is not yet implemented.");
					case BoogiePrimitiveType.ERROR:
					default:
						throw new UnsupportedOperationException("Boogie variable with"
								+ " error or unknown type.");
				}
				
			} else {
				throw new UnsupportedOperationException("Unsupported"
						+ "BoogieType:" + bt.toString() + "in havoc statement");
			}
		}
	}

	private void executeIfStatement(IfStatement stmt) {
		assert mCurrentState instanceof ThreadState;
		ThreadExprEvaluator exprEvaluator = new ThreadExprEvaluator(mCurrentState);
		if((boolean) exprEvaluator.evaluate(stmt.getCondition())) {
			ThreadStatementsExecutor newStatementsExecutor
					 = new ThreadStatementsExecutor(Arrays.asList(stmt.getThenPart()), mCurrentState, mExecType);
			ThreadState newState = newStatementsExecutor.execute();
			setCurrentState(newState);
		} else {
			ThreadStatementsExecutor newStatementsExecutor
			 		= new ThreadStatementsExecutor(Arrays.asList(stmt.getElsePart()), mCurrentState, mExecType);
			ThreadState newState = newStatementsExecutor.execute();
			setCurrentState(newState);
		}
	}

	private void executeJoinStatement(JoinStatement stmt) {
		assert mCurrentState instanceof ThreadState;
	}

	private void executeLabel(Label stmt) {
		/**
		 * Do nothing.
		 */
	}

	private void executeReturnStatement(ReturnStatement stmt) {
		assert mCurrentState instanceof ThreadState;
		/**
		 * Cannot produce this case
		 */
		throw new NotImplementedException(stmt.getClass().getSimpleName()
				+ "is not yet implemented.");
	}

	private void executeWhileStatement(WhileStatement stmt) {
		assert mCurrentState instanceof ThreadState;
		ThreadExprEvaluator exprEvaluator = new ThreadExprEvaluator(mCurrentState);
		while((boolean) exprEvaluator.evaluate(stmt.getCondition())) {
			ThreadStatementsExecutor newStatementsExecutor
	 			= new ThreadStatementsExecutor(Arrays.asList(stmt.getBody()), mCurrentState, mExecType);
			ThreadState newState = newStatementsExecutor.execute();
			setCurrentState(newState);
		}
	}
	
	public void updateThreadState(final String procName, final String varName, final Object value) {
		Valuation newValuation;
		if(mExecType == execType.check) {
			newValuation = mCurrentState.getValuationFullCopy();
		} else if(mExecType == execType.realExec) {
			newValuation = mCurrentState.getValuationLocalCopy();
		} else {
			throw new UnsupportedOperationException("Unknown execType: " + 
					mExecType.getClass().getSimpleName());
		}
		
		assert(newValuation.containsProcOrFunc(procName));
		newValuation.setValue(procName, varName, value);

		mCurrentState = new ThreadState(newValuation, mCurrentState);
	}
	
	
	private void setCurrentState(ThreadState newState) {
		mCurrentState = new ThreadState(newState);
	}
	
	public ThreadState getCurrentState() {
		return mCurrentState;
	}

}
