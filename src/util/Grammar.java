package util;

import static java.lang.String.join;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.IntStream.range;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
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
		
	CLR(1), LALR(1)	
		S -> a A a
		S -> b A b
		A -> a
		A -> a a
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
				if (rule == 0) add(new Rule(StartRule, startVariable = lhs));
				add(new Rule(lhs, tokens));
				rule += 1;
			}
		}
		terminals.removeAll(variables);
		terminals.add(EndToken);
		computeFirstSets();
		computeFollowSets();
	}

	public Set<Rule> getRules(String variable) {
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
			@Override public String toString() { return keySet().stream().map(s-> s + ": " + join(" ", get(s))).collect(joining("\n")); }
		};
		firstSets.put(StartRule, null);
		for (String variable: variables) firstSets.put(variable, new LinkedHashSet<>());
		boolean changed; do {
			changed = false;
			for (String variable: variables) {
				Set<String> firstSet = new LinkedHashSet<>();
				for (Rule rule: this) {
					if (!rule.lhs.equals(variable)) continue;
					firstSet.addAll(computeFirstSet(rule.rhs, 0));
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
			@Override public String toString() { return keySet().stream().map(s-> s + ": " + join(" ", get(s))).collect(joining("\n")); }
		};
		fallowSets.put(StartRule, LRParser.set(EndToken));
		for (String variable: variables) fallowSets.put(variable, new LinkedHashSet<>());
		boolean changed; do {
			changed = false;
			for (String variable: variables) {
				for (Rule rule: this) {
					for (int i=0; i<rule.rhs.length; i+=1) {
						if (!rule.rhs[i].equals(variable)) continue;
						Set<String> firstSet;
						if (i == rule.rhs.length - 1) {
							firstSet = fallowSets.get(rule.lhs);
						}
						else {
							firstSet = computeFirstSet(rule.rhs, i + 1);
							if (firstSet.contains(Epsilon)) {
								firstSet.remove(Epsilon);
								firstSet.addAll(fallowSets.get(rule.lhs));
							}
						}
						if (fallowSets.get(variable).containsAll(firstSet)) continue;
						fallowSets.get(variable).addAll(firstSet);
						changed = true;
					}
				}
			}
		} while (changed);
	}

	public Set<String> computeFirstSet(String[] rhs, int i) {
		Set<String> firstSet = new LinkedHashSet<>();
		if (i == rhs.length) return firstSet;

		String symbol = rhs[i];
		if (isTerminal(symbol) || symbol.equals(Epsilon)) {
			firstSet.add(symbol);
			return firstSet;
		}

		if (isVariable(symbol)) {
			for (String terminal: firstSets.get(symbol)) firstSet.add(terminal);
		}
		if (firstSet.contains(Epsilon)) {
			if (i != rhs.length - 1) {
				firstSet.remove(Epsilon);
				firstSet.addAll(computeFirstSet(rhs, i + 1));
			}
		}
		return firstSet;
	}

	@Override
	public String toString() {
		return range(0, size()).mapToObj(i-> i + ": " + get(i)).collect(joining("\n"));
	}
}
