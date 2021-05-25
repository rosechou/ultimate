package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.explorer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.automata.nestedword.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.neverstate.NeverState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.neverstate.NeverStateFactory;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ThreadState;

public class NeverClaimAutExplorer {
	final INestedWordAutomaton<CodeBlock, String> mNwa;
	final Map<String, NeverState> mName2State;
	
	public NeverClaimAutExplorer(INestedWordAutomaton<CodeBlock, String> nwa) {
		mNwa = nwa;
		NeverStateFactory neverStatefactory = new NeverStateFactory(nwa);
		mName2State = neverStatefactory.createName2State();
	}
	
	public Set<NeverState> getInitialStates() {
		final Set<NeverState> result = new HashSet<>();
		final Set<String> initialNames = mNwa.getInitialStates();
		for(final String initialName : initialNames) {
			result.add(mName2State.get(initialName));
		}
		return result;
	}
	
	
}
