package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state;

import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.Valuation;

public abstract class ValuationState<S extends ValuationState<S>> implements IState<S> {
	protected Valuation mValuation;
	
	public Valuation getValuationCopy() {
		return mValuation.clone();
	}
}
