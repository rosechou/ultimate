package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.threadtransitiontoolkit;

import java.util.Arrays;
import java.util.List;

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

	/**
	 * Check Statement Sequence.
	 */
	public boolean checkStatementsAccessOnlyLocalVar() {
		boolean accessOnlyLocals = true;
		for(final Statement statement : mStatements) {
			if(statement instanceof AssertStatement) {
				/**
				 * Do nothing.
				 */
			} else if(statement instanceof AssignmentStatement) {
				accessOnlyLocals = checkAssignmentAccessOnlyLocalVar((AssignmentStatement) statement);
			} else if(statement instanceof AssumeStatement) {
				accessOnlyLocals = checkAssumeAccessOnlyLocalVar((AssumeStatement) statement);
			} else if(statement instanceof AtomicStatement) {
				List<Statement> statements = Arrays.asList(((AtomicStatement) statement).getBody());
				ThreadStatementsChecker nextStatementsChecker 
					= new ThreadStatementsChecker(statements, mState, mProgramStateExplorer);
				accessOnlyLocals = nextStatementsChecker.checkStatementsAccessOnlyLocalVar();
			} else if(statement instanceof BreakStatement) {
				/**
				 * Do nothing.
				 */
			} else if(statement instanceof CallStatement) {
				/**
				 * handled in the {@link CodeBlock} level.
				 */
				throw new UnsupportedOperationException("Suppose "
						+ statement.getClass().getSimpleName() + " cannot appear here.");
			} else if(statement instanceof ForkStatement) {
				/**
				 * handled in the {@link CodeBlock} level.
				 */
				throw new UnsupportedOperationException("Suppose "
						+ statement.getClass().getSimpleName() + " cannot appear here.");
			} else if(statement instanceof GotoStatement) {
				/**
				 * Do nothing.
				 */
			} else if(statement instanceof HavocStatement) {
				accessOnlyLocals = checkHavocAccessOnlyLocalVar((HavocStatement) statement);
			} else if(statement instanceof IfStatement) {
				accessOnlyLocals = checkIfAccessOnlyLocalVar((IfStatement) statement);
			} else if(statement instanceof JoinStatement) {
				/**
				 * handled in the {@link CodeBlock} level.
				 */
				throw new UnsupportedOperationException("Suppose "
						+ statement.getClass().getSimpleName() + " cannot appear here.");
			} else if(statement instanceof Label) {
				/**
				 * Do nothing.
				 */
			} else if(statement instanceof ReturnStatement) {
				/**
				 * handled in the {@link CodeBlock} level.
				 */
				throw new UnsupportedOperationException("Suppose "
						+ statement.getClass().getSimpleName() + " cannot appear here.");
			} else if(statement instanceof WhileStatement) {
				accessOnlyLocals = checkWhileAccessOnlyLocalVar((WhileStatement) statement);
			} else {
				throw new UnsupportedOperationException("Unknown statement type: "
						+ statement.getClass().getSimpleName());
			}
			if(!accessOnlyLocals) {
				return accessOnlyLocals;
			}
		}
		return accessOnlyLocals;
	}

	
	private boolean checkAssignmentAccessOnlyLocalVar(final AssignmentStatement stmt) {
		/**
		 * Check left hand sides
		 */
		final LeftHandSide[] lhss = stmt.getLhs();
		for(final LeftHandSide lhs : lhss) {
			if(lhs instanceof VariableLHS) {
				final String procName = ((VariableLHS) lhs).getDeclarationInformation().getProcedure();
				if(procName == null) {
					return false;
				}
			} else if(lhs instanceof ArrayLHS) {
				/**
				 * I don't know how to produce these case.
				 * It seems no chance to occur. (?)
				 */
				throw new UnsupportedOperationException(lhs.getClass().getSimpleName() 
						+ " is not yet supported.");
			} else if(lhs instanceof StructLHS) {
				throw new UnsupportedOperationException(lhs.getClass().getSimpleName() 
						+ " is not yet supported.");
			}
		}
		
		/**
		 * Check right hand sides
		 */
		final ThreadExprEvaluator exprEvaluator = new ThreadExprEvaluator(mState, mProgramStateExplorer);
		final Expression[] rhs = stmt.getRhs();
		
		return false;
	}

	private boolean checkAssumeAccessOnlyLocalVar(final AssumeStatement stmt) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean checkHavocAccessOnlyLocalVar(final HavocStatement stmt) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean checkIfAccessOnlyLocalVar(final IfStatement stmt) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean checkWhileAccessOnlyLocalVar(final WhileStatement stmt) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * For CodeBlocks.
	 */
	
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
