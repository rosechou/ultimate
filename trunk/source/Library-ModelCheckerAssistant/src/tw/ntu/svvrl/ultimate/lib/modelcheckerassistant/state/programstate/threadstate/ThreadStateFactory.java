package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.boogie.Boogie2SmtSymbolTable;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.CfgSmtToolkit;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.ILocalProgramVar;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.FuncInitValuationInfo;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.Valuation;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.VarAndParamAdder;

public class ThreadStateFactory {
	
	/**
	 * States which are generated from the same rcfg
	 * have the same funcInitValuationInfo, proc2InParams and proc2OutParams.
	 */
	private final FuncInitValuationInfo mFuncInitValuationInfo;
	private final Map<String, List<String>> mProc2InParams;
	private final Map<String, List<String>> mProc2OutParams;
	
	private final VarAndParamAdder mVarAdder;
	
	public ThreadStateFactory(final Boogie2SmtSymbolTable boogie2SmtSymbolTable
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


	public ThreadState createInitialState(final BoogieIcfgLocation loc
										, final Valuation globalValuation) {
		Valuation newValuation = globalValuation.clone();
		mVarAdder.addLocalVars2Valuation(newValuation);
		mVarAdder.addProcInParams2Valuation(newValuation);
		mVarAdder.addProcOutParams2Valuation(newValuation);
		
		/**
		 * Main Thread ID: 0
		 */
		return new ThreadState(newValuation, loc, mFuncInitValuationInfo
				, mProc2InParams, mProc2OutParams, 0);
	}
	
}
