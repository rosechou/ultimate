package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.programstate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.boogie.Boogie2SmtSymbolTable;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.CfgSmtToolkit;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;

public class ProgramStateFactory {
	
	/**
	 * Every state has the same funcInitValuationInfo.
	 */
	private final FuncInitValuationInfo mFuncInitValuationInfo;
	private final VarAndParamAdder mVarAdder;
	
	public ProgramStateFactory(final Boogie2SmtSymbolTable boogie2SmtSymbolTable
			, final CfgSmtToolkit cfgSmtToolkit) {
		mFuncInitValuationInfo = new FuncInitValuationInfo(
			boogie2SmtSymbolTable.getBoogieDeclarations().getFunctionDeclarations());
		mVarAdder = new VarAndParamAdder(boogie2SmtSymbolTable, cfgSmtToolkit.getProcedures());
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
	public ProgramState createInitialState(final BoogieIcfgLocation loc) {
		Map<String, Map<String, Object>> valuation = new HashMap<>();
		mVarAdder.addGlobalVars2Valuation(valuation);
		mVarAdder.addOldGlobalVars2Valuation(valuation);
		mVarAdder.addLocalVars2Valuation(valuation);
		mVarAdder.addProcInParams2Valuation(valuation);
		mVarAdder.addProcOutParams2Valuation(valuation);
		return new ProgramState(valuation, loc, mFuncInitValuationInfo);
	}
	
//	public ProgramState createNextProgramState(ProgramState lastProgramState, transition) {
//	}
	
	
	
}
