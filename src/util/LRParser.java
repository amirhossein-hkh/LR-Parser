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
		@Override
		public String toString() {
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
				boolean exist = false;
				var nextState = newState.apply(grammar,
					itemsi.stream().filter(it-> symbol.equals(it.getSymbol())).map(I::<I>toNextSymbol).collect(toSet())
				);
				for (int j=0; j<statesList.size(); j+=1) {
					var statej = statesList.get(j);
					if (!statej.items.equals(nextState.items)) continue;
					statei.transitions.put(symbol, statej);
					exist = true;
				}
				if (exist) continue;
				statesList.add(nextState);
				statei.transitions.put(symbol, nextState);
			}
		}
	}
	
	protected boolean createActionGoToTable(Function<I, Set<String>> f) {
		for (int i=0; i<statesList.size(); i+=1) {
			var transitions = statesList.get(i).transitions;
			for (String symbol: (Set<String>) transitions.keySet()) {
				if (grammar.isVariable(symbol))
					actionGoToTable.put(i, symbol, statesList.indexOf(transitions.get(symbol)));
				else
					actionGoToTable.put(i, symbol, new Action(Shift, statesList.indexOf(transitions.get(symbol))));
			}
		}
		for (int i=0; i<statesList.size(); i+=1) {
			for (var item: (Set<I>) statesList.get(i).items) {
				if (!item.atEnd()) continue;
				if (item.lhs.equals(StartRule)) {
					actionGoToTable.put(i, EndToken, new Action(Accept, 0));
					continue;
				}
				for (String terminal: f.apply(item)) {
					Action action = actionGoToTable.get(i, terminal);
					if (action != null) {
						System.out.println("it has a " + Reduce + "-" + action.type() + " confilct in state " + i);
						return false;
					}
					actionGoToTable.put(i, terminal, new Action(Reduce, grammar.indexOf(new Rule(item))));
				}
			}
		}
		return true;
	}
	
	private StringBuilder log = new StringBuilder();
	public String getLog() {
		return log.toString();
	}

	public boolean accept(List<String> tokens) {
		log.setLength(0);
		tokens.add(EndToken);
		Stack<String> symbols = new Stack<String>() {
			private static final long serialVersionUID = 1L;
			public String toString() { // return join("", this); } /*
				return join("", this);
			} //*/ 
		};
		Stack<Integer> states = new Stack<>() {
			private static final long serialVersionUID = 1L;
			public String toString() { // return stream().map(i-> i.toString()).collect(joining(",")); } /*
				return join(",", this);
			} //*/
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
		do {
			switch (action.type) {
				case Accept:
					log.append("Accepted.");
					return true;
	
				case Shift:
					symbols.push(token);
					states.push(state = action.operand);
					log.append(format(format2, "Shift", state, token=tokens.get(index += 1), symbols, states));
					break;
	
				case Reduce:
					Rule rule = grammar.get(action.operand);
					for (int i=0; i<rule.rhs.length; i+=1) { symbols.pop(); states.pop(); }
					String lhs = rule.lhs;
					symbols.push(lhs);
					states.push(state = actionGoToTable.get(states.peek(), lhs));
					log.append(format(format2, "Reduce " + action.operand, state, reduce(rule), symbols, states));
			}
			log.append(format(format1, state, token));
			action = actionGoToTable.get(state, token);
		}
		while (index < tokens.size() && action != null);
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
	
	public class StateList extends ArrayList<S> {
		private static final long serialVersionUID = 1L;
		public String toString() {
			return range(0, size()).mapToObj(i->"State " + i + ": \n" + get(i)).collect(joining("\n\n"));
		}
	}
	
	public class ActionGoToTable extends LinkedHashMap<String, Object> {
		private static final long serialVersionUID = 1L;
		public <T> T put(int state, String token, T t) {
			return (T) put(state + '\0' + token, t);
		}
		public <T> T get(int state, String token) {
			return (T) get(state + '\0' + token);
		}
		@Override
		public String toString() {
			return toString(3);
		}
		public String toString(int t) {
			boolean goTo = (t & 1) > 0; 
			boolean access = (t & 2) > 0; 
			var variables = grammar.getVariables();
			var terminals = new LinkedHashSet<>(grammar.getTerminals());
			terminals.add(EndToken);
			int sSize = 2 + (int) log10(actionGoToTable.size());
			int vSize = variables.stream().mapToInt(s->s.length()).max().getAsInt();
			int tSize = terminals.stream().mapToInt(s->s.length()).max().getAsInt();
			String str = "";

			str += " ".repeat(sSize);
			if (access) for (String terminal: terminals) str += format("%-"+ (max(tSize, sSize)+1) + "s", terminal);
			//if (access && goTo) str += " ";
			if (goTo) for (String variable: variables) str += format("%"+ max(vSize, sSize-1) + "s ", variable);
			str += "\n";

			String brd = " ".repeat(sSize-1);
			if (access) brd += "+" + "-".repeat(terminals.size()*(max(tSize, sSize)+1)-1) + "+";
			if (goTo) brd += (access ? "" : "+") + "-".repeat(variables.size()*max(vSize, sSize)-1) + "+";
			str += brd + "\n";

			for (int i=0; i<statesList.size(); i+=1) {
				str += format("%" + (sSize-1) + "s|", i);
				if (access) for (String terminal: terminals) {
					Action action = get(i, terminal);
					str += format("%-" + max(tSize, sSize) + "s|", action == null ? "" : action);
				}
				//if (access && goTo) str += "|";
				if (goTo) for (String variable: variables) {
					Integer state = get(i, variable);
					str += format("%"+ max(vSize, sSize-1) + "s|", state == null ? "" : state);
				}
				str += "\n";
			}

			return str += brd + "\n";
		}
	}
}
