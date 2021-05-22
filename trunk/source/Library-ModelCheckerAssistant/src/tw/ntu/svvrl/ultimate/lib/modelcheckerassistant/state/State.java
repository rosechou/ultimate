package tw.ntu.svvrl.ultimate.lib.modelcheckerassistant.state;

import java.util.List;




public interface State<S extends State<S, T>, T> {
	public List<T> getEnableTrans();
	public S doTransition(T t);
}
