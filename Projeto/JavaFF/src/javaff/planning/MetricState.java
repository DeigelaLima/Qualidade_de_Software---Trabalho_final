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

package javaff.planning;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javaff.data.GroundCondition;
import javaff.data.Metric;
import javaff.data.TotalOrderPlan;
import javaff.data.metric.NamedFunction;

public class MetricState extends STRIPSState {
	public Map funcValues; // maps Named Functions onto BigDecimals
	public Metric metric;

	public MetricState(Set a, Set f, GroundCondition g, Map funcs, Metric m) {
		super(a, f, g);
		funcValues = funcs;
		metric = m;
	}

	protected MetricState(Set a, Set f, GroundCondition g, Map funcs, TotalOrderPlan p, Metric m) {
		super(a, f, g, p);
		funcValues = funcs;
		metric = m;
	}

	public Object clone() {
		Set nf = (Set) ((HashSet) facts).clone();
		TotalOrderPlan p = (TotalOrderPlan) plan.clone();
		Map nfuncs = (Map) ((Hashtable) funcValues).clone();
		MetricState ms = new MetricState(actions, nf, goal, nfuncs, p, metric);
		ms.setRPG(RPG);
		// ms.setFilter(filter);
		return ms;
	}

	public BigDecimal getValue(NamedFunction f) {
		return (BigDecimal) funcValues.get(f);
	}

	public void setValue(NamedFunction f, BigDecimal d) {
		funcValues.put(f, d);
	}

	// WARNING - not yet implemented - must be overridden and take account of the
	// metric
	public BigDecimal getHValue() {
		return super.getHValue();
	}

	public BigDecimal getGValue() {
		return super.getGValue();
	}

	public boolean equals(Object o) {
		if (o instanceof MetricState) {
			MetricState s = (MetricState) o;
			return (s.facts.equals(facts) && s.funcValues.equals(funcValues));
		} else
			return false;
	}

	public int hashCode() {
		int hash = 31 * 8 ^ facts.hashCode();
		return hash = 31 * hash ^ funcValues.hashCode();
	}
}
