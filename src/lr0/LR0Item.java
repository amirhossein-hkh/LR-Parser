package lr0;

import static util.Grammar.Epsilon;

import util.Rule;

public class LR0Item extends Rule {

	protected int dot;

	public LR0Item(Rule rule) {
		super(rule);
		var rhs = rule.getRhs(); 
		dot = rhs.length == 1 && rhs[0].equals(Epsilon) ? 1 : 0;
	}

	public LR0Item(LR0Item item) {
		super(item);
		dot = item.dot;
	}

	public int getDot() {
		return dot;
	}

	public boolean atEnd() {
		return dot == rhs.length;
	}

	public boolean lastSymbol() {
		return dot == rhs.length - 1;
	}

	public String getSymbol() {
		return atEnd() ? null : rhs[dot];
	}

	public <T extends LR0Item> T toNextSymbol() {
		return (T) new LR0Item(this).advanceDot();
	}

	protected <T extends LR0Item> T advanceDot() {
		if (!atEnd()) dot += 1;
		return (T) this;
	}

	@Override
	public int hashCode() {
		int hash = super.hashCode();
		return 31 * hash + dot;
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj)
			&& dot == ((LR0Item) obj).dot
		;
	}

	@Override
	public String toString() {
		String str = lhs + " -> ";
		for (int i=0; i<rhs.length; i+=1) {
			if (i == dot) str += ". ";
			str += rhs[i];
			if (i != rhs.length - 1) str += " ";
		}
		if (rhs.length == dot) str += " .";
		return str;
	}
}
