package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.StructLHS;
import de.uni_freiburg.informatik.ultimate.boogie.ast.VariableLHS;
import de.uni_freiburg.informatik.ultimate.boogie.ast.WhileStatement;
import de.uni_freiburg.informatik.ultimate.boogie.type.BoogiePrimitiveType;
import de.uni_freiburg.informatik.ultimate.core.model.models.IBoogieType;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.programstate.ProgramState;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
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

public class StatementsExecutor {
	private ProgramState mCurrentProgramState;

	public StatementsExecutor(final ProgramState programState) {
		mCurrentProgramState = new ProgramState(programState);
	}
	
	public void execute(final List<Statement> stmts) {
		for(final Statement stmt : stmts) {
			execute(stmt);
		}
	}
	
	public void execute(final Statement stmt) {
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
	

	private void updateValuation(final String procName, final String varName, final Object value) {
		final Map<String, Map<String, Object>> newValuation = new HashMap<>();
		newValuation.putAll(mCurrentProgramState.getValuationMap());
		assert(newValuation.containsKey(procName));
		
		final Map<String, Object> id2v = newValuation.get(procName);
		if(id2v.containsKey(varName)) {
			id2v.replace(varName, value);
		} else {
			id2v.put(varName, value);
		}
		mCurrentProgramState = new ProgramState(newValuation, mCurrentProgramState.getFuncInitValuationInfo());
	}
	
	
	public ProgramState getCurrentState() {
		return mCurrentProgramState;
	}

	private void executeAssertStatement(AssertStatement stmt) {
		// TODO Auto-generated method stub
	}

	/**
	 * AssignmentStatement will change the valuation and move to a new state.
	 * @param stmt
	 * 		an assignment statement.
	 * @return
	 * 		new program state.
	 */
	private void executeAssignmentStatement(final AssignmentStatement stmt) {
		final ExprEvaluator exprEvaluator = new ExprEvaluator(mCurrentProgramState);
		
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
				final String procName = ((VariableLHS)lhs[i]).getDeclarationInformation().getProcedure();
				final String varName = ((VariableLHS)lhs[i]).getIdentifier();
				final Object value = exprEvaluator.evaluate(rhs[i]);
				
				updateValuation(procName, varName, value);
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


	private void executeAssumeStatement(AssumeStatement stmt) {
		ExprEvaluator exprEvaluator = new ExprEvaluator(mCurrentProgramState);
		assert((boolean) exprEvaluator.evaluate(stmt.getFormula()) == true);
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
	}

	private void executeForkStatement(ForkStatement stmt) {
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
						updateValuation(procName, varName, value);
					case BoogiePrimitiveType.INT:
						value = r.nextInt();
						updateValuation(procName, varName, value);
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
		ExprEvaluator exprEvaluator = new ExprEvaluator(mCurrentProgramState);
		if((boolean) exprEvaluator.evaluate(stmt.getCondition())) {
			execute(Arrays.asList(stmt.getThenPart()));
		} else {
			execute(Arrays.asList(stmt.getElsePart()));
		}
	}

	private void executeJoinStatement(JoinStatement stmt) {
	}

	private void executeLabel(Label stmt) {
		/**
		 * Do nothing.
		 */
	}

	private void executeReturnStatement(ReturnStatement stmt) {
	}

	private void executeWhileStatement(WhileStatement stmt) {
		ExprEvaluator exprEvaluator = new ExprEvaluator(mCurrentProgramState);
		while((boolean) exprEvaluator.evaluate(stmt.getCondition())) {
			execute(Arrays.asList(stmt.getBody()));
		}
	}
}
