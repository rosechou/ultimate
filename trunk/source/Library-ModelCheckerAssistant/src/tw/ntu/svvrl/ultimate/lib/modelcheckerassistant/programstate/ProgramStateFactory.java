package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.programstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.boogie.Boogie2SmtSymbolTable;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.CfgSmtToolkit;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.ILocalProgramVar;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;

public class ProgramStateFactory {
	
	/**
	 * States which are generated from the same rcfg
	 * have the same funcInitValuationInfo and proc2InParams.
	 */
	private final FuncInitValuationInfo mFuncInitValuationInfo;
	private final Map<String, List<String>> mProc2InParams;
	
	private final VarAndParamAdder mVarAdder;
	
	public ProgramStateFactory(final Boogie2SmtSymbolTable boogie2SmtSymbolTable
			, final CfgSmtToolkit cfgSmtToolkit) {
		mFuncInitValuationInfo = new FuncInitValuationInfo(
			boogie2SmtSymbolTable.getBoogieDeclarations().getFunctionDeclarations());
		mProc2InParams = createProc2InPrams(boogie2SmtSymbolTable);
		mVarAdder = new VarAndParamAdder(boogie2SmtSymbolTable, cfgSmtToolkit.getProcedures());
	}
	
	

	private Map<String, List<String>> createProc2InPrams(Boogie2SmtSymbolTable boogie2SmtSymbolTable) {
		final Map<String, List<String>> result = new HashMap<>();
		final Map<String, List<ILocalProgramVar>> p2ip = boogie2SmtSymbolTable.getProc2InParams();
		for(final String procName : p2ip.keySet()) {
			List<String> inParamNames = new ArrayList<>();
			for(final ILocalProgramVar inParam : p2ip.get(procName)) {
				inParamNames.add(inParam.getIdentifier());
			}
			result.put(procName, inParamNames);
		}
		return result;
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
		Valuation valuation = new Valuation();
		mVarAdder.addGlobalVars2Valuation(valuation);
		mVarAdder.addOldGlobalVars2Valuation(valuation);
		mVarAdder.addLocalVars2Valuation(valuation);
		mVarAdder.addProcInParams2Valuation(valuation);
		mVarAdder.addProcOutParams2Valuation(valuation);
		return new ProgramState(valuation, loc, mFuncInitValuationInfo, mProc2InParams);
	}
	
//	public ProgramState createNextProgramState(ProgramState lastProgramState, transition) {
//	}
	
	
	
}
