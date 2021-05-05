package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.explorer;

import java.util.Map;

import de.uni_freiburg.informatik.ultimate.boogie.ast.BoogieASTNode;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.DefaultIcfgSymbolTable;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramVar;

public class StateExplorer {
	private final DefaultIcfgSymbolTable mIcfgSymbolTable;
	private final Map<IProgramVar, BoogieASTNode> mBoogieVar2AstNode;

	public StateExplorer() {
		mIcfgSymbolTable = null;
		mBoogieVar2AstNode = null;
	}
}
