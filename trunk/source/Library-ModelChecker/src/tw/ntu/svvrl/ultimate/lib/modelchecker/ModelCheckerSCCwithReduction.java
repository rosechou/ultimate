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

public class ModelCheckerSCCwithReduction {
	private final ModelCheckerAssistant assistant;
	private final ILogger mLogger;
	private boolean match = false;
	private int count = 0;
	private List<ProgramState> levelNodes = new ArrayList<ProgramState>();

	Stack<Pair<Pair<ProgramState, NeverState>, Integer>> dfsnum = new Stack<>();
	// Map<ProgramState, Integer> dfsnum = new HashMap<ProgramState, Integer>();
	Stack<Pair<Pair<ProgramState, NeverState>, Boolean>> current = new Stack<>();
	// Map<ProgramState, Boolean> current = new HashMap<ProgramState, Boolean>();
	// Stack<ProgramState> Roots = new Stack<>();
	Stack<Pair<ProgramState, NeverState>> Roots = new Stack<>();
	
	public ModelCheckerSCCwithReduction(final ILogger logger, final ModelCheckerAssistant mca) 
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
	
	public boolean compare(Stack<Pair<ProgramState, NeverState>> path, ProgramState node, NeverState state)
	{
		for(int i = 0; i < path.size();i++)
		{
			if(node.equals(path.get(i).getFirst()) && state.equals(path.get(i).getSecond()))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean compareCurrent(Stack<Pair<Pair<ProgramState, NeverState>, Boolean>> path, ProgramState node, NeverState state)
	{
		for(int i = 0; i < path.size();i++)
		{
			if(node.equals(path.get(i).getFirst().getFirst()) && state.equals(path.get(i).getFirst().getSecond()) && path.get(i).getSecond())
			{
				return true;
			}
		}
		return false;
	}
	
	public int compareDfsnum(Stack<Pair<Pair<ProgramState, NeverState>, Integer>> path, ProgramState node, NeverState state)
	{
		for(int i = 0; i < path.size();i++)
		{
			if(node.equals(path.get(i).getFirst().getFirst()) && state.equals(path.get(i).getFirst().getSecond()))
			{
				return path.get(i).getSecond();
			}
		}
		return 0;
	}
	
	public void remove(Pair<ProgramState, NeverState> RemoveElement)
	{
		// if(!current.get(RemoveElement.getFirst()))
		if(!compareCurrent(current, RemoveElement.getFirst(), RemoveElement.getSecond()))
		{
			return;
		}
		for(int i = 0;i < current.size();i++)
		{
			if(compareCurrent(current, RemoveElement.getFirst(), RemoveElement.getSecond()))
			{
				// current.replace(RemoveElement.getFirst(), false);
				current.get(i).setSecond(false);
				break;
			}
		}
		
		List<Long> OrderofProcesses = assistant.getProgramSafestOrder(RemoveElement.getFirst());
		List<ProgramStateTransition> programEdges = new ArrayList<ProgramStateTransition>();
		
		for(int k = 0;k < OrderofProcesses.size();k++)
		{
			programEdges = assistant.getProgramEnabledTransByThreadID(RemoveElement.getFirst(), OrderofProcesses.get(k));
			
			List<ProgramState> nProgramNodes = new ArrayList<ProgramState>();
			for(int j = 0;j < programEdges.size();j++)
			{
				nProgramNodes.add(assistant.doProgramTransition(RemoveElement.getFirst(), programEdges.get(j)));
			}
			levelNodes = nProgramNodes;
			
			for (int i = 0;i < levelNodes.size();i++) {
				if(match) {return;}
				ProgramState nextNode = levelNodes.get(i);
				
				if(!assistant.globalVarsInitialized(nextNode))
				{					
					Pair p = new Pair(nextNode, RemoveElement.getSecond());
					remove(p);
					continue;
				}
				
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
	}
	
	
	public void startSCC(ProgramState node, NeverState init)
	{
		count = count + 1;
		
		List<Long> OrderofProcesses = assistant.getProgramSafestOrder(node);
 		List<ProgramStateTransition> programEdges = new ArrayList<ProgramStateTransition>();
		
		for(int k = 0;k < OrderofProcesses.size();k++)
		{
			programEdges = assistant.getProgramEnabledTransByThreadID(node, OrderofProcesses.get(k));
			
			if(programEdges.isEmpty())
			{
				programEdges.add(assistant.checkNeedOfSelfLoop(node));
			}
			
			boolean NotInStack = true;
			boolean AtLeaseOneSuccesor = false;
			
			List<ProgramState> nProgramNodes = new ArrayList<ProgramState>();
			for(int j = 0;j < programEdges.size();j++)
			{
				nProgramNodes.add(assistant.doProgramTransition(node, programEdges.get(j)));
			}
			levelNodes = nProgramNodes;
			
			for (int i = 0;i < levelNodes.size();i++) {
				if(match) {return;}
				ProgramState nextNode = levelNodes.get(i);
				
				if(!assistant.globalVarsInitialized(nextNode))
				{
					Pair p = new Pair(node, init);					
					dfsnum.push(new Pair(p, count));
					Roots.push(p);
					current.push(new Pair(p, true));
					
					startSCC(nextNode, init);
					continue;
				}
				
				List<OutgoingInternalTransition<CodeBlock, NeverState>> neverEdges = assistant.getNeverEnabledTrans(init, nextNode);
				
				for (int j = 0;j < neverEdges.size();j++) {
					if(match) {return;}
					OutgoingInternalTransition<CodeBlock, NeverState> neverEdge = neverEdges.get(j);
					NeverState nextState = assistant.doNeverTransition(init, neverEdge, nextNode);
					
					// if(!dfsnum.containsKey(nextNode))
					if(!compare(Roots, nextNode, nextState))
					{
						Pair p = new Pair(node, init);					
						dfsnum.push(new Pair(p, count));
						Roots.push(p);
						// current.push(new Pair(p, true));
						
						startSCC(nextNode, nextState);
					}
					// else if(current.get(nextNode))
					// else if(compareCurrent(current, nextNode, nextState))
					else
					{
						Pair p = new Pair(nextNode, nextNode);					
						dfsnum.push(new Pair(p, count));
						
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
						}while(compareDfsnum(dfsnum, element.getFirst(),element.getSecond())
								> compareDfsnum(dfsnum, nextNode, nextState));
						Roots.push(element);
						NotInStack = false;
					}
				}
				AtLeaseOneSuccesor = true;
			}
			if(AtLeaseOneSuccesor && NotInStack)
			{
				break;
			}
		}

		if(!Roots.isEmpty())
		{
			Pair<ProgramState, NeverState> RemoveElement = Roots.pop();
			mLogger.info(RemoveElement.getFirst().getThreadNumber() + RemoveElement.getFirst().getThreadStates().toString() + RemoveElement.getSecond().getName());
			// remove(RemoveElement);
		}
	}

}
