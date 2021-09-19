package util;

import static java.lang.String.join;

import java.util.Arrays;
import java.util.Objects;

public class Rule {

	protected String lhs;
	protected String[] rhs;

	public Rule(String lhs, String ... rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}

	public Rule(Rule rule) {
		this(rule.lhs, rule.rhs);
	}

	public String getLhs() {
		return lhs;
	}

	public String[] getRhs() {
		return rhs;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + Objects.hashCode(lhs);
		return 31 * hash + Arrays.hashCode(rhs);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		final Rule other = (Rule) obj;
		if (!lhs.equals(other.lhs)) return false;
		if (!Arrays.equals(rhs, other.rhs)) return false;
		return true;
	}

	@Override
	public String toString() {
		return lhs + " -> " + join(" ", rhs); 
	}
}
