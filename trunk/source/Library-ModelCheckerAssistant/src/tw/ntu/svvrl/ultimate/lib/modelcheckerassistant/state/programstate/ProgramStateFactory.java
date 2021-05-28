package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate;


import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.boogie.Boogie2SmtSymbolTable;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.CfgSmtToolkit;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadStateFactory;

public class ProgramStateFactory {
	private final VarAndParamAdder mVarAdder;
	private final ThreadStateFactory mThreadStateFactory;
	
	public ProgramStateFactory(final Boogie2SmtSymbolTable boogie2SmtSymbolTable
			, final CfgSmtToolkit cfgSmtToolkit) {
		mVarAdder = new VarAndParamAdder(boogie2SmtSymbolTable);
		mThreadStateFactory = new ThreadStateFactory(boogie2SmtSymbolTable, cfgSmtToolkit);
	}


	public ProgramState createInitialState(final BoogieIcfgLocation loc) {
		Valuation globalValuation = new Valuation();
		mVarAdder.addGlobalVars2Valuation(globalValuation);
		mVarAdder.addOldGlobalVars2Valuation(globalValuation);
		mVarAdder.addPthreadsForks(globalValuation);
		
		final ThreadState initialThreadState = mThreadStateFactory.createInitialState(loc, globalValuation);
		
		return new ProgramState(initialThreadState, globalValuation);
	}
	
}
