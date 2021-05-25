package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state;

import java.util.List;


public interface IState<S extends IState<S>> {
	public boolean equals(S anotherS);
}
