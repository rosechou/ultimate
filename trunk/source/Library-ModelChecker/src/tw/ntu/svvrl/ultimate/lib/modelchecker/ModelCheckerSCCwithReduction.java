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
	private boolean end = false;
	private int count = 1;
	private List<ProgramState> levelNodes = new ArrayList<ProgramState>();

	Stack<Pair<Pair<ProgramState, NeverState>, Integer>> dfsnum = new Stack<>();
	Stack<Pair<Pair<ProgramState, NeverState>, Boolean>> current = new Stack<>();
	Stack<Pair<ProgramState, NeverState>> Roots = new Stack<>();
	Stack<Pair<ProgramState, NeverState>> StateSpace = new Stack<>();
	List<Pair<ProgramState, NeverState>> ErrorPath = new ArrayList<Pair<ProgramState, NeverState>>();
	
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
				Pair p = new Pair(levelNodes.get(i), (NeverState) initStates.toArray()[j]);
				StateSpace.push(p);
				Roots.push(p);
				dfsnum.push(new Pair(p, count));
				dfs(count);
			}
		}
		if(!end)
		{
			mLogger.info("*Violation of LTL property");
			return;
		}else if(!match && end) 
		{
			mLogger.info("All specifications hold");
			return;
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
	
	public boolean compareErrorPath(List<Pair<ProgramState, NeverState>> path, ProgramState node, NeverState state)
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
	
	/* system move */
	public void dfs(int count)
	{
		count = count + 1;
		ProgramState node = Roots.peek().getFirst();
		NeverState state = Roots.peek().getSecond();
		
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
			
			if(programEdges.contains(null))
			{
				continue;
			}
			
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
					Pair p = new Pair(nextNode, state);
					dfsnum.push(new Pair(p, count));
					Roots.push(p);
					StateSpace.push(p);
					dfs(count);
					continue;
				}
				
				Pair p = new Pair(nextNode, state);

				//if(!compare(StateSpace, nextNode, state) || (node.equals(nextNode)&& !node.visited))
				if(!compare(StateSpace, nextNode, state))
				{
					dfsnum.push(new Pair(p, count));
					Roots.push(p);
					StateSpace.push(p);
					Dfs(count);
 				}else if(compare(Roots, node, state))
				{
					NotInStack = false;
				}
				AtLeaseOneSuccesor = true;
			}
			if(AtLeaseOneSuccesor && NotInStack)
			{
				break;
			}
		}
	}
	/* specification move */
	public void Dfs(int count)
	{
		NeverState state = Roots.peek().getSecond();
		ProgramState node = Roots.peek().getFirst();
		
		List<OutgoingInternalTransition<CodeBlock, NeverState>> neverEdges = assistant.getNeverEnabledTrans(state, node);
		
		for (int j = 0;j < neverEdges.size();j++) {
			if(match) {return;}
			OutgoingInternalTransition<CodeBlock, NeverState> neverEdge = neverEdges.get(j);
			NeverState nextState = assistant.doNeverTransition(state, neverEdge, node);
			
			dfsnum.pop();
			Roots.pop();
			StateSpace.pop();

			Pair p = new Pair(node, nextState);
			// dfsnum.push(new Pair(p, count));
			
			if(node.getThreadStates().toString().contains("ULTIMATE.startEXIT"))
			{
				end = true;
			}
			
			if(compare(StateSpace, node, nextState))
			{			
				Pair<ProgramState, NeverState> element;
				do
				{
					element = Roots.pop();
					if(element.getSecond().isFinal())
					{
						match = true;
						mLogger.info("Violation of LTL property");
						Roots.push(element);
						Roots.push(p);
						for(int a = 0; a < Roots.size();a++)
						{
							mLogger.info(Roots.get(a).getFirst().getThreadStates().toString() + Roots.get(a).getSecond().getName());
						}
						return;
					}
				}while(compareDfsnum(dfsnum, element.getFirst(),element.getSecond())
					> compareDfsnum(dfsnum, node, nextState));
				//element.getFirst().visited = true;
				Roots.push(element);
				dfs(count);
				continue;
			}
			dfsnum.push(new Pair(p, count));
			StateSpace.push(p);			
			Roots.push(p);	
			dfs(count);
		}

		if(!Roots.isEmpty())
		{
			Pair<ProgramState, NeverState> RemoveElement = Roots.pop();
			// StateSpace.pop();
			mLogger.info(RemoveElement.getFirst().getThreadNumber() + RemoveElement.getFirst().getThreadStates().toString() + RemoveElement.getSecond().getName());
		}
		
	}
}
