package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.programstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;

import de.uni_freiburg.informatik.ultimate.boogie.ast.BoogieASTNode;
import de.uni_freiburg.informatik.ultimate.boogie.ast.VarList;
import de.uni_freiburg.informatik.ultimate.boogie.type.BoogiePrimitiveType;
import de.uni_freiburg.informatik.ultimate.core.model.models.IBoogieType;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.boogie.Boogie2SmtSymbolTable;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.boogie.BoogieNonOldVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.boogie.BoogieOldVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.boogie.GlobalBoogieVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.boogie.LocalBoogieVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.CfgSmtToolkit;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.ILocalProgramVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramNonOldVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramVar;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;

public class ProgramStateFactory {
	/*---------------RCFG fields---------------*/
	private final Boogie2SmtSymbolTable mBoogie2SmtSymbolTable;
	/*------------End of RCFG fields-----------*/
	
	
	public ProgramStateFactory(Boogie2SmtSymbolTable boogie2SmtSymbolTable) {
		mBoogie2SmtSymbolTable = boogie2SmtSymbolTable;
	}
	
	/**
	 * Extend an initial {@link BoogieIcfgLocation} with
	 * a value table so that it becomes an automaton initial state.
	 * Only global variables in the value table are handled.
	 * Local variables will be handled when a call statement occurs. 
	 * @param loc
	 * 		a {@link BoogieIcfgLocation}  which is an initial node.
	 * @return
	 * 		the result initial state.
	 */
	public ProgramState createInitialState(BoogieIcfgLocation loc) {
		Map<String, Map<String, Object>> valuation = new HashMap<>();
		addGlobalVars2Valuation(valuation);
		return new ProgramState(valuation, loc);
	}
	
//	public ProgramState createNextProgramState(ProgramState lastProgramState, transition) {
//	}
	
	/**
	 * Add all global boogie variables' type and value to valuation.
	 * @param valuation
	 * 		the value map should be added.
	 */
	private void addGlobalVars2Valuation(Map<String, Map<String, Object>> valuation) {
		
		/**
		 * process all global variables
		 */
		for(IProgramNonOldVar globalVar : mBoogie2SmtSymbolTable.getGlobals()) {
			addVar2Valuation(valuation, globalVar);
		}
	}
	
	/**
	 * Add local boogie variables' type and value to valuation.
	 * in a specific procedure.
	 * @param valuation
	 * 		the value map should be added.
	 * @param procName
	 * 		A specific procedure name.
	 */
	private void addLocalVars2Valuation(Map<String, Map<String, Object>> valuation, String procName) {
		
		/**
		 * process all local variables
		 */
		for(ILocalProgramVar localVar : mBoogie2SmtSymbolTable.getLocals(procName)) {
			addVar2Valuation(valuation, localVar);
		}
	}
	
	/**
	 *  Initialize a specific boogie variable's type and value and add it to valuation.
	 *  For Boolean variables, set their values to false.
	 *  For Int variables, set their values to 0.
	 *  Real variables are not yet implemented.
	 *  
	 * @param valuation
	 * 		the value map should be added.
	 * @param var
	 * 		the target variable.
	 */
	private void addVar2Valuation(Map<String, Map<String, Object>> valuation, IProgramVar var) {
		String procName = var.getProcedure();
		String identifier = null;
		if(var instanceof GlobalBoogieVar) {
			if(var instanceof BoogieNonOldVar) {
				identifier = ((BoogieNonOldVar)var).getIdentifier();
			} else if(var instanceof BoogieOldVar) {
				identifier = ((BoogieOldVar)var).getIdentifierOfNonOldVar();
			}
		} else if(var instanceof LocalBoogieVar) {
			identifier = ((LocalBoogieVar)var).getIdentifier();
		}
		BoogieASTNode boogieASTNode = mBoogie2SmtSymbolTable.getAstNode(var);
		if(boogieASTNode instanceof VarList) {
			IBoogieType boogieType = ((VarList)boogieASTNode).getType().getBoogieType();
			if (boogieType instanceof BoogiePrimitiveType) {
				Map<String, Object> id2v = new HashMap<>();
				switch(((BoogiePrimitiveType) boogieType).getTypeCode()) {
					case BoogiePrimitiveType.BOOL:
						boolean boolValue = false;
						id2v.put(identifier, boolValue);
						break;
					case BoogiePrimitiveType.INT:
						int intValue = 0;
						id2v.put(identifier, intValue);
						break;
					case BoogiePrimitiveType.REAL:
						throw new NotImplementedException("Boogie variable with type"
								+ " \"real\" is not yet implemented.");
					case BoogiePrimitiveType.ERROR:
					default:
						throw new UnsupportedOperationException("Boogie variable with"
								+ " error or unknown type.");
				}
				if(valuation.containsKey(procName)) {
					valuation.get(procName).putAll(id2v);
				} else {
					valuation.put(procName, id2v);
				}
			}
			else {
				throw new UnsupportedOperationException("Unsupported"
						+ "BoogieType:" + boogieType.toString());
			}
		}
	}
	
}
