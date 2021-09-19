package util;

import static java.util.stream.Collectors.joining;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class State<S extends State, I extends Rule> {

	protected Set<I> items;
	protected Map<String, S> transitions = new LinkedHashMap<>();

	public Set<I> getItems() {
		return items;
	}

	public Map<String, S> getTransitions() {
		return transitions;
	}

	@Override
	public String toString() {
		return items.stream().map(I::toString).collect(joining("\n"));
	}
}
