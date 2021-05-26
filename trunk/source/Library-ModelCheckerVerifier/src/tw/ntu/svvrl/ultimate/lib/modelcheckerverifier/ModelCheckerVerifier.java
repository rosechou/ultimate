package tw.ntu.svvrl.ultimate.lib.modelcheckerverifier;

import de.uni_freiburg.informatik.ultimate.automata.nestedword.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.transitions.IncomingInternalTransition;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.NestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.NestedWordAutomatonCache;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramState;
import de.uni_freiburg.informatik.ultimate.util.datastructures.relation.Pair;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.neverstate.NeverState;
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

/* The needed API
 
	public static boolean isInitial(ProgramState node){
		// TODO
	}
	public static boolean isAcc(STATE state){
		// TODO: return true, if the state is accepting state
	}
	public static boolean equals(ProgramState node){
		// TODO
	}
	
	public static Map<String, Object> getSpecValueMap(){
		// TODO
	}
	public static STATE succState(OutgoingInternalTransition tran){
		// TODO
	}
	
 */

public class ModelCheckerVerifier {
	private final ModelCheckerAssistant assistant = null;
	//	ProgramState is the node of the Control Flow Graph
	private final List<ProgramState> levelNodes = new ArrayList<ProgramState>();
	// ProgramState preState = null;
	ProgramState loc = null;
	ProgramState seed = null;
	Stack<ProgramState> firstVisited = new Stack<>();
	Stack<ProgramState> secondVisited = new Stack<>();
	
	private final INestedWordAutomaton<CodeBlock, String> mNWA; //<stmt, l0>
	protected final Set<NeverState> initStates;
	protected final Set<NeverState> finalStates;
	
	Stack<Pair<ProgramState, NeverState>> bluepath = new Stack<>();
	Stack<Pair<ProgramState, NeverState>> redpath = new Stack<>();
	
	private final ILogger mLogger;
	
	private final Map<String, Object> currentTable = new HashMap<>();
	
	public ModelCheckerVerifier(final INestedWordAutomaton<CodeBlock, String> nwa)
	{	
		mNWA = nwa;
		initStates = new HashSet<>();
		finalStates = new HashSet<>();
		
		// set of initial cfg locations
		levelNodes = new ArrayList<>();
		levelNodes.addAll(assistant.getProgramInitialStates());
		
		// set of initial states of automaton
		initStates = mNWA.getInitialStates();
		finalStates = mNWA.getFinalStates();
		Iterator initIterator = initStates.iterator();
		NeverState init = initIterator.next();
		
		dfsBlue(levelNodes, init);
	}
	
	public static void dfsRed(ProgramState node, NeverState state)
	{
		for (final OutgoingInternalTransition<LETTER, NeverState> outTrans : internalSuccessors(state)) {
			boolean match = true;
			
			// automaton walks one step
			currentTable = outTrans.getLetter().getSpecValueMap;
			
			// cfg walks one step
			loc = node;
			
			if(loc.equals(seed))
			{
				mLogger.info("report cycle");
			}
			secondVisited.push(node);
			
			// var, value
			HashMap<String, Object> NodeStatus = loc.getValuationMap().values();
			List<IcfgEdge> edge = loc.getEnableTrans();
			
			// check program satisfies spec, compare currentTable & NodeStatus
			Iterator currentEntry = currentTable.entrySet().iterator();
			while(currentEntry.hasNext())
			{
				Map.Entry entry = (Map.Entry)currentEntry.next();
				if(NodeStatus.containsKey(entry.getKey())
						&&(!NodeStatus.containsValue(entry.getValue())))
				{
						match = false;
						break;
				}
			}
			if(match)
			{	
				Pair p = new(loc, init);
				redpath.push(p);
				
				// automaton move to next state
				state = state.succState(outTrans);
				
				// new a ProgramState
				// preState = loc;
				List<ProgramState> nProgramState = new ArrayList<>();
				for(int j = 0;j < edge.size();j++)
				{
					nProgramState.add(doTransition(edge.get(j)));
				}
				levelNodes = nProgramState;
				
				for(int k = 0;k < levelNodes.size(); k++)
				{
					dfsRed(levelNodes.get(k), state);
				}
			}			
		}
		redpath.pop();
	}
		
	public static void dfsBlue(List<ProgramState> levelNodes, NeverState init)
	{
		for (final OutgoingInternalTransition<LETTER, NeverState> outTrans : internalSuccessors(init)) {
		anotherTrans:
			for (int i = 0;i < levelNodes.size();i++) {
				boolean match = true;
				
				// automaton walks one step
				currentTable = outTrans.getLetter().getSpecValueMap;
				
				// cfg walks one step
				loc = levelNodes.get(i);
				for(int k = 0;k < firstVisited.size();k++)
				{
					if(loc.equals(firstVisited.get(k)))
					{
						break anotherTrans;
					}
				}
				firstVisited.push(loc);
				
				// var, value
				HashMap<String, Object> NodeStatus = loc.getValuationMap().values();
				List<IcfgEdge> edge = loc.getEnableTrans();
				
				// check program satisfies spec, compare currentTable & NodeStatus
				Iterator currentEntry = currentTable.entrySet().iterator();
				while(currentEntry.hasNext())
				{
					Map.Entry entry = (Map.Entry)currentEntry.next();
					if(NodeStatus.containsKey(entry.getKey())
							&&(!NodeStatus.containsValue(entry.getValue())))
					{
							match = false;
							break;
					}
				}
				if(match)
				{	
					Pair p = new(loc, init);
					bluepath.push(p);
					
					// automaton move to next state
					init = init.succState(outTrans);
					
					// new a ProgramState
					// preState = loc;
					List<ProgramState> nProgramState = new ArrayList<>();
					for(int j = 0;j < edge.size();j++)
					{
						nProgramState.add(doTransition(edge.get(j)));
					}
					levelNodes = nProgramState;
					
					dfsBlue(levelNodes, init);
				}			
			}
		}
		if(bluepath.peek().getSecond().isAcc())
		{
			seed = blupath.peek().getFirst();
			dfsRed(seed, blupath.peek().getSecond());
		}
		bluepath.pop();
	}

}
