package lr1;

import static util.Grammar.Epsilon;

import java.util.LinkedHashSet;
import java.util.Set;

import util.Grammar;
import util.Rule;
import util.State;

public class LR1State extends State<LR1State, LR1Item> {

	/*
	class SetLR1Item extends LinkedHashSet<LR1Item> {
		private static final long serialVersionUID = 1L;
		public SetLR1Item(Collection<? extends LR1Item> c) {
			super(c);
		}
		public boolean equalsLR0(Set<LR1Item> itemsj) {
	        if (this == itemsj) return true;
	        if (size() != itemsj.size()) return false;
	        itemsj = new LinkedHashSet(itemsj);
	        loop: for (LR1Item itemi: this) {
	        	for (LR1Item item2: itemsj) if (itemi.equalsLR0(item2)) {
	        		itemsj.remove(item2);
	        		continue loop;
	        	}
	         	return false;
	    	}
	        return true;
		}
	}
	//*/

	public LR1State(Grammar grammar, Set<LR1Item> items){
		this.items = items; //new LinkedHashSet<>(items); // make a copy!
		//this.items = new SetLR1Item(items); // make a copy!
		closure(grammar);
	}

	private void closure(Grammar grammar) {
		boolean changed; do {
			changed = false;
			for (LR1Item item1: items) {
				if (item1.atEnd() || grammar.isTerminal(item1.getSymbol())) continue;
				Set<String> newLA = newLookahead(grammar, item1);
				for (Rule rule: grammar.getRules(item1.getSymbol())) {
					var newItem = new LR1Item(rule, null);
					// merge lookaheads with existing item
					LR1Item item2 = items.stream().filter(it-> it.equalsLR0(newItem)).findFirst().orElse(null);
					if (item2 != null) {
						var item2LA = item2.lookahead;
						if (item2LA.containsAll(newLA)) continue;
						// changing the lookahead will change the hash code of the item,
						// which means it must be re-added.
						items.remove(item2);
						item2LA.addAll(newLA);
						items.add(item2);
						changed = true;
						continue;
					}
					newItem.lookahead = new LinkedHashSet<>(newLA); // make a copy!
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
			Set<String> firstSet = grammar.computeFirstSet(item.getRhs(), item.getDot() + 1);
			if (firstSet.contains(Epsilon)) {
				firstSet.remove(Epsilon);
				firstSet.addAll(item.lookahead);
			}
			newLA.addAll(firstSet);
		}
		return newLA;
	}
}
