package lr1;

import static util.Grammar.Epsilon;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import util.Grammar;
import util.Rule;
import util.State;

public class LR1State extends State<LR1State, LR1Item> {

	public LR1State(Grammar grammar, Set<LR1Item> items){
		this.items = new LinkedHashSet<>(items);
		transitions = new LinkedHashMap<>();
		closure(grammar);
	}

	private void closure(Grammar grammar) {
		boolean changed;
		do {
			changed = false;
			for (LR1Item item1: items) {
				if (item1.atEnd() || grammar.isTerminal(item1.getSymbol())) continue;
				Set<String> newLA = newLookahead(grammar, item1);
				rule: for (Rule rule: grammar.getRulesByLhs(item1.getSymbol())) {
					var newItemLA = new LinkedHashSet<>(newLA); // make a copy!
					var newItem = new LR1Item(rule, newItemLA);
					// merge lookaheads with existing item
					for (LR1Item item2: items) {
						if (!item2.equalLR0(newItem)) continue;
						var item2LA = item2.lookahead;
						if (item2LA.containsAll(newItemLA)) continue rule;
						// changing the lookahead will change the hash code of the item,
						// which means it must be re-added.
						items.remove(item2);
						item2LA.addAll(newItemLA);
						items.add(item2);
						changed = true;
						continue rule;
					}
					items.add(newItem);
					changed = true;
				}
				if (changed) break;
			}
		} while (changed);
	}

	private Set<String> newLookahead(Grammar grammar, LR1Item item) {
		Set<String> newLA = new LinkedHashSet<>();
		if (item.lastSymbol()) {
			newLA.addAll(item.lookahead);
		}
		else {
			Set<String> firstSet = grammar.computeFirst(item.getRhs(), item.getDot() + 1);
			if (firstSet.contains(Epsilon)) {
				firstSet.remove(Epsilon);
				firstSet.addAll(item.lookahead);
			}
			newLA.addAll(firstSet);
		}
		return newLA;
	}
}
