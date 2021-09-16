package lr0;

import static java.util.stream.Collectors.toSet;

import java.util.LinkedHashSet;
import java.util.Set;

import util.Grammar;
import util.State;

public class LR0State extends State<LR0State, LR0Item> {

	public LR0State(Grammar grammar, Set<LR0Item> items) {
		this.items = new LinkedHashSet<>(items);
		closure(grammar);
	}

	private void closure(Grammar grammar) {
		for (;;) {
			Set<LR0Item> newItems = items.stream()
				.map(i-> i.getSymbol())
				.filter(s-> s!=null && grammar.isVariable(s))
				.flatMap(s-> grammar.getRulesByLhs(s).stream())
				.map(LR0Item::new)
				.collect(toSet())
			;
			if (items.containsAll(newItems)) break;
			items.addAll(newItems);
		}
	}
}
