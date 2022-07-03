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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javaff.data.Action;
import javaff.data.UngroundProblem;

public abstract class Operator implements javaff.data.PDDLPrintable
{
    public OperatorName name;
    public List params = new ArrayList(); // list of Variables

    public String toString()
    {
		String stringrep = name.toString();
		for (Iterator i = params.iterator(); i.hasNext();) {
			Variable v = (Variable) i.next();
			stringrep += " " + v.toString();
		}
		return stringrep;
    }

	public String toStringTyped()
    {
		String stringrep = name.toString();
		for (Iterator i = params.iterator(); i.hasNext();) {
			Variable v = (Variable) i.next();
			stringrep += " " + v.toStringTyped();
		}
		return stringrep;
    }

	public abstract boolean effects(PredicateSymbol s);
	protected abstract Action ground(Map varMap);
	public abstract Set getStaticConditionPredicates();
	
	public Action ground(List values)
	{
		Map varMap = new Hashtable();
		Iterator vit = values.iterator();
		Iterator pit = params.iterator();
		while (pit.hasNext())
		{
			Variable v = (Variable) pit.next();
			varMap.put(v,((PDDLObject) vit.next()));
		}
		return this.ground(varMap);
		
		
	}

	public Set ground(UngroundProblem p)
	{
		Set s = getParameterCombinations(p);
		Set rSet = new HashSet();
		Iterator sit = s.iterator();
		while (sit.hasNext())
		{
			List l = (List) sit.next();
			rSet.add(ground(l));
		}
		return rSet;
	}

	public Set getParameterCombinations(UngroundProblem up)
	{
		int arraysize = params.size();

		Set staticConditions = getStaticConditionPredicates();

		boolean[] set = new boolean[arraysize]; // which of the parameters has been fully set
		Arrays.fill(set, false);

		List combination = new ArrayList(arraysize);
		for (int i = 0; i < arraysize; ++i)
			combination.add(null);

		// Set for holding the combinations
		Set combinations = new HashSet();
		combinations.add(combination);

		for (Iterator scit = staticConditions.iterator(); scit.hasNext();) {
			Predicate p = (Predicate) scit.next();
			Set newcombs = new HashSet();
			Set sp = (HashSet) up.staticPropositionMap.get(p.getPredicateSymbol());
			Iterator spit = sp.iterator();
			while (spit.hasNext()) {
				Proposition prop = (Proposition) spit.next();
				Iterator combit = combinations.iterator();
				while (combit.hasNext()) {
					ArrayList c = (ArrayList) combit.next();
					boolean ok = true;
					Iterator propargit = prop.getParameters().iterator();
					int counter = 0;
					while (propargit.hasNext() && ok) {
						PDDLObject arg = (PDDLObject) propargit.next();
						Variable k = (Variable) p.getParameters().get(counter);
						int i = params.indexOf(k);
						boolean condition = i >= 0 && set[i] && !c.get(i).equals(arg);
						if (condition)
							ok = false;
						counter++;
					}
					if (ok) {
						List sdup = (ArrayList) c.clone();
						counter = 0;
						propargit = prop.getParameters().iterator();
						while (propargit.hasNext()) {
							PDDLObject arg = (PDDLObject) propargit.next();
							Variable k = (Variable) p.getParameters().get(counter);
							int i = params.indexOf(k);
							if (i >= 0) {
								sdup.set(i, arg);
								counter++;
							}
						}
						newcombs.add(sdup);
					}
				}
			}
			combinations = newcombs;
			Iterator pit = p.getParameters().iterator();
			while (pit.hasNext()) {
				Variable s = (Variable) pit.next();
				int i = params.indexOf(s);
				if (i >= 0)
					set[i] = true;
			}
		}

		int counter = 0;
		for (Iterator pit = params.iterator(); pit.hasNext();) {
			Variable p = (Variable) pit.next();
			if (!set[counter]) {
				Set newcombs = new HashSet();
				Iterator cit = combinations.iterator();
				while (cit.hasNext()) {
					ArrayList s = (ArrayList) cit.next();
					Set objs = (HashSet) up.typeSets.get(p.getType());
					Iterator oit = objs.iterator();
					while (oit.hasNext()) {
						PDDLObject ob = (PDDLObject) oit.next();
						List sdup = (ArrayList) s.clone();
						sdup.set(counter, ob);
						newcombs.add(sdup);
					}
				}
				combinations = newcombs;
			}
			++counter;
		}
		return combinations;
		
	}
}
