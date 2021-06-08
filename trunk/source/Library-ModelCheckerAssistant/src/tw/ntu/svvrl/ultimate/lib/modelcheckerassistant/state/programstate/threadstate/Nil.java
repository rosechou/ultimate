package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.threadstate;


import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IIcfgInternalTransition;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IcfgLocation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;

public class Nil extends CodeBlock implements IIcfgInternalTransition<IcfgLocation> {

	private static final long serialVersionUID = -8598381335761071681L;

	public Nil(int serialNumber, BoogieIcfgLocation source, BoogieIcfgLocation target, ILogger logger) {
		super(serialNumber, source, target, logger);
	}

	@Override
	public String getPrettyPrintedStatements() {
		return "Nil";
	}

	@Override
	public String toString() {
		return "Nil";
	}

}
