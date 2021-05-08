package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.programstate;

import java.util.HashMap;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.boogie.ast.BoogieASTNode;
import de.uni_freiburg.informatik.ultimate.boogie.ast.VarList;
import de.uni_freiburg.informatik.ultimate.boogie.type.BoogiePrimitiveType;
import de.uni_freiburg.informatik.ultimate.core.model.models.IBoogieType;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.boogie.Boogie2SmtSymbolTable;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.CfgSmtToolkit;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.ILocalProgramVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramNonOldVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramVar;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;

public class ProgramStateFactory {
	/*---------------RCFG fields---------------*/
	private final CfgSmtToolkit mCfgSmtTookit;
	private final Boogie2SmtSymbolTable mBoogie2SmtSymbolTable;
	/*------------End of RCFG fields-----------*/
	
	
	public ProgramStateFactory(Boogie2SmtSymbolTable boogie2SmtSymbolTable
						, CfgSmtToolkit cfgSmtToolkit) {
		mBoogie2SmtSymbolTable = boogie2SmtSymbolTable;
		mCfgSmtTookit = cfgSmtToolkit;
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
		Map<IProgramVar, Object> valuation = new HashMap<>();
		initializeGlobalVarsValuation(valuation);
		return new ProgramState(valuation, loc);
	}
	
//	public ProgramState createNextProgramState(ProgramState lastProgramState, transition) {
//	}
	
	
	/**
	 * Initialize all global boogie variables' type and value.
	 * @param valuation
	 * 		the value table should be initialized.
	 */
	private void initializeGlobalVarsValuation(Map<IProgramVar, Object> valuation) {
		
		/**
		 * process all global variables
		 */
		for(IProgramNonOldVar globalVar : mBoogie2SmtSymbolTable.getGlobals()) {
			initializeVarValuation(valuation, globalVar);
		}
	}
	
	/**
	 * Initialize local boogie variables' type and value
	 * in a specific procedure.
	 * @param valuation
	 * 		the value table should be initialized.
	 * @param procName
	 * 		A specific procedure name.
	 */
	private void initializeLocalVarsValuation(Map<IProgramVar, Object> valuation, String procName) {
		
		/**
		 * process all local variables
		 */
		for(ILocalProgramVar localVar : mBoogie2SmtSymbolTable.getLocals(procName)) {
			initializeVarValuation(valuation, localVar);
		}
	}
	
	/**
	 *  Initialize a specific boogie variable's type and value.
	 *  For Boolean variables, set their values to false.
	 *  For Int variables, set their values to 0.
	 *  Real variables are not yet implemented.
	 *  
	 * @param valueTable
	 * 		the value table should be initialized.
	 * @param var
	 * 		the target variable.
	 */
	private void initializeVarValuation(Map<IProgramVar, Object> valuation, IProgramVar var) {
		BoogieASTNode boogieASTNode = mBoogie2SmtSymbolTable.getAstNode(var);
		if(boogieASTNode instanceof VarList) {
			IBoogieType boogieType = ((VarList)boogieASTNode).getType().getBoogieType();
			if (boogieType instanceof BoogiePrimitiveType) {
				switch(((BoogiePrimitiveType) boogieType).getTypeCode()) {
					case BoogiePrimitiveType.BOOL:
						boolean boolValue = false;
						valuation.put(var, boolValue);
						break;
					case BoogiePrimitiveType.INT:
						int intValue = 0;
						valuation.put(var, intValue);
						break;
					case BoogiePrimitiveType.REAL:
						throw new UnsupportedOperationException("Boogie variable with type"
								+ " \"real\" is not yet supported.");
					case BoogiePrimitiveType.ERROR:
					default:
						throw new UnsupportedOperationException("Boogie variable with"
								+ " error or unknown type.");
				}
			}
			else {
				throw new UnsupportedOperationException("Unsupported"
						+ "BoogieType:" + boogieType.toString());
			}
		}
	}
	
}
