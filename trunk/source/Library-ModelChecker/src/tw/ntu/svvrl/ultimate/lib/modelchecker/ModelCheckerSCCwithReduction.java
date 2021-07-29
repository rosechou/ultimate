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
import java.util.stream.Collectors;
import java.util.*;

public class ModelCheckerSCCwithReduction {
	private final ModelCheckerAssistant assistant;
	private final ILogger mLogger;
	private boolean match = false;
	int count = 1;
	private List<ProgramState> levelNodes = new ArrayList<ProgramState>();
	Pair<ProgramState, NeverState> pre = null;
	Stack<Pair<Pair<ProgramState, NeverState>, Integer>> dfsnum = new Stack<>();
//	Stack<Pair<Pair<ProgramState, NeverState>, Boolean>> current = new Stack<>();
	Stack<Pair<ProgramState, NeverState>> Roots = new Stack<>();
	Stack<Pair<ProgramState, NeverState>> StateSpace = new Stack<>();
	Stack<Pair<Pair<ProgramState, NeverState>, List<Long>>> ErrorPath = new Stack<>();
	Stack<Pair<Pair<ProgramState, NeverState>, List<Long>>> CyclePath = new Stack<>();
	
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
				dfs();
			}
		}
		if(!match) 
		{
			mLogger.info("All specifications hold");
			return;
		}
	}
	public List<Long> addOrderofProcesses(Stack<Pair<Pair<ProgramState, NeverState>, List<Long>>> ErrorPath, ProgramState node)
	{
		List<Long> empty = new ArrayList<>();
		empty.add((long) 0);
		for(int i = 0; i < ErrorPath.size();i++)
		{
			if(node.toString().equals(ErrorPath.get(i).getFirst().getFirst().toString()))
			{
				return ErrorPath.get(i).getSecond();
			}
		}
		return empty;
	}
	
	public boolean compare(Stack<Pair<ProgramState, NeverState>> path, ProgramState node, NeverState state)
	{
		for(int i = 0; i < path.size();i++)
		{
			if(node.toString().equals(path.get(i).getFirst().toString()) && state.toString().equals(path.get(i).getSecond().toString()))
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
			if(node.toString().equals(path.get(i).getFirst().getFirst().toString()) && state.toString().equals(path.get(i).getFirst().getSecond().toString()))
			{
				return path.get(i).getSecond();
			}
		}
		return 0;
	}
	
	private static List<Long> getIntersectOfLists1(List<Long> list1, List<Long> list2) {
		List<Long> intersectElements = list1.stream()
				.filter(list2 :: contains)
				.collect(Collectors.toList());
		
		if(!intersectElements.isEmpty()) {
			return intersectElements;
		}else {
			return Collections.emptyList();
		}
	}
	
	public List<List<Long>> getFairList(Stack<Pair<Pair<ProgramState, NeverState>, List<Long>>> path)
	{
		List<List<Long>> FairList = new ArrayList<>();
		for(int i = 0;i<path.size();i++)
		{
			FairList.add(path.get(i).getSecond());
		}
		return FairList;
	}
	
	public boolean compareErrorPath(List<List<Long>> FairList)
	{
		while(FairList.size()>1)
		{
			List<Long> temp = getIntersectOfLists1(FairList.get(0), FairList.get(1));
			FairList.remove(0);
			FairList.remove(0);
			FairList.add(temp);
		}
		if(FairList.get(0).size()<3)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/* system move */
	public void dfs()
	{
		pre = Roots.peek();
		count = count + 1;
		ProgramState node = Roots.peek().getFirst();
		NeverState state = Roots.peek().getSecond();
		
		List<Long> OrderofProcesses = assistant.getProgramSafestOrder(node);
		List<ProgramStateTransition> programEdges = new ArrayList<ProgramStateTransition>();
		
		/* Without Partial Order Reduction */
//		Set<Long> Processes = node.getThreadIDs();
//		mLogger.info("getThreadIDs()"+Processes);
//		List<Long> OrderofProcesses = new ArrayList<>(Processes);
		
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
					dfs();
					continue;
				}
				
				Pair p = new Pair(nextNode, state);

//				if(!compare(StateSpace, nextNode, state))
//				{
				List<Long> f = OrderofProcesses;
				Pair l = new Pair(p, f);
				ErrorPath.push(l);
				//mLogger.info("ErrorPath: "+ErrorPath.peek().getFirst().getFirst().toString()+ErrorPath.peek().getFirst().getSecond().getName()+ErrorPath.peek().getSecond());
				
				dfsnum.push(new Pair(p, count));
				Roots.push(p);
				StateSpace.push(p);
				Dfs();
// 				}
//				else if(compare(Roots, nextNode, state))
//				{
//					NotInStack = false;
//				}
//				AtLeaseOneSuccesor = true;
			}
//			if(AtLeaseOneSuccesor && NotInStack)
//			{
//				break;
//			}
		}
	}
	/* specification move */
	public void Dfs()
	{
		NeverState state = Roots.peek().getSecond();
		ProgramState node = Roots.peek().getFirst();
		
		List<OutgoingInternalTransition<CodeBlock, NeverState>> neverEdges = assistant.getNeverEnabledTrans(state, node);
		
		if(neverEdges.contains(null))
		{
			return;
		}
		
		for (int j = 0;j < neverEdges.size();j++) {
			if(match) {return;}
			OutgoingInternalTransition<CodeBlock, NeverState> neverEdge = neverEdges.get(j);
			NeverState nextState = assistant.doNeverTransition(state, neverEdge, node);
			
			if(j==0)
			{
				dfsnum.pop();
				Roots.pop();
				StateSpace.pop();
			}

			Pair p = new Pair(node, nextState);
			
			if(!compare(StateSpace, node, nextState))
			{
				dfsnum.push(new Pair(p, count));
				StateSpace.push(p);
				Roots.push(p);
				if(j!=0)
				{
					Pair l = new Pair(p, addOrderofProcesses(ErrorPath, node));
					ErrorPath.push(l);
				}
//				mLogger.info(StateSpace.peek().getFirst().toString()+StateSpace.peek().getSecond().getName()+"("+dfsnum.peek().getSecond()+")"+StateSpace.size());
				mLogger.info(StateSpace.peek().getFirst().toString()+StateSpace.peek().getSecond().getName()+"(dfsnum: "+dfsnum.peek().getSecond()+")");
				//mLogger.info(Roots.size());	
				dfs();
			}
			else if(compare(Roots, node, nextState))
			{	
//				if(Roots.peek().getFirst().toString().equals(node.toString()) && Roots.peek().getSecond().toString().equals(nextState.toString()))
//				{
//					Roots.pop();
//					mLogger.info("continue");
//					continue;
//				}
				
//				mLogger.info("#####Roots contains:#####");
//				mLogger.info(Roots.peek().getFirst().toString()+Roots.peek().getSecond().getName());
//				mLogger.info("node: "+node.toString()+";"+"nextState: "+nextState.getName());
				Pair<ProgramState, NeverState> element;
				//Pair<Pair<ProgramState, NeverState>, Integer> dfselement;
				do
				{
					//dfselement = dfsnum.pop();
					element = Roots.pop();
//					mLogger.info(element.getSecond().getName());
					if(element.getSecond().isFinal())
					{
						// CyclePath.clear();
						/* check fairness */
//						while(!(ErrorPath.peek().getFirst().getFirst().toString().equals(element.getFirst().toString()) 
//								&& (ErrorPath.peek().getFirst().getSecond().toString().equals(element.getSecond().toString()))))
						List<List<Long>> fList = new ArrayList<>();
//						while(!(StateSpace.peek().getFirst().toString().equals(node.toString()) 
//								&& (StateSpace.peek().getSecond().toString().equals(nextState.toString()))))
//						{
//							mLogger.info(StateSpace.peek().getFirst().toString()+StateSpace.peek().getSecond().getName());
//							addOrderofProcesses(ErrorPath, );
//							CyclePath.add(ErrorPath.peek());
//							ErrorPath.pop();
//							Roots.pop();
//						}
						for(int s = StateSpace.size()-1;s>0;s--)
						{
							if(!(StateSpace.get(s).getFirst().toString().equals(node.toString()) 
									&& (StateSpace.get(s).getSecond().toString().equals(nextState.toString()))))
							{
//								mLogger.info(StateSpace.get(s).getFirst().toString()+StateSpace.get(s).getSecond().getName());
								fList.add(addOrderofProcesses(ErrorPath, StateSpace.get(s).getFirst()));
							}
							else
							{
								break;
							}
						}
						fList.add(addOrderofProcesses(ErrorPath, node));
//						CyclePath.add(ErrorPath.peek());
						// List<List<Long>> fList = getFairList(CyclePath);
						for(int a = 0; a < fList.size();a++)
						{
							mLogger.info(fList.get(a).toString());
						}
//						mLogger.info(fList.size());
//						mLogger.info(fList.size()<5);
						boolean x = (fList.size()<5);
						boolean y = compareErrorPath(fList);
//						mLogger.info("compareErrorPath(fList)"+compareErrorPath(fList));
//						mLogger.info("(compareErrorPath(fList)) && (fList.size()<5)"+((compareErrorPath(fList)) & (fList.size()<5)));
						//if(((compareErrorPath(fList)) && (fList.size()<5)))
						if(y)
						{
							if(x)
							{
//								mLogger.info("element_dfsnm: "+compareDfsnum(dfsnum, element.getFirst(),element.getSecond()));
//								mLogger.info("nextState_dfsnm: "+compareDfsnum(dfsnum, node, nextState));
								match = true;
								Roots.push(element);
								Roots.push(p);
								StateSpace.push(p);
								for(int a = 0; a < StateSpace.size();a++)
								{
									mLogger.info(StateSpace.get(a).getFirst().getThreadStates().toString() + StateSpace.get(a).getSecond().getName());
								}
								mLogger.info("Violation of LTL property");
								return;
							}
							return;
						}
					}
					
				}while(compareDfsnum(dfsnum, element.getFirst(),element.getSecond())
					> compareDfsnum(dfsnum, node, nextState));
				Roots.push(element);
				// dfsnum.push(dfselement);
				//dfs();
			}
			
			
		}
		if(!Roots.isEmpty())
		{
//			if(pre.getFirst().toString().equals(Roots.peek().getFirst().toString()) && pre.getSecond().toString().equals(Roots.peek().getSecond().toString()))
//			{
				Roots.pop();
//			}
		}
	}
}
