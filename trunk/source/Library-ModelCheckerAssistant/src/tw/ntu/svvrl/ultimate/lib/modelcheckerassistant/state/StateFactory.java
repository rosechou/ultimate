package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state;

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

public class StateFactory {
	/*---------------RCFG fields---------------*/
	private final CfgSmtToolkit mCfgSmtTookit;
	private final Boogie2SmtSymbolTable mBoogie2SmtSymbolTable;
	/*------------End of RCFG fields-----------*/
	
	private final Map<IProgramVar, Object> mVar2Value = new HashMap<>();
	
	
	public StateFactory(Boogie2SmtSymbolTable boogie2SmtSymbolTable
						, CfgSmtToolkit cfgSmtToolkit) {
		mBoogie2SmtSymbolTable = boogie2SmtSymbolTable;
		mCfgSmtTookit = cfgSmtToolkit;
	}
	
	/**
	 * Initialize all boogie variables' type and value.
	 */
	private void initializeVar2Value() {
		
		/**
		 * process all global variables
		 */
		for(IProgramNonOldVar globalVar : mBoogie2SmtSymbolTable.getGlobals()) {
			initializeVar2Value(globalVar);
		}
		
		
		/**
		 * process all local variables
		 * (Maybe do this when getting into a procedure)
		 */
		for(String procName : mCfgSmtTookit.getProcedures()) {
			for(ILocalProgramVar localVar : mBoogie2SmtSymbolTable.getLocals(procName)) {
				initializeVar2Value(localVar);
			}
		}
	}
	
	/**
	 *  Initialize a specific boogie variable's type and value.
	 *  For Boolean variables, set their values to false.
	 *  For Int variables, set their values to 0.
	 *  Real variables are not yet implemented.
	 *  
	 * @param boogie2SmtSymbolTable
	 * 		boogie2SmtSymbolTable retrieved from rcfg
	 * @param var
	 * 		the target variable.
	 */
	private void initializeVar2Value(IProgramVar var) {
		BoogieASTNode boogieASTNode = mBoogie2SmtSymbolTable.getAstNode(var);
		if(boogieASTNode instanceof VarList) {
			IBoogieType boogieType = ((VarList)boogieASTNode).getType().getBoogieType();
			if (boogieType instanceof BoogiePrimitiveType) {
				switch(((BoogiePrimitiveType) boogieType).getTypeCode()) {
					case BoogiePrimitiveType.BOOL:
						boolean boolValue = false;
						mVar2Value.put(var, boolValue);
						break;
					case BoogiePrimitiveType.INT:
						int intValue = 0;
						mVar2Value.put(var, intValue);
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
	
	public State creatState(Map<IProgramVar, Object> valueTable, BoogieIcfgLocation loc) {
		return new State(valueTable, loc);
	}
}
