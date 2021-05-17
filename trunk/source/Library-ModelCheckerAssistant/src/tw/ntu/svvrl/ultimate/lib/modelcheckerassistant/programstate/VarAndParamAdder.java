package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.programstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;

import de.uni_freiburg.informatik.ultimate.boogie.ast.BoogieASTNode;
import de.uni_freiburg.informatik.ultimate.boogie.ast.FunctionDeclaration;
import de.uni_freiburg.informatik.ultimate.boogie.ast.VarList;
import de.uni_freiburg.informatik.ultimate.boogie.type.BoogieArrayType;
import de.uni_freiburg.informatik.ultimate.boogie.type.BoogiePrimitiveType;
import de.uni_freiburg.informatik.ultimate.core.model.models.IBoogieType;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.boogie.Boogie2SmtSymbolTable;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.ILocalProgramVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramNonOldVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramVar;

public class VarAndParamAdder {
	/*---------------RCFG fields---------------*/
	private final Boogie2SmtSymbolTable mBoogie2SmtSymbolTable;
	/*------------End of RCFG fields-----------*/
	
	public VarAndParamAdder() {
		mBoogie2SmtSymbolTable = null;
	}
	
	public VarAndParamAdder(Boogie2SmtSymbolTable boogie2SmtSymbolTable) {
		mBoogie2SmtSymbolTable = boogie2SmtSymbolTable;
	}
	
	public void addInParams2Valuation(final Map<String, Map<String, Object>> valuation
			, final FunctionDeclaration funcDecl) {
		/**
		 * process all in params
		 */
		final String funcName = funcDecl.getIdentifier();
		for(VarList inParam : funcDecl.getInParams()) {
			addVarList2Valuation(valuation, funcName, inParam);
		}
	}
	
	/**
	 * Add all global boogie variables' type and value to valuation.
	 * @param valuation
	 * 		the value map should be added.
	 */
	public void addGlobalVars2Valuation(final Map<String, Map<String, Object>> valuation) {
		
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
	public void addLocalVars2Valuation(final Map<String, Map<String, Object>> valuation, final String procName) {
		
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
	private void addVar2Valuation(final Map<String, Map<String, Object>> valuation, final IProgramVar var) {
		String procName = var.getProcedure();
		BoogieASTNode boogieASTNode = mBoogie2SmtSymbolTable.getAstNode(var);
		if(boogieASTNode instanceof VarList) {
			addVarList2Valuation(valuation, procName, ((VarList) boogieASTNode));
		}
	}
	
	private void addVarList2Valuation(final Map<String, Map<String, Object>> valuation
			, final String procOrFuncName, final VarList varList) {
		IBoogieType boogieType = varList.getType().getBoogieType();
		
		Object value = processBoogieType(boogieType);
		
		for(String varName : varList.getIdentifiers()) {
			setValue(valuation, procOrFuncName, varName, value);
		}
	}
	
	/**
	 * Update the variable value in the valuation table.
	 * @param valuation
	 * 			the table needs to be updated.
	 * @param procOrFuncName
	 * @param varName
	 * @param value
	 */
	private void setValue(final Map<String, Map<String, Object>> valuation
			, final String procOrFuncName, final String varName
			, final Object value) {
		Map<String, Object> id2v = new HashMap<>();
		id2v.put(varName, value);

		if(valuation.containsKey(procOrFuncName)) {
			valuation.get(procOrFuncName).putAll(id2v);
		} else {
			valuation.put(procOrFuncName, id2v);
		}
	}
	
	
	/**
	 * Process a boogie type and return the default value of this type.
	 * To handle the array type, recursion is needed.
	 * @param bt
	 * 		An IBoogieType which can be {@link BoogiePrimitiveType} and {@link BoogieArrayType}.
	 * @return
	 * 		A default value for the given boogie type.
	 * 		Ex: bool 	-> 	null
	 * 			int 	-> 	null
	 * 			int[]	->	[null]
	 * 			int[][]	->	[[null]]
	 */
	private Object processBoogieType(final IBoogieType bt) {
		if (bt instanceof BoogiePrimitiveType) {
			switch(((BoogiePrimitiveType) bt).getTypeCode()) {
				case BoogiePrimitiveType.BOOL:
				case BoogiePrimitiveType.INT:
					return null;
				case BoogiePrimitiveType.REAL:
					throw new NotImplementedException("Boogie variable with type"
							+ ((BoogiePrimitiveType) bt).toString() + " is not yet implemented.");
				case BoogiePrimitiveType.ERROR:
				default:
					throw new UnsupportedOperationException("Boogie variable with"
							+ " error or unknown type.");
			}
			
		} else if(bt instanceof BoogieArrayType) {
			/**
			 * Check page 5 of https://www.microsoft.com/en-us/research/wp-content/uploads/2016/12/krml178.pdf
			 * The "maps" type is not supported.
			 */
			for(int i = 0; i < ((BoogieArrayType) bt).getIndexCount(); i++) {
				if(!(((BoogieArrayType) bt).getIndexType(i) instanceof BoogiePrimitiveType)) {
					throw new UnsupportedOperationException("Index type "
							+ ((BoogieArrayType) bt).getIndexType(i).getClass().getSimpleName()
							+ "is not supported.");
				} else {
					if(((BoogiePrimitiveType)((BoogieArrayType) bt).getIndexType(i)).getTypeCode()
							!= BoogiePrimitiveType.INT) {
						throw new UnsupportedOperationException("Index type "
								+ ((BoogiePrimitiveType)((BoogieArrayType) bt).getIndexType(i)).toString()
								+ "is not supported.");
					}
				}
			}
			Object v = processBoogieType(((BoogieArrayType) bt).getValueType());
			ArrayList<Object> array = new ArrayList<>();
			array.add(v);
			return array;
		} else {
			throw new UnsupportedOperationException("Unsupported"
					+ "BoogieType:" + bt.toString());
		}
	}

}
