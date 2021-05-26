package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state;

public interface IState<S extends IState<S, T>, T> {
	
	public boolean equals(S anotherS);
}
