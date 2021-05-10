package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.programstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;

import de.uni_freiburg.informatik.ultimate.boogie.ast.ArrayLHS;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssertStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssignmentStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssumeStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.LeftHandSide;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.StructLHS;
import de.uni_freiburg.informatik.ultimate.boogie.ast.VariableLHS;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.boogie.BoogieNonOldVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.boogie.BoogieOldVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.boogie.LocalBoogieVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.AbstractIcfgTransition;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramVar;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Call;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ForkThreadCurrent;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ForkThreadOther;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.GotoEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.JoinThreadCurrent;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.JoinThreadOther;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ParallelComposition;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Return;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RootEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.SequentialComposition;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.StatementSequence;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Summary;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.ExprEvaluator;

/**
 * This class represents a boogie program state.
 * It differs from {@link BoogieIcfgLocation} in the existence of actual valuation
 * during the program execution.
 * @author Hong-Yang Lin
 *
 */

public class ProgramState {
	/**
	 * To record the valuation of boogie variables.
	 * procedure name × identifier × value
	 */
	private final Map<String, Map<String, Object>> mValuation = new HashMap<>();
	/**
	 * To specify which IcfgLocation this state is generated from.
	 */
	private final BoogieIcfgLocation mRelaedIcfgLoc;
	private static final ExprEvaluator mExprEvaluator = new ExprEvaluator();
	
	public ProgramState(Map<String, Map<String, Object>> valuation,
						BoogieIcfgLocation boogieIcfgLocation) {
		mValuation.putAll(valuation);
		mRelaedIcfgLoc = boogieIcfgLocation;
	}
	
	public BoogieIcfgLocation getRelatedIcfgLoc() {
		return mRelaedIcfgLoc;
	}
	
	public Map<String, Map<String, Object>> getValuationMap() {
		return mValuation;
	}
	
	/**
	 * Look up the valuation table.
	 * @param procName
	 * 		name of procedure
	 * @param identifier
	 * 		name of identifier
	 * @return
	 * 		the value of given identifier
	 */
	private Object lookUpValue(String procName, String identifier) {
		Object v = mValuation.get(procName).get(identifier);
		if(v instanceof Integer) {
			return (Integer)v;
		} else if(v instanceof Boolean) {
			return (Boolean)v;
		} else {
			throw new UnsupportedOperationException("Unkown variable type");
		}
	}
	
	/**
	 * Update the valuation table.
	 * @param procName
	 * 		name of procedure
	 * @param identifier
	 * 		name of identifier
	 * @param v
	 * 		the new value of given identifier
	 */
	private void updateValue(String procName, String identifier, Object v) {
		Object result = mValuation.get(procName).replace(identifier, v);
		if(result == null) {
			throw new UnsupportedOperationException("No variable found in valuation table. "
					+ "Variable update failed.");
		}
	}
	
	public List<IcfgEdge> getEnableTrans() {
		List<IcfgEdge> edges = mRelaedIcfgLoc.getOutgoingEdges();
		List<IcfgEdge> enableTrans = new ArrayList<>();
		for(IcfgEdge edge : edges) {
			if (edge instanceof CodeBlock) {
				if(edge instanceof StatementSequence) {
					checkStatementsEnable(((StatementSequence) edge).getStatements());
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
					// same as above.
					throw new NotImplementedException(ParallelComposition.class.getSimpleName()
							+ "is not yet implemented.");
				} else {
					// other edge types are OK.
					enableTrans.add(edge);
				}
			} else if (edge instanceof RootEdge) {
				throw new UnsupportedOperationException("Suppose the type " + edge.getClass().getSimpleName()
						+ " should not appear in the function getEnableTrans()");
			} else {
				throw new UnsupportedOperationException("Error: " + edge.getClass().getSimpleName()
						+ " is not supported.");
			}
		}
		
		return enableTrans;
	}
	
	private boolean checkStatementsEnable(List<Statement> stmts) {
		for(Statement stmt : stmts) {
			if(stmt instanceof AssumeStatement) {
				// if the formula assumed is not hold, then not enable. 
				if(!checkAssumeStatement((AssumeStatement)stmt)) {
					return false;
				}
			} else if(stmt instanceof AssertStatement) {
				if(!checkAssertStatement((AssertStatement)stmt)) {
					throw new UnsupportedOperationException("Assertion is violated.");
				}
			} else if(stmt instanceof AssignmentStatement) {
				processAssignmentStatement((AssignmentStatement)stmt);
			}
		}
		return true;
	}
	
	private boolean checkAssumeStatement(AssumeStatement assumeStmt) {
		return true;
	}
	
	private boolean checkAssertStatement(AssertStatement assertStmt) {
		return true;
	}
	
	private void processAssignmentStatement(AssignmentStatement assignmentStmt) {
		LeftHandSide[] lhs = assignmentStmt.getLhs();
		Expression[] rhs = assignmentStmt.getRhs();
		assert(lhs.length == rhs.length);
		for(int i = 0; i < lhs.length; i++) {
			String procName = ((VariableLHS)lhs[i]).getDeclarationInformation().getProcedure();
			String identifier = ((VariableLHS)lhs[i]).getIdentifier();
			if(lhs[i] instanceof VariableLHS) {
				Object value = mExprEvaluator.evaluate(rhs[i]);
				updateValue(procName, identifier, value);
			} else if(lhs[i] instanceof ArrayLHS) {
				
			} else if(lhs[i] instanceof StructLHS) {
				throw new UnsupportedOperationException(StructLHS.class.getSimpleName() 
						+ "is not yet supported.");
			}
		}
	}
	
	/**
	 * Check whether two automaton states are equivalent.
	 * This method is needed in the nested DFS procedure. 
	 * @param anotherProgramState
	 * 		the state which is going to be compared to.
	 * @return
	 * 		true if two states are equivalent, false if not.
	 */
	public boolean equals(ProgramState anotherProgramState) {
		if(!mRelaedIcfgLoc.equals(anotherProgramState.getRelatedIcfgLoc())) {
			return false;
		}
		return mValuation.equals(anotherProgramState.getValuationMap()) ? true : false;
	}
}
