package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant;

import de.uni_freiburg.informatik.ultimate.automata.nestedword.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgContainer;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.explorer.StateExplorer;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.explorer.NeverClaimAutExplorer;

public class ModelCheckerAssistant {
	
	private final ILogger mLogger;
	private final IUltimateServiceProvider mServices;
	private final BoogieIcfgContainer mRcfgRoot;
	private final INestedWordAutomaton<CodeBlock, String> mNWA;
	
	public ModelCheckerAssistant(final INestedWordAutomaton<CodeBlock, String> nwa, final BoogieIcfgContainer rcfg,
			final ILogger logger, final IUltimateServiceProvider services) {
		
		// services and logger
		mServices = services;
		mLogger = logger;
		mRcfgRoot = rcfg;
		mNWA = nwa;
	}
}
