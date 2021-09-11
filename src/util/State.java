package util;

import static java.util.stream.Collectors.joining;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class State<S extends State, I extends Rule> {

	protected Set<I> items;
	protected Map<String, S> transitions;

	public Set<I> getItems() {
		return items;
	}

	public Map<String, S> getTransitions() {
		return transitions;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 83 * hash + Objects.hashCode(items);
		hash = 83 * hash + Objects.hashCode(transitions);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		final S other = (S) obj;
		if (!items.equals(other.items)) return false;
		if (!transitions.equals(other.transitions)) return false;
		return true;
	}

	@Override
	public String toString() {
		return items.stream().map(I::toString).collect(joining("\n"));
	}
}
