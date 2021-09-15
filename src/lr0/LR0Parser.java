package lr0;

import static util.Grammar.EndToken;
import static util.Utility.set;

import java.util.LinkedHashSet;

import util.Grammar;
import util.LRParser;

public class LR0Parser extends LRParser<LR0State, LR0Item> {

	public LR0Parser(Grammar grammar) {
		super(grammar);
	}

	public boolean parserLR0() {
		createStates(LR0State::new, new LR0State(grammar, set(new LR0Item(grammar.get(0)))));
		var terminals = new LinkedHashSet<>(grammar.getTerminals());
		terminals.add(EndToken);
		return createActionGoToTable(i-> terminals);
	}
	
	public boolean parserSLR1() {
		createStates(LR0State::new, new LR0State(grammar, set(new LR0Item(grammar.get(0)))));
		return createActionGoToTable(i-> grammar.getFallowSets().get(i.getLhs()));
	}
}
