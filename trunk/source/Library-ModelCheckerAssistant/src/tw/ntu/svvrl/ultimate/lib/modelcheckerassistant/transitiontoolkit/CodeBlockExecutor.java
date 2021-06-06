package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.transitiontoolkit;

import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.IState;

public abstract class CodeBlockExecutor<S extends IState<S>> {
	protected S mCurrentState;
	protected CodeBlock mCodeBlock;

	protected abstract boolean checkEnabled();

}
