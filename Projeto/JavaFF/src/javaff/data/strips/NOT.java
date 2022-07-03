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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javaff.data.GroundEffect;
import javaff.data.Literal;
import javaff.data.PDDLPrinter;
import javaff.data.UngroundCondition;
import javaff.data.UngroundEffect;
import javaff.planning.STRIPSState;
import javaff.planning.State;

public class NOT implements GroundEffect, UngroundEffect
{
	Literal literal;

	public NOT(Literal l)
	{
		literal = l;
	}

	public void apply(State s)
	{
		((STRIPSState) s).removeProposition((Proposition) literal); 
	}

	public void applyAdds(State s)
    {
	}

	public void applyDels(State s)
    {
		apply(s);
	}

	public boolean effects(PredicateSymbol s)
	{
		UngroundEffect ue = (UngroundEffect) literal;
		return ue.effects(s);
	}

	public UngroundCondition effectsAdd(UngroundCondition c)
	{
		return c;
	}

	public GroundEffect groundEffect(Map varMap)
	{
		return new NOT((Proposition) ((Predicate) literal).groundEffect(varMap));
	}

	public Set getAddPropositions()
	{
		return new HashSet();
	}
	
	public Set getDeletePropositions()
	{
		Set rSet = new HashSet();
		rSet.add(literal);
		return rSet;
	}

  public Set getOperators()
  {
  	return new HashSet();
  }


	public boolean equals(Object o)
    {
        if (o instanceof NOT)
		{
			NOT n = (NOT) o;
			return (literal.equals(n.literal));
		}
		else return false;
    }

	public GroundEffect staticifyEffect(Map fValues)
	{
		return this;
	}

    public int hashCode()
    {
        return literal.hashCode()^2;
    }

	public String toString()
	{
		return "not ("+literal.toString()+")";
	}

	public String toStringTyped()
	{
		return "not ("+literal.toStringTyped()+")";
	}

	
	public void pddlPrint(java.io.PrintStream s, int indent)
	{
		s.print("(not ");
		PDDLPrinter.printToString(literal, s, false, true, indent);
		s.print(")");
	}

}
