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
	Map<Integer, ProgramState> dfsnum = new HashMap<Integer, ProgramState>();
	
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
	
	public void startSCC(ProgramState node, NeverState init)
	{
		count = count + 1;
		dfsnum.put(count, node);
		
		List<ProgramStateTransition> programEdges = node.getEnabledTrans();
		
		List<ProgramState> nProgramNodes = new ArrayList<ProgramState>();
		for(int j = 0;j < programEdges.size();j++)
		{
			nProgramNodes.add(node.doTransition(programEdges.get(j)));
		}
		levelNodes = nProgramNodes;
			
		for (int i = 0;i < levelNodes.size();i++) {
			if(match) {return;}
			ProgramState nextNode = levelNodes.get(i);
			
			List<OutgoingInternalTransition<CodeBlock, NeverState>> neverEdges = init.getEnabledTrans(nextNode);
	
			for (int j = 0;j < neverEdges.size();j++) {
				if(match) {return;}
				OutgoingInternalTransition<CodeBlock, NeverState> neverEdge = neverEdges.get(j);
				NeverState nextState = init.doTransition(neverEdge, nextNode);
				
				if(!compare(bluepath, nextNode, nextState))
				{

					Pair p = new Pair(node, init);
					bluepath.push(p);
					dfsBlue(nextNode, nextState);
				}	
					
			}


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
