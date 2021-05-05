package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.explorer;

import java.util.Map;

import de.uni_freiburg.informatik.ultimate.boogie.ast.BoogieASTNode;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramVar;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgContainer;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.State;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.StateSymbolTable;

/**
 * This class explores the boogie program states with the help of
 * the pre-build {@link BoogieIcfgContainer}.
 * The {@link State}s is generated when a Icfg transition is going to be moved. 
 * 
 * @author Hong-Yang Lin
 */
public class StateExplorer {
	private final StateSymbolTable mCurrentStateSymbolTable;
	private final Map<IProgramVar, BoogieASTNode> mBoogieVar2AstNode;

	public StateExplorer(BoogieIcfgContainer rcfg) {
		mCurrentStateSymbolTable = 
				new StateSymbolTable(rcfg.getBoogie2SMT().getBoogie2SmtSymbolTable());
		mBoogieVar2AstNode = null;
	}
}
