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

package javaff.data;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javaff.data.strips.InstantAction;
import javaff.data.strips.Proposition;
import javaff.data.temporal.SplitInstantAction;
import javaff.scheduling.TemporalConstraint;


public class PartialOrderPlan implements Plan
{
	public Map strictOrderings = new Hashtable();
	public Map equalOrderings = new Hashtable();
	public Set actions = new HashSet();

	public PartialOrderPlan()
	{
		
	}

	public void addStrictOrdering(Action first, Action second)
	{
		Set ord = null;
		Object o = strictOrderings.get(first);
		if (o != null)
			ord = (HashSet) o;
		else {
			ord = new HashSet();
			strictOrderings.put(first, ord);
		}
		ord.add(second);
		actions.add(first);
		actions.add(second);
	}

	public void addEqualOrdering(Action first, Action second)
	{
		Set ord = null;
		Object o = equalOrderings.get(first);
		if (o != null)
			ord = (HashSet) o;
		else {
			ord = new HashSet();
			equalOrderings.put(first, ord);
		}
		ord.add(second);
		actions.add(first);
		actions.add(second);
	}

	public void addOrder(Action first, Action second, Proposition p)
	{
		boolean condition = first instanceof SplitInstantAction && !((SplitInstantAction) first).exclusivelyInvariant(p);
		if (condition) {
			addEqualOrdering(first, second);
			return;
		}

		boolean condition1 = second instanceof SplitInstantAction && !((SplitInstantAction) second).exclusivelyInvariant(p);
		if (condition1) {
			addEqualOrdering(first, second);
			return;
		}
		
		addStrictOrdering(first, second);
		
		
	}

	public void addAction(Action a)
	{
		actions.add(a);
		strictOrderings.put(a, new HashSet());
		equalOrderings.put(a, new HashSet());
	}

	public void addActions(Set s)
	{
		for (Iterator sit = s.iterator(); sit.hasNext();)
			addAction((Action) sit.next());
	}

	public Set getActions()
	{
		return actions;
	}

	public Set getTemporalConstraints()
	{
		Set rSet = new HashSet();
		for (Iterator ait = actions.iterator(); ait.hasNext();) {
			Action a = (Action) ait.next();
			Set ss = (HashSet) strictOrderings.get(a);
			Iterator sit = ss.iterator();
			while (sit.hasNext()) {
				Action b = (Action) sit.next();
				rSet.add(TemporalConstraint.getConstraint((InstantAction) a, (InstantAction) b));
			}
			Set es = (HashSet) equalOrderings.get(a);
			Iterator eit = es.iterator();
			while (eit.hasNext()) {
				Action b = (Action) eit.next();
				rSet.add(TemporalConstraint.getConstraintEqual((InstantAction) a, (InstantAction) b));
			}
		}
		return rSet;
			
	}

	public void print(PrintStream s)
	{
		Iterator sit = actions.iterator();
		while (sit.hasNext())
		{
			Action a = (Action) sit.next();
			s.println(a);
			s.println("\tStrict Orderings: "+strictOrderings.get(a));
			s.println("\tLess than or equal Orderings: "+equalOrderings.get(a));
		}
	}

	public void print(PrintWriter w)
	{
		Iterator sit = actions.iterator();
		while (sit.hasNext())
		{
			Action a = (Action) sit.next();
			w.println(a);
			w.println("\tStrict Orderings: "+strictOrderings.get(a));
			w.println("\tLess than or equal Orderings: "+equalOrderings.get(a));
		}
	}
}
