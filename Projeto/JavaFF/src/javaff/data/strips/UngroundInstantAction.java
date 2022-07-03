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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javaff.data.Action;
import javaff.data.PDDLPrinter;
import javaff.data.UngroundCondition;
import javaff.data.UngroundEffect;

public class UngroundInstantAction extends Operator
{
    public UngroundCondition condition;
    public UngroundEffect effect;

	public boolean effects(PredicateSymbol s)
	{
		return effect.effects(s);
	}

	public Action ground(Map varMap)
	{
		return ground(varMap, new STRIPSInstantAction());
	}

	public Action ground(Map varMap, InstantAction a)
	{
		a.name = this.name;

		for (Iterator pit = params.iterator(); pit.hasNext();) {
			Variable v = (Variable) pit.next();
			PDDLObject o = (PDDLObject) varMap.get(v);
			a.params.add(o);
		}
		a.condition = condition.groundCondition(varMap);
		a.effect = effect.groundEffect(varMap);
		return a;
	}

	public Set getStaticConditionPredicates()
	{
		return condition.getStaticPredicates();
	}

	public void pddlPrint(java.io.PrintStream s, int indent)
	{
		s.println();
		PDDLPrinter.printIndent(s, indent);
		s.print("(:action ");
		s.print(name);
		s.println();
		PDDLPrinter.printIndent(s, indent+1);
		s.print(":parameters(\n");
		PDDLPrinter.printToString(params, s, true, false, indent+2);
		s.println(")");
		PDDLPrinter.printIndent(s, indent+1);
		s.print(":precondition");
		condition.pddlPrint(s, indent+2);
		s.println();
		PDDLPrinter.printIndent(s, indent+1);
		s.print(":effect");
		effect.pddlPrint(s, indent+2);
		s.print(")");
	}

}
