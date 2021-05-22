package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.neverstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.automata.nestedword.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;

public class NeverStateFactory {
	final INestedWordAutomaton<CodeBlock, String> mNwa;
	
	
	public NeverStateFactory(INestedWordAutomaton<CodeBlock, String> nwa) {
		mNwa = nwa;
	}
	
	public Map<String, NeverState> createName2State() {
		final Map<String, NeverState> name2State = new HashMap<>();
		for(final String stateName : mNwa.getStates()) {
			/**
			 * States in Never claim automata have only internal transition.
			 * No call and return transition under the context of Nested Automata.
			 */
			assert !mNwa.callPredecessors(stateName).iterator().hasNext();
			assert !mNwa.callSuccessors(stateName).iterator().hasNext();
			assert !mNwa.returnPredecessors(stateName).iterator().hasNext();
			assert !mNwa.returnSuccessors(stateName).iterator().hasNext();
			
			Iterator<OutgoingInternalTransition<CodeBlock, String>> iter
						= mNwa.internalSuccessors(stateName).iterator();
			List<OutgoingInternalTransition<CodeBlock, String>> transs = new ArrayList<>();
			while(iter.hasNext()) {
				final OutgoingInternalTransition<CodeBlock, String> trans = iter.next();
				transs.add(trans);
			}
			NeverState s = new NeverState(stateName, transs, mNwa.isInitial(stateName), mNwa.isFinal(stateName));
			name2State.put(stateName, s);
		}
		return name2State;
	}
}
