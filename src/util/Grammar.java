package util;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.IntStream.range;
import static util.Utility.set;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/*
	LR(0)
	
		S -> A B
		A -> a
		B -> A b
		
		a a b

	SRL(1), CLR(1), LALR(1)
	
		E -> T + E | T
		T -> F * T | F
		F -> ( E ) | id
		
		id + id * ( id + id )
*/

public class Grammar /* TODO extends ArrayList<Rule>? */ {

	private List<Rule> rules;
	private Set<String> terminals;
	private Set<String> variables;
	private String startVariable;
	private Map<String, Set<String>> firstSets;
	private Map<String, Set<String>> fallowSets;

	public static final String StartRule = "S'";
	public static final String EndToken = "$";
	public static final String Epsilon = "epsilon"; // TODO why not "\u03B5"

	public Grammar(String str) {
		rules = new ArrayList<>();
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
					rules.add(new Rule(StartRule, startVariable));
				}
				rules.add(new Rule(lhs, tokens));
				rule += 1;
			}
		}
		terminals.removeAll(variables);
		computeFirstSets();
		computeFollowSets();
	}

	public List<Rule> getRules() {
		return rules;
	}

	public Rule getRule(int i) {
		return rules.get(i);
	}

	public int indexOfRule(Rule rule){
		return rules.indexOf(rule);
	}

	public Set<Rule> getRulesByLhs(String variable) {
		return rules.stream().filter(r-> r.lhs.equals(variable)).collect(toCollection(LinkedHashSet::new));
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
				for (Rule rule: rules) {
					if (!rule.lhs.equals(variable)) continue;
					firstSet.addAll(computeFirst(rule.rhs, 0));
				}
				if (firstSets.get(variable).containsAll(firstSet)) continue;
				changed = true;
				firstSets.get(variable).addAll(firstSet);
			}
			
		} while (changed);
		firstSets.put(StartRule, firstSets.get(startVariable));
	}

	private void computeFollowSets() {
		fallowSets = new LinkedHashMap<>() {
			private static final long serialVersionUID = 1L;
			public String toString() { return keySet().stream().map(s-> s + ": " + get(s)).collect(joining("\n")); }
		};
		fallowSets.put(StartRule, set(EndToken));
		for (String variable: variables) fallowSets.put(variable, new LinkedHashSet<>());
		for (;;) {
			boolean changed = false;
			for (String variable: variables) {
				for (Rule rule: rules) {
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
						changed = true;
						fallowSets.get(variable).addAll(first);
					}
				}
			}
			if (!changed) break;
		}
	}

	public Set<String> computeFirst(String[] rhs, int index) {
		Set<String> first = new LinkedHashSet<>();
		if (index == rhs.length) return first;

		String symbol = rhs[index];
		if (isTerminal(symbol) || symbol.equals(Epsilon)) {
			first.add(symbol);
			return first;
		}

		if (isVariable(symbol)) {
			for (String terminal: firstSets.get(symbol)) first.add(terminal);
		}
		if (first.contains(Epsilon)) {
			if (index != rhs.length - 1) {
				first.remove(Epsilon);
				first.addAll(computeFirst(rhs, index + 1));
			}
		}
		return first;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 37 * hash + Objects.hashCode(rules);
		hash = 37 * hash + Objects.hashCode(terminals);
		hash = 37 * hash + Objects.hashCode(variables);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		final Grammar other = (Grammar) obj;
		if (!rules.equals(other.rules)) return false;
		if (!terminals.equals(other.terminals)) return false;
		if (!variables.equals(other.variables)) return false;
		return true;
	}

	@Override
	public String toString() {
		return range(0, rules.size()).mapToObj(i-> i + ") " + rules.get(i)).collect(joining("\n"));
	}
}
