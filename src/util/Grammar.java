package util;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.IntStream.range;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/*
	LR(0)
	
		S -> A B
		B -> A b
		A -> a
		
		a a b

	SRL(1), CLR(1), LALR(1)
	
		E -> T + E | T
		T -> F * T | F
		F -> ( E ) | id
		
		id + id * ( id + id )
*/

public class Grammar extends ArrayList<Rule> {
	private static final long serialVersionUID = 1L;
	
	private Set<String> terminals;
	private Set<String> variables;
	private String startVariable;
	private Map<String, Set<String>> firstSets;
	private Map<String, Set<String>> fallowSets;

	public static final String StartRule = "S'";
	public static final String EndToken = "$";
	public static final String Epsilon = "epsilon"; // TODO why not "\u03B5"?

	public Grammar(String str) {
		terminals = new LinkedHashSet<>();
		variables = new LinkedHashSet<>();
		int rule = 0;
		for (String line: str.split("\n")) {
			String[] sides = line.split("->");
			String lhs = sides[0].trim();
			variables.add(lhs);
			for (String rhs: sides[1].trim().split("\\|")) {
				String[] tokens = rhs.trim().split("\\s+");
				for (String token: tokens) {
					if (token.equals(Epsilon)) continue;
					terminals.add(token);
				}
				if (rule == 0) {
					startVariable = lhs;
					add(new Rule(StartRule, startVariable));
				}
				add(new Rule(lhs, tokens));
				rule += 1;
			}
		}
		terminals.removeAll(variables);
		computeFirstSets();
		computeFollowSets();
	}

	public Set<Rule> getRulesByLhs(String variable) {
		return stream().filter(r-> r.lhs.equals(variable)).collect(toCollection(LinkedHashSet::new));
	}

	public Set<String> getVariables() {
		return variables;
	}

	public String getStartVariable() {
		return startVariable;
	}

	public boolean isVariable(String symbol) {
		return variables.contains(symbol);
	}

	public Set<String> getTerminals() {
		return terminals;
	}
	
	public boolean isTerminal(String symbol) {
		return terminals.contains(symbol);
	}

	public Map<String, Set<String>> getFirstSets() {
		return firstSets;
	}

	public Map<String, Set<String>> getFallowSets() {
		return fallowSets;
	}

	private void computeFirstSets() {
		firstSets = new LinkedHashMap<>() {
			private static final long serialVersionUID = 1L;
			public String toString() { return keySet().stream().map(s-> s + ": " + get(s)).collect(joining("\n")); }
		};
		firstSets.put(StartRule, null);
		for (String variable: variables) firstSets.put(variable, new LinkedHashSet<>());
		boolean changed;
		do {
			changed = false;
			for (String variable: variables) {
				Set<String> firstSet = new LinkedHashSet<>();
				for (Rule rule: this) {
					if (!rule.lhs.equals(variable)) continue;
					firstSet.addAll(computeFirst(rule.rhs, 0));
				}
				if (firstSets.get(variable).containsAll(firstSet)) continue;
				firstSets.get(variable).addAll(firstSet);
				changed = true;
			}
			
		} while (changed);
		firstSets.put(StartRule, firstSets.get(startVariable));
	}

	private void computeFollowSets() {
		fallowSets = new LinkedHashMap<>() {
			private static final long serialVersionUID = 1L;
			public String toString() { return keySet().stream().map(s-> s + ": " + get(s)).collect(joining("\n")); }
		};
		fallowSets.put(StartRule, Utility.set(EndToken));
		for (String variable: variables) fallowSets.put(variable, new LinkedHashSet<>());
		for (;;) {
			boolean changed = false;
			for (String variable: variables) {
				for (Rule rule: this) {
					for (int i=0; i<rule.rhs.length; i+=1) {
						if (!rule.rhs[i].equals(variable)) continue;
						Set<String> first;
						if (i == rule.rhs.length - 1) {
							first = fallowSets.get(rule.lhs);
						}
						else {
							first = computeFirst(rule.rhs, i + 1);
							if (first.contains(Epsilon)) {
								first.remove(Epsilon);
								first.addAll(fallowSets.get(rule.lhs));
							}
						}
						if (fallowSets.get(variable).containsAll(first)) continue;
						fallowSets.get(variable).addAll(first);
						changed = true;
					}
				}
			}
			if (!changed) break;
		}
	}

	public Set<String> computeFirst(String[] rhs, int i) {
		Set<String> first = new LinkedHashSet<>();
		if (i == rhs.length) return first;

		String symbol = rhs[i];
		if (isTerminal(symbol) || symbol.equals(Epsilon)) {
			first.add(symbol);
			return first;
		}

		if (isVariable(symbol)) {
			for (String terminal: firstSets.get(symbol)) first.add(terminal);
		}
		if (first.contains(Epsilon)) {
			if (i != rhs.length - 1) {
				first.remove(Epsilon);
				first.addAll(computeFirst(rhs, i + 1));
			}
		}
		return first;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 37 * hash + Objects.hashCode(this);
		hash = 37 * hash + Objects.hashCode(terminals);
		hash = 37 * hash + Objects.hashCode(variables);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		final Grammar other = (Grammar) obj;
		if (!this.equals(other)) return false;
		if (!terminals.equals(other.terminals)) return false;
		if (!variables.equals(other.variables)) return false;
		return true;
	}

	@Override
	public String toString() {
		return range(0, size()).mapToObj(i-> i + ") " + get(i)).collect(joining("\n"));
	}
}
