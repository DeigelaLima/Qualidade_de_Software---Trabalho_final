/************************************************************************
 * Strathclyde Planning Group,
 * Department of Computer and Information Sciences,
 * University of Strathclyde, Glasgow, UK
 * http://planning.cis.strath.ac.uk/
 * 
 * Copyright 2007, Keith Halsey
 * Copyright 2008, Andrew Coles and Amanda Smith
 *
 * (Questions/bug reports now to be sent to Andrew Coles)
 *
 * This file is part of JavaFF.
 * 
 * JavaFF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * JavaFF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JavaFF.  If not, see <http://www.gnu.org/licenses/>.
 * 
 ************************************************************************/

package javaff.data.strips;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javaff.data.Condition;
import javaff.data.GroundCondition;
import javaff.data.GroundEffect;
import javaff.data.Literal;
import javaff.data.PDDLPrinter;
import javaff.data.UngroundCondition;
import javaff.data.UngroundEffect;
import javaff.planning.State;

public class AND implements GroundCondition, GroundEffect, UngroundCondition, UngroundEffect {
	protected Set literals = new HashSet(); // set of Literals

	public void add(Object o) {
		if (!(o instanceof AND))
			literals.add(o);
		else {
			final AND a = (AND) o;
			final Iterator ait = a.literals.iterator();
			while (ait.hasNext()) {
				add(ait.next());
			}
		}
	}

	public boolean isStatic() {
		for (final Iterator it = literals.iterator(); it.hasNext();) {
			final Condition c = (Condition) it.next();
			if (!c.isStatic()) {
				return false;
			}
		}
		return true;
	}

	public GroundCondition staticifyCondition(Map fValues) {
		final Set newlit = new HashSet(literals.size());
		for (final Iterator it = literals.iterator(); it.hasNext();) {
			final GroundCondition c = (GroundCondition) it.next();
			if (!(c instanceof TrueCondition))
				newlit.add(c.staticifyCondition(fValues));
		}
		literals = newlit;
		if (literals.isEmpty()) {
			return TrueCondition.getInstance();
		} else {
			return this;
		}
	}

	public GroundEffect staticifyEffect(Map fValues) {
		Set newlit = new HashSet(literals.size());
		for (Iterator it = literals.iterator(); it.hasNext();) {
			GroundEffect e = (GroundEffect) it.next();
			if (!(e instanceof NullEffect)) {
				newlit.add(e.staticifyEffect(fValues));
			}
		}
		literals = newlit;
		if (literals.isEmpty()) {
			return NullEffect.getInstance();
		} else {
			return this;
		}
	}

	public Set getStaticPredicates() {
		Set rSet = new HashSet();
		for (final Iterator it = literals.iterator(); it.hasNext();) {
			UngroundCondition c = (UngroundCondition) it.next();
			rSet.addAll(c.getStaticPredicates());
		}
		return rSet;
	}

	public boolean effects(PredicateSymbol s) {
		boolean rEff = false;
		final Iterator lit = literals.iterator();
		while (lit.hasNext() && !(rEff)) {
			UngroundEffect ue = (UngroundEffect) lit.next();
			rEff = ue.effects(s);
		}
		return rEff;
	}

	public UngroundCondition minus(UngroundEffect e) {
		final AND a = new AND();
		final Iterator lit = literals.iterator();
		while (lit.hasNext()) {
			UngroundCondition p = (UngroundCondition) lit.next();
			a.add(p.minus(e));
		}
		return a;
	}

	public UngroundCondition effectsAdd(UngroundCondition cond) {
		final Iterator lit = literals.iterator();
		UngroundCondition c = null;
		while (lit.hasNext()) {
			UngroundCondition d = ((UngroundEffect) lit.next()).effectsAdd(cond);
			if (!d.equals(cond))
				c = d;
		}
		return c == null ? cond : c;
	}

	public GroundEffect groundEffect(Map varMap) {
		AND a = new AND();
		for (final Iterator lit = literals.iterator(); lit.hasNext();) {
			UngroundEffect p = (UngroundEffect) lit.next();
			a.add(p.groundEffect(varMap));
		}
		return a;
	}

	public GroundCondition groundCondition(Map varMap) {
		final AND a = new AND();
		for (final Iterator lit = literals.iterator(); lit.hasNext();) {
			UngroundCondition p = (UngroundCondition) lit.next();
			a.add(p.groundCondition(varMap));
		}
		return a;
	}

	public boolean stateIsTrue(State s) {
		for (Iterator cit = literals.iterator(); cit.hasNext();) {
			GroundCondition c = (GroundCondition) cit.next();
			if (!c.stateIsTrue(s))
				return false;
		}
		return true;
	}

	public void apply(State s) {
		applyDels(s);
		applyAdds(s);
	}

	public void applyAdds(State s) {
		for (Iterator eit = literals.iterator(); eit.hasNext();) {
			GroundEffect e = (GroundEffect) eit.next();
			e.applyAdds(s);
		}
	}

	public void applyDels(State s) {
		for (Iterator eit = literals.iterator(); eit.hasNext();) {
			GroundEffect e = (GroundEffect) eit.next();
			e.applyDels(s);
		}
	}

	public Set getConditionalPropositions() {
		Set rSet = new HashSet();
		for (Iterator eit = literals.iterator(); eit.hasNext();) {
			GroundCondition e = (GroundCondition) eit.next();
			rSet.addAll(e.getConditionalPropositions());
		}
		return rSet;
	}

	public Set getAddPropositions() {
		Set rSet = new HashSet();
		for (Iterator eit = literals.iterator(); eit.hasNext();) {
			GroundEffect e = (GroundEffect) eit.next();
			rSet.addAll(e.getAddPropositions());
		}
		return rSet;
	}

	public Set getDeletePropositions() {
		Set rSet = new HashSet();
		for (Iterator eit = literals.iterator(); eit.hasNext();) {
			GroundEffect e = (GroundEffect) eit.next();
			rSet.addAll(e.getDeletePropositions());
		}
		return rSet;
	}

	public Set getOperators() {
		Set rSet = new HashSet();
		for (Iterator eit = literals.iterator(); eit.hasNext();) {
			GroundEffect e = (GroundEffect) eit.next();
			rSet.addAll(e.getOperators());
		}
		return rSet;
	}

	public Set getComparators() {
		Set rSet = new HashSet();
		for (Iterator eit = literals.iterator(); eit.hasNext();) {
			GroundCondition e = (GroundCondition) eit.next();
			rSet.addAll(e.getComparators());
		}
		return rSet;
	}

	public boolean equals(Object o) {
		if (o instanceof AND) {
			AND a = (AND) o;
			return (literals.equals(a.literals));
		} else
			return false;
	}

	public int hashCode() {
		return literals.hashCode();
	}

	public void pddlPrint(PrintStream s, int indent) {
		PDDLPrinter.printToString(literals, "and", s, false, true, indent);
	}

	public String toString() {
		String str = "(and";
		for (Iterator it = literals.iterator(); it.hasNext();) {
			str += " " + it.next();
		}
		return str += ")";
	}

	public String toStringTyped() {
		String str = "(and";
		for (Iterator it = literals.iterator(); it.hasNext();) {
			Literal l = (Literal) it.next();
			str += " " + l.toStringTyped();
		}
		return str += ")";
	}
}
