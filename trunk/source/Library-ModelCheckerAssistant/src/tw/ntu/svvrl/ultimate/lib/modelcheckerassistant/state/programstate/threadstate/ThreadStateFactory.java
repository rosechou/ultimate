package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.boogie.Boogie2SmtSymbolTable;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.CfgSmtToolkit;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.ILocalProgramVar;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.explorer.ProgramStateExplorer;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.Valuation;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.FuncInitValuationInfo;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.VarAndParamAdder;

public class ThreadStateFactory {
	private final ProgramStateExplorer mProgramStateExplorer;
	private final VarAndParamAdder mVarAdder;
	
	public ThreadStateFactory(final Boogie2SmtSymbolTable boogie2SmtSymbolTable
			, final CfgSmtToolkit cfgSmtToolkit, final ProgramStateExplorer pe) {
		
		mVarAdder = new VarAndParamAdder(boogie2SmtSymbolTable, cfgSmtToolkit.getProcedures());
		mProgramStateExplorer = pe;
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
		return new ThreadState(newValuation, loc, 0, mProgramStateExplorer);
	}
	
}
