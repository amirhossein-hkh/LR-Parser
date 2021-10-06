package util;

import static java.lang.Math.log10;
import static java.lang.Math.max;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;
import static util.Grammar.EndToken;
import static util.Grammar.StartRule;
import static util.LRParser.ActionType.Accept;
import static util.LRParser.ActionType.Reduce;
import static util.LRParser.ActionType.Shift;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Function;

import lr0.LR0Item;

public abstract class LRParser<S extends State, I extends LR0Item> {

	protected Grammar grammar;
	protected StateList statesList = new StateList();
	protected ActionGoToTable actionGoToTable = new ActionGoToTable();

	public enum ActionType {
		Accept("a"), Shift("s"), Reduce("r");
		String sigla;
		ActionType(String sigla) {
			this.sigla = sigla;
		}
	}

	public record Action(ActionType type, int operand) {
		@Override public String toString() {
			return type.sigla + (type == Accept ? "" : operand);
		}
	}

	public LRParser(Grammar grammar) {
		this.grammar = grammar;
	}

	public Grammar getGrammar() {
		return grammar;
	}
	
	public List<S> getStatesList() {
		return statesList;
	}
	
	public ActionGoToTable getActionGoToTable() {
		return actionGoToTable;
	}

	protected void createStates(BiFunction<Grammar, Set<I>, S> newState, S initialState) {
		statesList.add(initialState);
		for (int i=0; i<statesList.size(); i+=1) {
			var statei = statesList.get(i);
			Set<I> itemsi = statei.items;
			for (String symbol: itemsi.stream().map(it-> it.getSymbol()).filter(s-> s!=null).collect(toSet())) {
				var nextState = newState.apply(grammar,
					itemsi.stream().filter(it-> symbol.equals(it.getSymbol())).map(I::<I>toNextSymbol).collect(toSet())
				);
				if (statesList.stream().filter(st-> st.items.equals(nextState.items)).map(st-> statei.transitions.put(symbol, st)).count() > 0) continue;
				statei.transitions.put(symbol, nextState);
				statesList.add(nextState);
			}
		}
	}
	
	protected boolean createActionGoToTable(Function<I, Set<String>> f) {
		log.setLength(0);
		for (int i=0; i<statesList.size(); i+=1) {
			var transitions = statesList.get(i).transitions;
			for (var symbol: (Set<String>) transitions.keySet()) {
				int state = statesList.indexOf(transitions.get(symbol));
				actionGoToTable.put(i, symbol, grammar.isVariable(symbol) ? state : new Action(Shift, state)); 
			}
		}
		for (int i=0; i<statesList.size(); i+=1) {
			for (var item: (Set<I>) statesList.get(i).items) {
				if (!item.atEnd()) continue;
				if (item.lhs.equals(StartRule)) {
					actionGoToTable.put(i, EndToken, new Action(Accept, 0));
					continue;
				}
				for (var terminal: f.apply(item)) {
					switch (actionGoToTable.get(i, terminal)) {
						case Action a-> log.append(a.type() + "-" + Reduce  + " conflict in state " + i + " at terminal " + terminal + "\n");
						case null, default-> actionGoToTable.put(i, terminal, new Action(Reduce, grammar.indexOf(new Rule(item))));
					}
				}
			}
		}
		return log.isEmpty();
	}
	
	private StringBuilder log = new StringBuilder();
	public String getLog() {
		return log.toString();
	}

	public boolean accept(String line) {
		log.setLength(0);
		String[] tokens = (line.trim() + " " + EndToken).split("\\s+");
		Stack<String> symbols = new Stack<String>() {
			private static final long serialVersionUID = 1L;
			@Override public String toString() { return join("", this); }
		};
		Stack<Integer> states = new Stack<>() {
			private static final long serialVersionUID = 1L;
			@Override public String toString() { return join(",", this); }
		};
		
		int sSize = max(2, 1 + (int) log10(statesList.size()));
		int rSize = max(5, grammar.stream().mapToInt(this::size).max().getAsInt());
		int tSize = max(2, grammar.getTerminals().stream().mapToInt(String::length).max().getAsInt());
		int aSize = stream(ActionType.values()).mapToInt(at-> at.name().length()).max().getAsInt() + sSize + 1;
		
		String format1 = format("%%%dd %%-%ds ", sSize, tSize);
		String format2 = format("%%-%ds - %%%dd: %%-%ds | %%-8s | %%s\n", aSize, sSize, rSize);

		String token = "";
		int index = -1, state = -1;
		Action action = new Action(Shift, 0);
		log.append(format(format("%%%ds %%-%ds %%-%ds - %%%ds: %%-%ds | %%-8s | %%s\n\n", sSize, tSize, aSize, sSize, rSize), "st", "tk", "action", "ns", "tk/rl", "symbols", "states"));
		log.append(" ".repeat(sSize + tSize + 2));
		loop: do {
			switch (action.type) {
				case Accept:
					log.append("Accepted.");
					return true;
	
				case Shift:
					symbols.push(token);
					states.push(state = action.operand);
					log.append(format(format2, action.type, state, token=tokens[index += 1], symbols, states));
					if (grammar.isVariable(token)) break loop;
					break;
	
				case Reduce:
					Rule rule = grammar.get(action.operand);
					for (int i=0; i<rule.rhs.length; i+=1) { symbols.pop(); states.pop(); }
					String lhs = rule.lhs;
					symbols.push(lhs);
					states.push(state = actionGoToTable.get(states.peek(), lhs));
					log.append(format(format2, action.type + " " + action.operand, state, reduce(rule), symbols, states));
			}
			log.append(format(format1, state, token));
			action = actionGoToTable.get(state, token);
		}
		while (index < tokens.length && action != null);
		log.append("Rejected.");
		return false;
	}
	
	public String join(String del, Stack stack) {
		var str = ""; for (int i=stack.size()-1, e=i-6; i>=0; i-=1) {
			if (i==e && i>0) return "\u2026" + del + str; 
			str = stack.get(i) + (str.length() == 0 ? "" : del) + str; 
		}
		return str;
	}
	
	private String reduce(Rule r) {
		return String.join("", r.rhs) + "->" + r.lhs;
	}
	
	private int size(Rule r) {
		return stream(r.rhs).mapToInt(String::length).sum() + 2 + r.lhs.length();
	}
	
	public static <T> Set<T> set(T ... objects) {
		Set<T> set = new LinkedHashSet();
		for (T o: objects) set.add(o);
		return set;
	}

	public class StateList extends ArrayList<S> {
		private static final long serialVersionUID = 1L;
		@Override public String toString() {
			return range(0, size()).mapToObj(i-> "State " + i + "\n" + get(i)).collect(joining("\n\n"));
		}
	}
	
	public class ActionGoToTable extends LinkedHashMap<String, Object> {
		private static final long serialVersionUID = 1L;
		public <T> T put(int state, String symbol, T t) {
			return (T) put(state + '\0' + symbol, t);
		}
		public <T> T get(int state, String symbol) {
			return (T) get(state + '\0' + symbol);
		}
		private int value(int state, String symbol) {
			return switch (get(state, symbol)) { case null, default-> 0; case Integer i-> i; case Action a-> a.operand; };
		}
		@Override
		public String toString() {
			return toString(3);
		}
		public String toString(int table) {
			boolean goTo = (table & 1) > 0; 
			boolean access = (table & 2) > 0; 
			var variables = grammar.getVariables();
			var terminals = grammar.getTerminals();
			int sSize = 1 + (int) log10(statesList.size());			
			Map<String, Integer> cSize = new LinkedHashMap<>();
			Set<String> symbols = new LinkedHashSet<>(terminals);
			symbols.addAll(variables);
			for (String symbol: symbols) {
				var max = range(0,statesList.size()).map(i-> value(i,symbol)).max().getAsInt();
				cSize.put(symbol, max(symbol.length(), (grammar.isTerminal(symbol) ? 2 : 1) + (int) log10(max)));
			}

			String str = " ".repeat(sSize+1);
			if (access) for (String terminal: terminals) str += format("%-"+ cSize.get(terminal) + "s ", terminal);
			if (goTo) for (String variable: variables) str += format("%"+ cSize.get(variable) + "s ", variable);
			str += "\n";

			String brd = " ".repeat(sSize) + "+";
			if (access) brd += "-".repeat(terminals.stream().mapToInt(t-> cSize.get(t)+1).sum()-1) + "+";
			if (goTo) brd += "-".repeat(variables.stream().mapToInt(v-> cSize.get(v)+1).sum()-1) + "+";
			str += brd + "\n";

			for (int i=0; i<statesList.size(); i+=1) {
				str += format("%" + (sSize) + "s|", i);
				if (access) for (String terminal: terminals) {
					Action action = get(i, terminal);
					str += format("%-" + cSize.get(terminal) + "s|", action == null ? "" : action);
				}
				if (goTo) for (String variable: variables) {
					Integer state = get(i, variable);
					str += format("%"+ cSize.get(variable) + "s|", state == null ? "" : state);
				}
				str += "\n";
			}

			return str += brd + "\n";
		}
	}
}
