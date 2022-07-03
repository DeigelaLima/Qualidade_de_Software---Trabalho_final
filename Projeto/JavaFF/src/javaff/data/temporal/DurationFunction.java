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

import java.math.BigDecimal;
import java.util.Map;

import javaff.data.metric.Function;
import javaff.data.metric.NamedFunction;
import javaff.data.metric.NumberFunction;
import javaff.planning.MetricState;
import javaff.scheduling.MatrixSTN;

public class DurationFunction extends NamedFunction
{
	public DurativeAction durativeAction;
	UngroundDurativeAction ungroundDurativeAction;        //horrible hack
	
	public DurationFunction(DurativeAction da)
    {
		durativeAction = da;
	}

	public DurationFunction(UngroundDurativeAction uda)
    {
		ungroundDurativeAction = uda;
	}

	public BigDecimal getValue(MetricState s)
	{
		return durativeAction.getDuration(s);
	}

	public BigDecimal getMaxValue(MatrixSTN n)
	{
		return n.getMaximum(durativeAction);
	}

	public BigDecimal getMinValue(MatrixSTN n)
	{
		return n.getMinimum(durativeAction);
	}

	public Function staticify(Map fValues)
	{
		if (!durativeAction.staticDuration())
			return this;
		BigDecimal d = getValue(null);
		return new NumberFunction(d);
	}

	public Function makeOnlyDurationDependent(MetricState s)
	{
		return this;
	}
	
	public Function ground(Map varMap)
	{
		return (Function) varMap.get(this);
	}


	public String toString()
	{
		return "?duration";
	}

	public String toStringTyped()
	{
		return "?duration";
	}

	public int hashCode()
	{
		int hash = 7;
		hash = durativeAction != null ? 31 * hash ^ durativeAction.hashCode()
				: 31 * hash ^ ungroundDurativeAction.hashCode();
		return hash;
	}

	public boolean equals(Object o)
	{
		if (o instanceof DurationFunction)
		{
			DurationFunction f = (DurationFunction) o;
			if (f.durativeAction != null && durativeAction != null) return durativeAction.equals(f.durativeAction);
			else if (f.ungroundDurativeAction != null && ungroundDurativeAction != null) return ungroundDurativeAction.equals(f.ungroundDurativeAction);
			else return false;
		}
		else return false;
	}

}
