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
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadStateTransition;
import de.uni_freiburg.informatik.ultimate.util.datastructures.relation.Pair;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.neverstate.NeverStateFactory;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.ModelCheckerAssistant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Stack;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.*;

public class ModelCheckerDoubleDFS{
	private final ModelCheckerAssistant assistant;
	private final ILogger mLogger;
	private boolean match = false;
	//	ProgramState is the node of the Control Flow Graph
	private List<ProgramState> levelNodes = new ArrayList<ProgramState>();
	// ProgramState preState = null;
	ProgramState loc = null;
	ProgramState seed = null;
	NeverState Neverseed = null;
	Stack<ProgramState> firstVisited = new Stack<>();
	Stack<ProgramState> secondVisited = new Stack<>();

	
	protected Set<NeverState> initStates =new HashSet<>();
	
	Stack<Pair<ProgramState, NeverState>> bluepath = new Stack<>();
	Stack<Pair<ProgramState, NeverState>> redpath = new Stack<>();
	
	public ModelCheckerDoubleDFS(final ILogger logger, final ModelCheckerAssistant mca)
	{	
		mLogger = logger;
		assistant = mca;
		// set of initial cfg locations
		levelNodes.addAll(assistant.getProgramInitialStates());
		
		// set of initial states of automaton
		Set<NeverState> initStates = new HashSet<>();
		initStates = assistant.getNeverInitialStates();
		
		// Iterator initIterator = initStates.iterator();
		// NeverState init = (NeverState) initIterator.next();
		
		for (int i = 0;i < levelNodes.size();i++) {
			for(int j = 0;j < initStates.size();j++)
			{
				if(match) {return;}
				NeverState init = ((NeverState) initStates.toArray()[j]);
				dfsBlue(levelNodes.get(i), init);
			}
		}
		
	}
	
	public void dfsRed(ProgramState node, NeverState state)
	{
		secondVisited.push(node);
		List<ThreadStateTransition> programEdges = node.getEnableTrans();
		
		List<ProgramState> nProgramNodes = new ArrayList<ProgramState>();
		for(int j = 0;j < programEdges.size();j++)
		{
			nProgramNodes.add(node.doTransition(programEdges.get(j)));
		}
		levelNodes = nProgramNodes;
		
		for (int i = 0;i < levelNodes.size();i++) {
			ProgramState nextNode = levelNodes.get(i);
			
			List<OutgoingInternalTransition<CodeBlock, NeverState>> neverEdges = state.getEnableTrans(nextNode);
			
//			for(int k = 0;k < firstVisited.size();k++)
//			{
//				if(nextNode.equals(firstVisited.get(k)) && (neverEdges.size()>0))
//				{
//					match = true;
//					mLogger.info("report cycle");
//					return;
//				}
//			}
//			else if(nextNode.equals(seed) && nextState.equals(Neverseed))
			
			for (int j = 0;j < neverEdges.size();j++) {
				OutgoingInternalTransition<CodeBlock, NeverState> neverEdge = neverEdges.get(j);
				NeverState nextState = state.doTransition(neverEdge, nextNode);
				
				if(!secondVisited.contains(nextNode))
				{
					Pair p = new Pair(node, state);
					redpath.push(p);
					dfsRed(nextNode, nextState);
				}
				else if(nextNode.equals(seed))
//				else if(firstVisited.contains(nextNode))
				{
					match = true;
					mLogger.info("report cycle");
					return;
				}
			}	
		}
		if(!redpath.empty())
		{
			redpath.pop();
		}
	}
		
	public void dfsBlue(ProgramState node, NeverState init)
	{
		firstVisited.push(node);
		List<ThreadStateTransition> programEdges = node.getEnableTrans();
		
		List<ProgramState> nProgramNodes = new ArrayList<ProgramState>();
		for(int j = 0;j < programEdges.size();j++)
		{
			nProgramNodes.add(node.doTransition(programEdges.get(j)));
		}
		levelNodes = nProgramNodes;
		
//		anotherTrans:
		for (int i = 0;i < levelNodes.size();i++) {
			if(match) {return;}
			ProgramState nextNode = levelNodes.get(i);
			
//			for(int k = 0;k < firstVisited.size();k++)
//			{
//				if(nextNode.equals(firstVisited.get(k)))
//				{
//					levelNodes.remove(i);
//					break anotherTrans;
//				}
//			}
			
			if(!firstVisited.contains(nextNode))
			{
				List<OutgoingInternalTransition<CodeBlock, NeverState>> neverEdges = init.getEnableTrans(nextNode);
				
				for (int j = 0;j < neverEdges.size();j++) {
					if(match) {return;}
					OutgoingInternalTransition<CodeBlock, NeverState> neverEdge = neverEdges.get(j);
					NeverState nextState = init.doTransition(neverEdge, nextNode);
					
					Pair p = new Pair(node, init);
					bluepath.push(p);
					dfsBlue(nextNode, nextState);
				}	
			}
			
//			List<OutgoingInternalTransition<CodeBlock, NeverState>> neverEdges = init.getEnableTrans(nextNode);
//			
//			for (int j = 0;j < neverEdges.size();j++) {
//				if(match) {return;}
//				OutgoingInternalTransition<CodeBlock, NeverState> neverEdge = neverEdges.get(j);
//				NeverState nextState = init.doTransition(neverEdge, nextNode);
//				
//				Pair p = new Pair(node, init);
//				bluepath.push(p);
//				
//				dfsBlue(nextNode, nextState);
//			}	
		}
		
		if(!bluepath.empty())
		{
			if(match) {return;}
			if(bluepath.peek().getSecond().isFinal())
			{
				seed = bluepath.peek().getFirst();
				Neverseed = bluepath.peek().getSecond();
				dfsRed(seed, Neverseed);
			}
			bluepath.pop();
		}
	}
}
