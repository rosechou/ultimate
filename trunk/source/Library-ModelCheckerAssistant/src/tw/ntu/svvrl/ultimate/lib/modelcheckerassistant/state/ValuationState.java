package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state;

import tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state.programstate.Valuation;

public abstract class ValuationState<S extends ValuationState<S, T>, T> implements IState<S, T> {
	protected Valuation mValuation;
	
	public Valuation getValuationCopy() {
		return mValuation.clone();
	}
}
