package lr1;

import static java.util.stream.Collectors.toSet;
import static util.Grammar.EndToken;
import static util.Utility.set;

import lr0.LR0Item;
import util.Grammar;
import util.LRParser;

public class LR1Parser extends LRParser<LR1State, LR1Item> {

	public LR1Parser(Grammar grammar){
		super(grammar);
	}

	public boolean parseCLR1() {
		createStates(LR1State::new, new LR1State(grammar, set(new LR1Item(grammar.getRule(0), set(EndToken)))));
		createGoToTable();
		return createActionTable(i-> ((LR1Item) i).lookahead);
	}

	public boolean parseLALR1() {
		createStates(LR1State::new, new LR1State(grammar, set(new LR1Item(grammar.getRule(0), set(EndToken)))));
		createStatesForLALR1();
		createGoToTable();
		return createActionTable(i-> ((LR1Item) i).lookahead);
	}

	private void createStatesForLALR1(){
		StateList newStateList = new StateList();
		for (int i=0; i<statesList.size(); i+=1) {
			var statei = statesList.get(i);
			var itemsi = statei.getItems();
			var lr0Itemsi = itemsi.stream().map(LR0Item::new).collect(toSet());
			for (int j=i+1; j<statesList.size(); j+=1) {
				var itemsj = statesList.get(j).getItems();
				var lr0Itemsj = itemsj.stream().map(LR0Item::new).collect(toSet());
				if (!lr0Itemsi.equals(lr0Itemsj)) continue;
				for (var itemi: itemsi) {
					for (var itemj: itemsj) {
						if (!itemi.equalLR0(itemj)) continue;
						itemi.lookahead.addAll(itemj.lookahead);
						break;
					}
				}
				for (int k=0; k<statesList.size(); k+=1) {
					var transitions = statesList.get(k).getTransitions();
					for (var e: transitions.entrySet()) {
						if (!e.getValue().getItems().equals(itemsj)) continue;
						transitions.put(e.getKey(), statei);
					}
				}
				statesList.remove(j);
				j -= 1;
			}
			newStateList.add(statesList.get(i));
		}
		statesList = newStateList;
	}
}
