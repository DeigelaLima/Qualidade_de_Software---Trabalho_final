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
import java.util.Map;

import javaff.data.PDDLPrinter;
import javaff.data.metric.Function;
import javaff.data.metric.MetricSymbolStore;
import javaff.planning.MetricState;

public class SimpleDurationConstraint extends DurationConstraint
{
	protected int type;
	protected DurationFunction variable;
	protected Function value;

	public SimpleDurationConstraint(DurationFunction v, Function f, int t)
	{
		type = t;
		variable = v;
		value = f;
	}

	public DurationConstraint ground(Map varMap)
	{
		return new SimpleDurationConstraint((DurationFunction) variable.ground(varMap), value.ground(varMap), type);
	}

	public BigDecimal getDuration(MetricState s)
	{
		return value.getValue(s);
	}

	//could put stuff about < and > using epsilon
	public BigDecimal getMaxDuration(MetricState s)
	{
		if (type == MetricSymbolStore.lessThanEqual) return value.getValue(s);
		else if (type == MetricSymbolStore.greaterThanEqual) return javaff.JavaFF.maxDuration;
		else if (type == MetricSymbolStore.equal) return value.getValue(s);

		else return null;
	}

	public BigDecimal getMinDuration(MetricState s)
	{
		if (type == MetricSymbolStore.lessThanEqual) return new BigDecimal(0);
		else if (type == MetricSymbolStore.greaterThanEqual) return value.getValue(s);
		else if (type == MetricSymbolStore.equal) return value.getValue(s);
		else return null;
	}

	public boolean staticDuration()
	{
		//return value.isStatic();
		return (type == MetricSymbolStore.equal);
	}

	
	public void addConstraint(SimpleDurationConstraint c)
    {

	}

	public void pddlPrint(PrintStream s, int indent)
	{
		PDDLPrinter.printToString(this, s, true, false, indent);
	}

	public String toString()
	{
		return "(" + MetricSymbolStore.getSymbol(type) + " " + variable.toString() + " " + value.toString() + ")";
	}

	public String toStringTyped()
	{
		return "(" + MetricSymbolStore.getSymbol(type) + " " + variable.toStringTyped() + " " + value.toStringTyped() + ")";
	}

	public int hashCode()
	{
		int hash = 31 * 7 ^ type;
		hash = 31 * (31 * hash ^ variable.hashCode()) ^ value.hashCode();
		return hash;
	}

	public boolean equals(Object o)
	{
		if (o instanceof SimpleDurationConstraint)
		{
			SimpleDurationConstraint c = (SimpleDurationConstraint) o;
			return (type == c.type && variable.equals(c.variable) && value.equals(c.value));
		}
		else return false;
	}
	
}
