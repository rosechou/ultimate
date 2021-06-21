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
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.optncsb.automata.IState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramStateTransition;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadStateTransition;
import de.uni_freiburg.informatik.ultimate.util.datastructures.relation.Pair;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.neverstate.NeverStateFactory;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.ModelCheckerAssistant;

import java.util.Map.Entry;
import java.util.*;

public class ModelCheckerDoubleDFSwithReduction{
	private final ModelCheckerAssistant assistant;
	private final ILogger mLogger;
	private boolean match = false;
	//	ProgramState is the node of the Control Flow Graph
	private List<ProgramState> levelNodes = new ArrayList<ProgramState>();
	// ProgramState loc = null;
	Pair<ProgramState, NeverState> seed = null;
	// NeverState Neverseed = null;
	// Stack<ProgramState> firstVisited = new Stack<>();
	// Stack<ProgramState> secondVisited = new Stack<>();
	
	protected Set<NeverState> initStates =new HashSet<>();
	// Stack<NeverState> Neverstack = new Stack<>();
	// Stack<ProgramState> Programstack = new Stack<>();
	
	Stack<Pair<ProgramState, NeverState>> CompoundStack = new Stack<>();
	Stack<Pair<Pair<ProgramState, NeverState>, Integer>> StateSpace = new Stack<>();
	

	public ModelCheckerDoubleDFSwithReduction(final ILogger logger, final ModelCheckerAssistant mca)
	{	
		mLogger = logger;
		assistant = mca;
		
		// set of initial cfg locations
		levelNodes.addAll(assistant.getProgramInitialStates());
		
		// set of initial states of automaton
		Set<NeverState> initStates = new HashSet<>();
		initStates = assistant.getNeverInitialStates();
		
		for (int i = 0;i < levelNodes.size();i++) {
			for(int j = 0;j < initStates.size();j++)
			{
				if(match) {return;}
				Pair p = new Pair(levelNodes.get(i), (NeverState) initStates.toArray()[j]);
				CompoundStack.push(p);
				StateSpace.push(new Pair(p, 1));
				dfs(1);
			}
		}
		
		if(!match) 
		{
			mLogger.info("All specifications hold");
		}
		
	}
	
	public boolean compare(Stack<Pair<Pair<ProgramState, NeverState>, Integer>> path, ProgramState node, NeverState state, int ab)
	{
		for(int i = 0; i < path.size();i++)
		{
			if(node.equals(path.get(i).getFirst().getFirst()) && state.equals(path.get(i).getFirst().getSecond()) && path.get(i).getSecond() == ab)
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean compare2(Stack<Pair<ProgramState, NeverState>> path, ProgramState node, NeverState state)
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
	public void dfs(int a)
	{
		ProgramState node = CompoundStack.peek().getFirst();
		NeverState state = CompoundStack.peek().getSecond();
		
		List<Long> OrderofProcesses = assistant.getProgramSafestOrder(node);
		List<ProgramStateTransition> programEdges = new ArrayList<ProgramStateTransition>();
		
		
		for(int k = 0;k < OrderofProcesses.size();k++)
		{
			// programEdges.addAll(assistant.getProgramEnabledTransByThreadID(node, k));
			programEdges = assistant.getProgramEnabledTransByThreadID(node, OrderofProcesses.get(k));
			
			if(programEdges.isEmpty())
			{
				programEdges.add(assistant.checkNeedOfSelfLoop(node));
			}
			
			boolean NotInStack = true;
			boolean AtLeaseOneSuccesor = false;
			
			// List<ProgramStateTransition> programEdges = assistant.getProgramEnabledTrans(node);
			
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
					Pair s = new Pair(p, a);
					StateSpace.push(s);
					CompoundStack.push(p);
					dfs(a);
					continue;
				}
				
				Pair p = new Pair(nextNode, state);
				Pair s = new Pair(p, a);

				
				// if(!StateSpace.contains(s) || (node.equals(nextNode)&&state.equals(state)))
				if(!compare(StateSpace, nextNode, state, a) || (node.equals(nextNode)&&(a == 2)))
				{
					StateSpace.push(s);
					CompoundStack.push(p);
					Dfs(a);
				// }else if(CompoundStack.contains(p))
				// }else
 				}else if(!compare2(CompoundStack, nextNode, state))
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
	public void Dfs(int b)
	{
		NeverState state = CompoundStack.peek().getSecond();
		ProgramState node = CompoundStack.peek().getFirst();
		
		List<OutgoingInternalTransition<CodeBlock, NeverState>> neverEdges = assistant.getNeverEnabledTrans(state, node);
		
		for (int j = 0;j < neverEdges.size();j++) {
			if(match) {return;}
			OutgoingInternalTransition<CodeBlock, NeverState> neverEdge = neverEdges.get(j);
			NeverState nextState = assistant.doNeverTransition(state, neverEdge, node);
			
			StateSpace.pop();
			CompoundStack.pop();

			Pair p = new Pair(node, nextState);
			Pair s = new Pair(p, b);
			
			// if(b==2 && node.equals(seed.getFirst()) && nextState.equals(seed.getSecond()))
			if(b==2 && compare(StateSpace, node, nextState, b))
			{
				match = true;
				mLogger.info("Violation of LTL property");
				return;
			}
			
			
			StateSpace.push(s);			
			CompoundStack.push(p);			
			dfs(b);

		}

		if(!CompoundStack.empty())
		{
			if(match) {return;}
			if(b == 1 && CompoundStack.peek().getSecond().isFinal())
			{
				seed = CompoundStack.peek();
				dfs(2);
			}	
			CompoundStack.pop();
			StateSpace.pop();
		}
		
	}

}
