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
	 * have the same funcInitValuationInfo, proc2InParams and proc2OutParams.
	 */
	private final FuncInitValuationInfo mFuncInitValuationInfo;
	private final Map<String, List<String>> mProc2InParams;
	private final Map<String, List<String>> mProc2OutParams;
	
	private final VarAndParamAdder mVarAdder;
	
	public ProgramStateFactory(final Boogie2SmtSymbolTable boogie2SmtSymbolTable
			, final CfgSmtToolkit cfgSmtToolkit) {
		mFuncInitValuationInfo = new FuncInitValuationInfo(
			boogie2SmtSymbolTable.getBoogieDeclarations().getFunctionDeclarations());
		mProc2InParams = createProc2Prams(boogie2SmtSymbolTable, "in");
		mProc2OutParams = createProc2Prams(boogie2SmtSymbolTable, "out");
		mVarAdder = new VarAndParamAdder(boogie2SmtSymbolTable, cfgSmtToolkit.getProcedures());
	}
	
	

	private Map<String, List<String>> createProc2Prams(Boogie2SmtSymbolTable boogie2SmtSymbolTable
			, String inOrOut) {
		final Map<String, List<String>> result = new HashMap<>();
		Map<String, List<ILocalProgramVar>> p2p = new HashMap<>();
		if(inOrOut.equals("in")) {
			p2p = boogie2SmtSymbolTable.getProc2InParams();
		} else if(inOrOut.equals("out")) {
			p2p = boogie2SmtSymbolTable.getProc2OutParams();
		} else {
			throw new UnsupportedOperationException("Invalid argument: "
					+ inOrOut);
		}
				
		for(final String procName : p2p.keySet()) {
			List<String> paramNames = new ArrayList<>();
			for(final ILocalProgramVar param : p2p.get(procName)) {
				paramNames.add(param.getIdentifier());
			}
			result.put(procName, paramNames);
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
		return new ProgramState(valuation, loc, mFuncInitValuationInfo
				, mProc2InParams, mProc2OutParams);
	}
	
//	public ProgramState createNextProgramState(ProgramState lastProgramState, transition) {
//	}
	
	
	
}
