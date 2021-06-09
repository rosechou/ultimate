/*
 * Copyright (C) 2014-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * Copyright (C) 2013-2015 Vincent Langenfeld (langenfv@informatik.uni-freiburg.de)
 *
 * This file is part of the ULTIMATE DebugPlugin plug-in.
 *
 * The ULTIMATE DebugPlugin plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ULTIMATE DebugPlugin plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE DebugPlugin plug-in. If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE DebugPlugin plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP),
 * containing parts covered by the terms of the Eclipse Public License, the
 * licensors of the ULTIMATE DebugPlugin plug-in grant you additional permission
 * to convey the resulting work.
 */
package tw.ntu.svvrl.ultimate.debugplugin;

import java.util.ArrayList;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.*;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.explorer.NeverClaimAutExplorer;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.explorer.ProgramStateExplorer;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.neverstate.NeverState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramState;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramStateTransition;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate.ThreadStateTransition;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.ProgramState;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.automata.nestedword.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.NestedWordAutomataSizeBenchmark;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.boogie.annotation.LTLPropertyCheck;
import de.uni_freiburg.informatik.ultimate.core.model.models.IElement;
import de.uni_freiburg.informatik.ultimate.core.model.models.ModelType;
import de.uni_freiburg.informatik.ultimate.core.model.observers.IUnmanagedObserver;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgEdge;
import de.uni_freiburg.informatik.ultimate.ltl2aut.never2nwa.NWAContainer;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgContainer;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.util.IcfgSizeBenchmark;

/**
 *
 * @author Hong-Yang Lin
 *
 */
public class DebugPluginObserver implements IUnmanagedObserver {
	
	private final ILogger mLogger;
	private BoogieIcfgContainer mRcfg;
	private NWAContainer mNeverClaimNWAContainer;
	private final IUltimateServiceProvider mServices;
	
	private ModelCheckerAssistant mModelCheckerAssistant;

	public DebugPluginObserver(final ILogger logger, final IUltimateServiceProvider services) {
		mLogger = logger;
		mServices = services;
		mRcfg = null;
		mNeverClaimNWAContainer = null;
		mModelCheckerAssistant = null;
	}

	@Override
	public void init(final ModelType modelType, final int currentModelIndex, final int numberOfModels) {
		// no initialization needed
	}

	@Override
	public void finish() throws Throwable {
		if (mNeverClaimNWAContainer == null || mRcfg == null) {
			return;
		}

		// measure size of nwa and rcfg
		reportSizeBenchmark("Initial property automaton", mNeverClaimNWAContainer.getValue());
		reportSizeBenchmark("Initial RCFG", mRcfg);

		mLogger.info("Do something with these two models...");
		// new crawler here. 
		mModelCheckerAssistant = new ModelCheckerAssistant(mNeverClaimNWAContainer.getValue(), mRcfg, mLogger, mServices);
		// mModelCheckerAssistant = new ModelCheckerAssistant(mRcfg, mLogger, mServices);
		
		/*-----------debugging-----------*/
		Set<ProgramState> pInitialStates = new HashSet<>();
		pInitialStates = mModelCheckerAssistant.getProgramInitialStates();
		
		Set<NeverState> nInitialStates = new HashSet<>();
		nInitialStates = mModelCheckerAssistant.getNeverInitialStates();
		NeverState n = ((NeverState) nInitialStates.toArray()[0]);
		
		
		ProgramState a = ((ProgramState) pInitialStates.toArray()[0]);
		List<ProgramStateTransition> edges = a.getEnabledTrans();
		ProgramStateTransition edge = edges.get(0);
		ProgramState b = a.doTransition(edge);
		
		List<OutgoingInternalTransition<CodeBlock, NeverState>> nedges = n.getEnabledTrans(b);
		if(nedges.size() > 0) {
			OutgoingInternalTransition<CodeBlock, NeverState> nedge = nedges.get(0);
			NeverState m = n.doTransition(nedge, b);
			n = m;
		}
		
		edges = b.getEnabledTrans();
		edge = edges.get(0);
		ProgramState c = b.doTransition(edge);
		
		edges = c.getEnabledTrans();
		edge = edges.get(0);
		ProgramState d = c.doTransition(edge);
		
		edges = d.getEnabledTrans();
		edge = edges.get(0);
		ProgramState e = d.doTransition(edge);
		
		edges = e.getEnabledTrans();
		edge = edges.get(0);
		ProgramState f = e.doTransition(edge);
		
		edges = f.getEnabledTrans();
		edge = edges.get(0);
		ProgramState g = f.doTransition(edge);
		
		nedges = n.getEnabledTrans(g);
		if(nedges.size() > 0) {
			OutgoingInternalTransition<CodeBlock, NeverState> nedge = nedges.get(0);
			NeverState m = n.doTransition(nedge, g);
			n = m;
		}
		
		edges = g.getEnabledTrans();
		edge = edges.get(0);
		ProgramState h = g.doTransition(edge);
		
		edges = h.getEnabledTrans();
		edge = edges.get(0);
		ProgramState i = h.doTransition(edge);
		
		edges = i.getEnabledTrans();
		edge = edges.get(0);
		ProgramState j = i.doTransition(edge);
		
		edges = j.getEnabledTrans();
		edge = edges.get(0);
		ProgramState k = j.doTransition(edge);
		
		edges = k.getEnabledTrans();
		edge = edges.get(0);
		ProgramState l = k.doTransition(edge);
		
		edges = l.getEnabledTrans();
		edge = edges.get(0);
		ProgramState o = l.doTransition(edge);
		
		edges = o.getEnabledTrans();
		edge = edges.get(0);
		ProgramState p = o.doTransition(edge);
		
		edges = p.getEnabledTrans();
		edge = edges.get(0);
		ProgramState q = p.doTransition(edge);
		
		edges = q.getEnabledTrans();
		edge = edges.get(0);
		ProgramState r = q.doTransition(edge);
		
		edges = r.getEnabledTrans();
		edge = edges.get(0);
		ProgramState s = r.doTransition(edge);
		
		edges = s.getEnabledTrans();
		edge = edges.get(0);
		ProgramState t = s.doTransition(edge);
		
		edges = t.getEnabledTrans();
		edge = edges.get(0);
		ProgramState u = t.doTransition(edge);
		
		edges = u.getEnabledTrans();
		edge = edges.get(0);
		ProgramState v = u.doTransition(edge);
		
		edges = v.getEnabledTrans();
		edge = edges.get(0);
		ProgramState w = v.doTransition(edge);
		
		edges = w.getEnabledTrans();
		edge = edges.get(0);
		ProgramState y = w.doTransition(edge);
		
		edges = y.getEnabledTrans();
		edge = edges.get(0);
		ProgramState z = y.doTransition(edge);
		
		edges = z.getEnabledTrans();
		edge = edges.get(0);
		ProgramState A = z.doTransition(edge);
		
		edges = A.getEnabledTrans();
		edge = edges.get(0);
		ProgramState B = A.doTransition(edge);
		
		edges = B.getEnabledTrans();
		edge = edges.get(0);
		ProgramState C = B.doTransition(edge);
		
		edges = C.getEnabledTrans();
		edge = edges.get(0);
		ProgramState D = C.doTransition(edge);
		
		edges = D.getEnabledTrans();
		edge = edges.get(0);
		ProgramState E = D.doTransition(edge);
		
		edges = E.getEnabledTrans();
		edge = edges.get(0);
		ProgramState F = E.doTransition(edge);
		
		edges = F.getEnabledTrans();
		edge = edges.get(0);
		ProgramState G = F.doTransition(edge);
		
		edges = G.getEnabledTrans();
		edge = edges.get(0);
		ProgramState H = G.doTransition(edge);
		
		edges = H.getEnabledTrans();
		edge = edges.get(0);
		ProgramState I = H.doTransition(edge);
		
		edges = I.getEnabledTrans();
		edge = edges.get(0);
		ProgramState J = I.doTransition(edge);
		
		edges = J.getEnabledTrans();
		edge = edges.get(0);
		ProgramState K = J.doTransition(edge);
		
		edges = K.getEnabledTrans();
		edge = edges.get(0);
		ProgramState L = K.doTransition(edge);
		
		edges = L.getEnabledTrans();
		edge = edges.get(0);
		ProgramState M = L.doTransition(edge);
		
		edges = M.getEnabledTrans();
		edge = edges.get(0);
		ProgramState N = M.doTransition(edge);
		
		edges = N.getEnabledTrans();
		edge = edges.get(0);
		ProgramState O = N.doTransition(edge);
		
		edges = O.getEnabledTrans();
		edge = edges.get(0);
		ProgramState P = O.doTransition(edge);
		
		edges = P.getEnabledTrans();
		edge = edges.get(0);
		ProgramState Q = P.doTransition(edge);
		
		edges = Q.getEnabledTrans();
		edge = edges.get(0);
		ProgramState R = Q.doTransition(edge);
		
		edges = R.getEnabledTrans();
		edge = edges.get(0);
		ProgramState S = R.doTransition(edge);
		
		edges = S.getEnabledTrans();
		
//		nedges = n.getEnabledTrans(c);
//		if(nedges.size() > 0) {
//			OutgoingInternalTransition<CodeBlock, NeverState> nedge = nedges.get(0);
//			NeverState m = n.doTransition(nedge, c);
//			n = m;
//		}
//		
//		edges = c.getEnabledTrans();
//		edge = edges.get(0);
//		ProgramState d = c.doTransition(edge);
//		
//		nedges = n.getEnabledTrans(d);
//		if(nedges.size() > 0) {
//			OutgoingInternalTransition<CodeBlock, NeverState> nedge = nedges.get(0);
//			NeverState m = n.doTransition(nedge, d);
//			n = m;
//		}
//		
//		edges = d.getEnabledTrans();
//		edge = edges.get(0);
//		ProgramState e = d.doTransition(edge);
//		
//		nedges = n.getEnabledTrans(e);
//		if(nedges.size() > 0) {
//			OutgoingInternalTransition<CodeBlock, NeverState> nedge = nedges.get(0);
//			NeverState m = n.doTransition(nedge, e);
//			n = m;
//		}
//		
//		edges = e.getEnabledTrans();
//		edge = edges.get(0);
//		ProgramState f = e.doTransition(edge);
//		
//		nedges = n.getEnabledTrans(f);
//		if(nedges.size() > 0) {
//			OutgoingInternalTransition<CodeBlock, NeverState> nedge = nedges.get(1);
//			NeverState m = n.doTransition(nedge, f);
//			n = m;
//		}
//		
//		edges = f.getEnabledTrans();
//		edge = edges.get(0);
//		ProgramState g = f.doTransition(edge);
//		
//		nedges = n.getEnabledTrans(g);
//		if(nedges.size() > 0) {
//			OutgoingInternalTransition<CodeBlock, NeverState> nedge = nedges.get(0);
//			NeverState m = n.doTransition(nedge, g);
//			n = m;
//		}
//		
//		edges = g.getEnabledTrans();
//		edge = edges.get(0);
//		ProgramState h = g.doTransition(edge);
//		
//		nedges = n.getEnabledTrans(h);
//		if(nedges.size() > 0) {
//			OutgoingInternalTransition<CodeBlock, NeverState> nedge = nedges.get(0);
//			NeverState m = n.doTransition(nedge, h);
//			n = m;
//		}
	
		
//		for(ProgramState initialState : initialStates) {
//			initialState.getEnabledTrans();
//		}
		// Do something...
	}

	private void reportSizeBenchmark(final String message, final INestedWordAutomaton<CodeBlock, String> nwa) {
		final NestedWordAutomataSizeBenchmark<CodeBlock, String> bench =
				new NestedWordAutomataSizeBenchmark<>(nwa, message);
		mLogger.info(message + " " + bench);
		bench.reportBenchmarkResult(mServices.getResultService(), Activator.PLUGIN_ID, message);
	}

	private void reportSizeBenchmark(final String message, final BoogieIcfgContainer root) {
		final IcfgSizeBenchmark bench = new IcfgSizeBenchmark(root, message);
		mLogger.info(message + " " + bench);
		bench.reportBenchmarkResult(mServices.getResultService(), Activator.PLUGIN_ID, message);
	}

	@Override
	public boolean performedChanges() {
		return false;
	}

	// No model generated so far.
	public IElement getModel() {
		return null;
	}

	/**
	 * Collect one RCFG and one LTL2Aut.AST
	 */
	@Override
	public boolean process(final IElement root) throws Exception {
		// collect root nodes of Buchi automaton
		if (root instanceof NWAContainer) {
			mLogger.debug("Collecting NWA representing NeverClaim");
			mNeverClaimNWAContainer = (NWAContainer) root;
			return false;
		}

		// collect root node of program's RCFG
		if (root instanceof BoogieIcfgContainer) {
			mLogger.debug("Collecting RCFG RootNode");
			mRcfg = (BoogieIcfgContainer) root;
			return false;
		}
		return true;
	}
}
