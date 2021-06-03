package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.threadtransitiontoolkit;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.Valuation;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProcInfo;
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
		final ThreadExprEvaluator exprEvaluator = new ThreadExprEvaluator(mCurrentState);
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
						+ " is not yet supported.");
			} else if(lhs[i] instanceof StructLHS) {
				throw new UnsupportedOperationException(StructLHS.class.getSimpleName() 
						+ " is not yet supported.");
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
		
		recordOlds();
		/**
		 * {@link CallStatement#isForall} is not yet implemented.
		 */
		final String procName = stmt.getMethodName();
		final Expression[] args = stmt.getArguments();
		
		doCallRoutines(procName, args);
	}

	private void executeForkStatement(ForkStatement stmt) {
		assert mCurrentState instanceof ThreadState;
		
		recordOlds();
		final String procName = stmt.getProcedureName();
		final Expression[] args = stmt.getArguments();
		
		doCallRoutines(procName, args);
		
		final ThreadExprEvaluator exprEvaluator = new ThreadExprEvaluator(mCurrentState);
		long newThreadID = (long) exprEvaluator.evaluate(stmt.getThreadID()[0]);
		mCurrentState.assignNewThreadID(newThreadID);
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
			final IBoogieType bt = lhs[i].getType();
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
						+ "BoogieType:" + bt.toString() + " in havoc statement");
			}
		}
	}

	private void executeIfStatement(IfStatement stmt) {
		assert mCurrentState instanceof ThreadState;
		final ThreadExprEvaluator exprEvaluator = new ThreadExprEvaluator(mCurrentState);
		if((boolean) exprEvaluator.evaluate(stmt.getCondition())) {
			final ThreadStatementsExecutor newStatementsExecutor
					 = new ThreadStatementsExecutor(Arrays.asList(stmt.getThenPart()), mCurrentState, mExecType);
			final ThreadState newState = newStatementsExecutor.execute();
			setCurrentState(newState);
		} else {
			final ThreadStatementsExecutor newStatementsExecutor
			 		= new ThreadStatementsExecutor(Arrays.asList(stmt.getElsePart()), mCurrentState, mExecType);
			final ThreadState newState = newStatementsExecutor.execute();
			setCurrentState(newState);
		}
	}

	private void executeJoinStatement(JoinStatement stmt) {
		/**
		 * Join statement is implemented in the {@link JoinHandler}
		 * because we need other thread state's information.
		 */
		throw new UnsupportedOperationException(stmt.getClass().getSimpleName()
				+ " is not implemented in the statement executor.");
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
		final ThreadExprEvaluator exprEvaluator = new ThreadExprEvaluator(mCurrentState);
		while((boolean) exprEvaluator.evaluate(stmt.getCondition())) {
			final ThreadStatementsExecutor newStatementsExecutor
	 			= new ThreadStatementsExecutor(Arrays.asList(stmt.getBody()), mCurrentState, mExecType);
			final ThreadState newState = newStatementsExecutor.execute();
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
		mCurrentState = newState;
	}
	
	public ThreadState getCurrentState() {
		return mCurrentState;
	}
	
	private void recordOlds() {
		/**
		 * Record current value of global variables to old variables.
		 */
		final Map<String, Object> globalId2v = mCurrentState.getValuation().getProcOrFuncId2V(null);
		final Set<String> globalNonOldVarNames = new HashSet<>();
		for(final String globalVarName : globalId2v.keySet()) {
			if(!mCurrentState.getValuation().isOld(globalVarName)) {
				globalNonOldVarNames.add(globalVarName);
			}
		}
		for(final String globalNonOldVarName : globalNonOldVarNames) {
			final String oldVarName = "old(" + globalNonOldVarName + ")";
			updateThreadState(null, oldVarName, globalId2v.get(globalNonOldVarName));
		}
	}
	
	private void doCallRoutines(String procName, Expression[] args) {
		ThreadExprEvaluator exprEvaluator = new ThreadExprEvaluator(mCurrentState);
		
		List<String> argsName = mCurrentState.getProc2InParams().get(procName);
		assert(args.length == argsName.size());
		
		/**
		 * Record current valuation.
		 */
		mCurrentState.getCurrentProc().setValuationRecord(mCurrentState.getValuationFullCopy());
		
		/**
		 * Clear the rest value (occur in recursive call) of the target procedure.
		 * Before getting into the procedure, set all locals to null.
		 */
		final Map<String, Object> id2v = mCurrentState.getValuation().getProcOrFuncId2V(procName);
		for(final String varName : id2v.keySet()) {
			updateThreadState(procName, varName, null);
		}
		
		/**
		 * assign values to in params
		 */
		for(int i = 0; i < args.length; i++) {
			updateThreadState(procName, argsName.get(i), exprEvaluator.evaluate(args[i]));
		}
		mCurrentState.pushProc(new ProcInfo(procName));
		
	}

}
