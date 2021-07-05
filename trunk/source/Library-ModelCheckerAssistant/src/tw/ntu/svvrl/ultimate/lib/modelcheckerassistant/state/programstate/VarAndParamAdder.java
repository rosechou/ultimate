package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramOldVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramVar;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.Valuation;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.util.SFO;

public class VarAndParamAdder {
	/*---------------RCFG fields---------------*/
	private final Boogie2SmtSymbolTable mBoogie2SmtSymbolTable;
	private final Set<String> mProcNames;
	/*------------End of RCFG fields-----------*/
	
	/**
	 * For Boogie functions.
	 */
	public VarAndParamAdder() {
		mBoogie2SmtSymbolTable = null;
		mProcNames = null;
	}
	
	/**
	 * For globals
	 * @param boogie2SmtSymbolTable
	 */
	public VarAndParamAdder(Boogie2SmtSymbolTable boogie2SmtSymbolTable) {
		mBoogie2SmtSymbolTable = boogie2SmtSymbolTable;
		mProcNames = null;
	}
	
	/**
	 * For locals, procedures
	 * @param boogie2SmtSymbolTable
	 * @param procNames 
	 * 		all procedure names
	 */
	public VarAndParamAdder(Boogie2SmtSymbolTable boogie2SmtSymbolTable, Set<String> procNames) {
		mBoogie2SmtSymbolTable = boogie2SmtSymbolTable;
		mProcNames = procNames;
	}
	
	/**
	 * Because function params do not stored in symbol table,
	 * we manually process it from the declaration level.
	 */
	public void addFunInParams2Valuation(final Valuation valuation
			, final FunctionDeclaration funcDecl) {
		/**
		 * process all in params
		 */
		final String funcName = funcDecl.getIdentifier();
		for(final VarList inParam : funcDecl.getInParams()) {
			addVarList2Valuation(valuation, funcName, inParam, false);
		}
	}
	
	/**
	 * Add all global boogie variables' type and value to valuation.
	 * @param valuation
	 * 		the value map should be added.
	 */
	public void addGlobalVars2Valuation(final Valuation valuation) {
		
		/**
		 * process all global variables
		 */
		for(final IProgramNonOldVar globalVar : mBoogie2SmtSymbolTable.getGlobals()) {
			addVar2Valuation(valuation, globalVar);
		}
	}
	
	/**
	 * Add all old global boogie variables' type and value to valuation.
	 * @param valuation
	 * 		the value map should be added.
	 */
	public void addOldGlobalVars2Valuation(final Valuation valuation) {
		
		/**
		 * process all global variables
		 */
		for(final IProgramVar oldGlobalVar : mBoogie2SmtSymbolTable.getOldVars().values()) {
			addVar2Valuation(valuation, oldGlobalVar);
		}
	}
	
	/**
	 * Add all procedures' local boogie variables' type and value to valuation.
	 * @param valuation
	 * 		the value map should be added.
	 */
	public void addLocalVars2Valuation(final Valuation valuation) {
		for(final String procName : mProcNames) {
			for(final ILocalProgramVar localVar : mBoogie2SmtSymbolTable.getLocals(procName)) {
				addVar2Valuation(valuation, localVar);
			}
		}
	}
	
	/**
	 * Add all boogie procedures' in parameters' type and value to valuation.
	 * in a specific procedure.
	 * @param valuation
	 * 		the value map should be added.
	 */
	public void addProcInParams2Valuation(final Valuation valuation) {
		for(final String procName : mProcNames) {
			for(final ILocalProgramVar inParam : mBoogie2SmtSymbolTable.getProc2InParams().get(procName)) {
				addVar2Valuation(valuation, inParam);
			}
		}
	}
	
	/**
	 * Add all boogie procedures' out parameters' type and value to valuation.
	 * in a specific procedure.
	 * @param valuation
	 * 		the value map should be added.
	 */
	public void addProcOutParams2Valuation(final Valuation valuation) {
		for(final String procName : mProcNames) {
			for(final ILocalProgramVar outParam : mBoogie2SmtSymbolTable.getProc2OutParams().get(procName)) {
				addVar2Valuation(valuation, outParam);
			}
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
	private void addVar2Valuation(final Valuation valuation, final IProgramVar var) {
		final String procName = var.getProcedure();
		final BoogieASTNode boogieASTNode = mBoogie2SmtSymbolTable.getAstNode(var);
		boolean isOld = false;
		if(var instanceof IProgramOldVar) {
			isOld = true;
		}
		if(boogieASTNode instanceof VarList) {
			addVarList2Valuation(valuation, procName, ((VarList) boogieASTNode), isOld);
		}
	}
	
	/**
	 * Caution: the where clause in Boogie's syntax is not yet implemented.
	 */
	private void addVarList2Valuation(final Valuation valuation
			, final String procOrFuncName, final VarList varList, final boolean isOld) {
		if(varList.getWhereClause() != null) {
			throw new NotImplementedException("Where clause is not yet supported.");
		}
		
		final IBoogieType boogieType = varList.getType().getBoogieType();
		
		final Object value = processBoogieType(boogieType);
		
		for(String varName : varList.getIdentifiers()) {
			if(isOld) {
				varName = "old(" + varName + ")";
			}
			valuation.setValue(procOrFuncName, varName, value);
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
	 * 			int[]	->	{null=null}
	 * 			int[][]	->	{null={null=null}}
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
//			final Object v = processBoogieType(((BoogieArrayType) bt).getValueType());
//			final Map<Object, Object> map = new HashMap<>();
//			map.put(null, v);
//			return map;
			final Map<Object, Object> map = new HashMap<>();
			return map;
		} else {
			throw new UnsupportedOperationException("Unsupported"
					+ "BoogieType:" + bt.toString());
		}
	}

	/**
	 * Because the global variable #pthreadsForks is not initialize
	 * in the input Boogie program. We initialize it to 1 (main thread).
	 * Once a thread is ready to be forked, the variable will increase by 1.
	 * #pthreadsForks records the number of threads created and it never decreases.
	 * @param valuation
	 */
	public void addPthreadsForks(Valuation valuation) {
		long initialCount = 1;
		valuation.setValue(null, SFO.ULTIMATE_FORK_COUNT, initialCount);
	}

}
