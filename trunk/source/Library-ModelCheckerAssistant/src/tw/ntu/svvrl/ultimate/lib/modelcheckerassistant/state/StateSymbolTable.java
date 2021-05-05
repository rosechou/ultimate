package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.DefaultIcfgSymbolTable;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.IIcfgSymbolTable;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.ILocalProgramVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramConst;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramNonOldVar;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.variables.IProgramVar;
import de.uni_freiburg.informatik.ultimate.util.datastructures.relation.HashRelation;

/**
 * Implementation of an {@link IIcfgSymbolTable}
 * Keep all Boogie variables.
 *
 */
public class StateSymbolTable implements IStateSymbolTable {
	
	protected final Set<IProgramNonOldVar> mGlobals;
	protected final Set<IProgramConst> mConstants;
	protected final HashRelation<String, ILocalProgramVar> mLocals;

	/**
	 * Constructor for state symbol table.
	 * It copys a default Icfg symbol table and drop some 
	 * unused information(term variables related).
	 */
	public StateSymbolTable(final DefaultIcfgSymbolTable defaultIcfgSymbolTable) {
		mGlobals = defaultIcfgSymbolTable.getGlobals();
		mConstants = defaultIcfgSymbolTable.getConstants();
		mLocals = defaultIcfgSymbolTable.getLocalsRelation();
	}

	@Override
	public Set<IProgramNonOldVar> getGlobals() {
		return Collections.unmodifiableSet(mGlobals);
	}

	@Override
	public Set<IProgramConst> getConstants() {
		return Collections.unmodifiableSet(mConstants);
	}
	
	@Override
	public Set<ILocalProgramVar> getLocals(String procedurename) {
		final Set<ILocalProgramVar> locals = mLocals.getImage(proc);
		if (locals == null) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(mLocals.getImage(proc));
	}

}
