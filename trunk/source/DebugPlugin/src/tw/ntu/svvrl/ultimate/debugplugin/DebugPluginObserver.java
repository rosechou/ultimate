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
		
		
		ProgramState aState = ((ProgramState) pInitialStates.toArray()[0]);
		List<IcfgEdge> edges = aState.getEnableTrans();
		IcfgEdge edge = edges.get(0);
		ProgramState bState = aState.doTransition(edge);
		edges = bState.getEnableTrans();
		edge = edges.get(0);
		ProgramState cState = bState.doTransition(edge);
		
		
		Set<NeverState> nInitialStates = new HashSet<>();
		nInitialStates = mModelCheckerAssistant.getNeverInitialStates();
		NeverState nState = ((NeverState) nInitialStates.toArray()[0]);
		List<OutgoingInternalTransition<CodeBlock, NeverState>> nedges = nState.getEnableTrans(cState);
		OutgoingInternalTransition<CodeBlock, NeverState> nedge = nedges.get(0);
		NeverState mState = nState.doTransition(nedge, cState);
		
//		ProgramState aState = explorer.getLocStateById("mainENTRY");
//		List<IcfgEdge> edges = aState.getEnableTrans();
//		IcfgEdge edge = edges.get(0);
//		ProgramState bState = aState.doTransition(edge);
//		edges = bState.getEnableTrans();
//		edge = edges.get(0);
//		ProgramState cState = bState.doTransition(edge);
//		edges = cState.getEnableTrans();
//		edge = edges.get(0);
//		ProgramState dState = cState.doTransition(edge);
//		edges = dState.getEnableTrans();
//		edge = edges.get(0);
//		ProgramState eState = dState.doTransition(edge);
//		edges = eState.getEnableTrans();
//		edge = edges.get(0);
//		ProgramState fState = eState.doTransition(edge);
//		edges = fState.getEnableTrans();
//		edge = edges.get(0);
//		ProgramState gState = fState.doTransition(edge);
//		edges = gState.getEnableTrans();
//		edge = edges.get(0);
//		ProgramState hState = gState.doTransition(edge);
//		edges = hState.getEnableTrans();
		/*-------------------------------*/
		
//		for(ProgramState initialState : initialStates) {
//			initialState.getEnableTrans();
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
