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

package javaff.data.temporal;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javaff.data.PDDLPrintable;
import javaff.data.PDDLPrinter;
import javaff.planning.MetricState;

public class DurationConstraint implements PDDLPrintable {
	Set constraints = new HashSet();

	public boolean staticDuration() {
		boolean rTest = true;
		for (Iterator cit = constraints.iterator(); cit.hasNext() && rTest;) {
			SimpleDurationConstraint c = (SimpleDurationConstraint) cit.next();
			rTest = c.staticDuration();
		}
		return rTest;
	}

	public void add(SimpleDurationConstraint c) {
		constraints.add(c);
	}

	public DurationConstraint ground(Map varMap) {
		DurationConstraint dc = new DurationConstraint();
		for (Iterator cit = constraints.iterator(); cit.hasNext();) {
			SimpleDurationConstraint c = (SimpleDurationConstraint) cit.next();
			dc.add((SimpleDurationConstraint) c.ground(varMap));
		}
		return dc;
	}

	public BigDecimal getDuration(MetricState s) {
		return getMaxDuration(s);
	}

	public BigDecimal getMaxDuration(MetricState s) {
		BigDecimal sofar = javaff.JavaFF.maxDuration;
		Iterator cit = constraints.iterator();
		while (cit.hasNext()) {
			SimpleDurationConstraint c = (SimpleDurationConstraint) cit.next();
			BigDecimal ndec = c.getMaxDuration(s);
			sofar = sofar.min(ndec);
		}
		return sofar;
	}

	public BigDecimal getMinDuration(MetricState s) {
		BigDecimal sofar = new BigDecimal(0);
		Iterator cit = constraints.iterator();
		while (cit.hasNext()) {
			SimpleDurationConstraint c = (SimpleDurationConstraint) cit.next();
			BigDecimal ndec = c.getMinDuration(s);
			sofar = sofar.max(ndec);
		}
		return sofar;
	}

	public void pddlPrint(PrintStream s, int indent) {
		s.println("(and ");
		Iterator cit = constraints.iterator();
		while (cit.hasNext()) {
			SimpleDurationConstraint c = (SimpleDurationConstraint) cit.next();
			PDDLPrinter.printToString(c, s, false, false, indent);
		}
		s.print(")");
	}

	public String toString() {
		String str = "(and ";
		for (Iterator cit = constraints.iterator(); cit.hasNext();) {
			SimpleDurationConstraint c = (SimpleDurationConstraint) cit.next();
			str += c.toString();
		}
		return str += ")";
	}

	public String toStringTyped() {
		String str = "(and ";
		for (Iterator cit = constraints.iterator(); cit.hasNext();) {
			SimpleDurationConstraint c = (SimpleDurationConstraint) cit.next();
			str += c.toStringTyped();
		}
		return str += ")";
	}

	public int hashCode() {
		int hash = 31 * 7 ^ constraints.hashCode();
		return hash;
	}

	public boolean equals(Object o) {
		if (o instanceof DurationConstraint) {
			DurationConstraint c = (DurationConstraint) o;
			return c.constraints.equals(constraints);
		} else
			return false;
	}
}
