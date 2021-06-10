package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.explorer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.automata.nestedword.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.neverstate.NeverState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.neverstate.NeverStateFactory;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit.nevertransitiontoolkit.NeverTransitionToolkit;

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
	
	public List<OutgoingInternalTransition<CodeBlock, NeverState>> getEnabledTrans(final NeverState n, final ProgramState correspondingProgramState) {
		List<OutgoingInternalTransition<CodeBlock, NeverState>> enabledTrans = new ArrayList<>();
		
		for(final OutgoingInternalTransition<CodeBlock, NeverState> edge : n.getTranss()) {
			final NeverTransitionToolkit transitionToolkit
			= new NeverTransitionToolkit(edge, n, correspondingProgramState);
			if (transitionToolkit.checkTransEnabled()) {
				enabledTrans.add(edge);
			}
		}
		return enabledTrans;
	}
	

	public NeverState doTransition(final NeverState n, final OutgoingInternalTransition<CodeBlock, NeverState> edge
			, final ProgramState correspondingProgramState) {
		final NeverTransitionToolkit transitionToolkit 
			= new NeverTransitionToolkit(edge, n, correspondingProgramState);
		return (NeverState) transitionToolkit.doTransition();
	}
}
