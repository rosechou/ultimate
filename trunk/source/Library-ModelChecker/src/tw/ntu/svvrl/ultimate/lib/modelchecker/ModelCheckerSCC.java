package tw.ntu.svvrl.ultimate.lib.modelchecker;

import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.*;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.neverstate.NeverState;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.transitions.IncomingInternalTransition;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.NestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.NestedWordAutomatonCache;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramStateTransition;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadStateTransition;
import de.uni_freiburg.informatik.ultimate.util.datastructures.relation.Pair;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.neverstate.NeverStateFactory;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.ModelCheckerAssistant;

import java.util.Map.Entry;
import java.util.*;

public class ModelCheckerSCC {
	private final ModelCheckerAssistant assistant;
	private final ILogger mLogger;
	private boolean match = false;
	private int count = 0;
	private List<ProgramState> levelNodes = new ArrayList<ProgramState>();
	Stack<Pair<ProgramState, NeverState>> root = new Stack<>();
	Map<ProgramState, Integer> dfsnum = new HashMap<ProgramState, Integer>();
	Map<ProgramState, Boolean> current = new HashMap<ProgramState, Boolean 	>();
	// Stack<ProgramState> Roots = new Stack<>();
	Stack<Pair<ProgramState, NeverState>> Roots = new Stack<>();
	
	public ModelCheckerSCC(final ILogger logger, final ModelCheckerAssistant mca) 
	{
		mLogger = logger;
		assistant = mca;
		levelNodes.addAll(assistant.getProgramInitialStates());
		Set<NeverState> initStates = new HashSet<>();
		initStates = assistant.getNeverInitialStates();
		
		for (int i = 0;i < levelNodes.size();i++) {
			for(int j = 0;j < initStates.size();j++)
			{
				if(match) {return;}
				NeverState init = ((NeverState) initStates.toArray()[j]);
				startSCC(levelNodes.get(i), init);
			}
		}
		if(!match) 
		{
			mLogger.info("All specifications hold");
		}
	}
	public void remove(Pair<ProgramState, NeverState> RemoveElement)
	{
		if(!current.get(RemoveElement.getFirst()))
		{
			return;
		}
		current.replace(RemoveElement.getFirst(), false);
		
		List<ProgramStateTransition> programEdges = assistant.getProgramEnabledTrans(RemoveElement.getFirst());
		
		List<ProgramState> nProgramNodes = new ArrayList<ProgramState>();
		for(int j = 0;j < programEdges.size();j++)
		{
			nProgramNodes.add(assistant.doProgramTransition(RemoveElement.getFirst(), programEdges.get(j)));
		}
		levelNodes = nProgramNodes;
		
		anotherTrans:
			for (int i = 0;i < levelNodes.size();i++) {
				if(match) {return;}
				ProgramState nextNode = levelNodes.get(i);
				
				List<OutgoingInternalTransition<CodeBlock, NeverState>> neverEdges = assistant.getNeverEnabledTrans(RemoveElement.getSecond(), nextNode);
				
				
				for (int j = 0;j < neverEdges.size();j++) {
					if(match) {return;}
					OutgoingInternalTransition<CodeBlock, NeverState> neverEdge = neverEdges.get(j);
					NeverState nextState = assistant.doNeverTransition(RemoveElement.getSecond(), neverEdge, nextNode);
					
					Pair p = new Pair(nextNode, nextState);
					remove(p);
				}
			}

	}
	
	public void startSCC(ProgramState node, NeverState init)
	{
		count = count + 1;
		dfsnum.put(node, count);
		Pair p = new Pair(node, init);
		Roots.push(p);
		current.put(node, true);
		
		List<ProgramStateTransition> programEdges = assistant.getProgramEnabledTrans(node);
		
		List<ProgramState> nProgramNodes = new ArrayList<ProgramState>();
		for(int j = 0;j < programEdges.size();j++)
		{
			nProgramNodes.add(assistant.doProgramTransition(node, programEdges.get(j)));
		}
		levelNodes = nProgramNodes;
		
		anotherTrans:
		for (int i = 0;i < levelNodes.size();i++) {
			if(match) {return;}
			ProgramState nextNode = levelNodes.get(i);
			
			List<OutgoingInternalTransition<CodeBlock, NeverState>> neverEdges = assistant.getNeverEnabledTrans(init, nextNode);
			
			if(!assistant.globalVarsInitialized(nextNode))
			{
				startSCC(nextNode, init);
				break anotherTrans;
			}
			
			for (int j = 0;j < neverEdges.size();j++) {
				if(match) {return;}
				OutgoingInternalTransition<CodeBlock, NeverState> neverEdge = neverEdges.get(j);
				NeverState nextState = assistant.doNeverTransition(init, neverEdge, nextNode);
				
				if(!dfsnum.containsKey(nextNode))
				{
					startSCC(nextNode, nextState);
				}
				else if(current.get(nextNode))
				{
					Pair<ProgramState, NeverState> element;
					do
					{
						element = Roots.pop();
						if(element.getSecond().isFinal())
						{
							match = true;
							mLogger.info("Violation of LTL property");
							return;
						}
					}while(dfsnum.get(element)<= dfsnum.get(nextNode));
					Roots.push(element);
				}
			}
		}
		if(Roots.peek().equals(p))
		{
			Roots.pop();
			remove(p);
		}
	}

}
