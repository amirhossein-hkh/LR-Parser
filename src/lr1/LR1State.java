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
			for (LR1Item item: items) {
				if (item.getDot() == item.getRhs().length || !grammar.isVariable(item.getSymbol())) continue;
				Set<String> lookahead = new LinkedHashSet<>();
				if (item.getDot() == item.getRhs().length - 1) {
					lookahead.addAll(item.lookahead);
				}
				else {
					Set<String> firstSet = grammar.computeFirst(item.getRhs(), item.getDot() + 1);
					if (firstSet.contains(Epsilon)) {
						firstSet.remove(Epsilon);
						firstSet.addAll(item.lookahead);
					}
					lookahead.addAll(firstSet);
				}
				rule: for (Rule rule: grammar.getRulesByLhs(item.getSymbol())) {
					var newItemLA = new LinkedHashSet<>(lookahead);
					var newItem = new LR1Item(rule, newItemLA);
					// merge lookaheads with existing item
					for (LR1Item existingItem: items) {
						if (!existingItem.equalLR0(newItem)) continue;
						var existingItemtLA = existingItem.lookahead;
						if (existingItemtLA.containsAll(newItemLA)) continue rule;
						// changing the lookahead will change the hash code of the item,
						// which means it must be re-added.
						items.remove(existingItem);
						existingItemtLA.addAll(newItemLA);
						items.add(existingItem);
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
}
