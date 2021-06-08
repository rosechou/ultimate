package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate;

/**
 * If the {@link ProgramState} has no successor, attach a nil self-loop.
 * @see the definition of synchronous product.
 */
public class NilSelfLoop extends ProgramStateTransition {
	public NilSelfLoop() {
		
	}
}
