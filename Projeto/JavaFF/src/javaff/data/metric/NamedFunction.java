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

package javaff.data.metric;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;

import javaff.data.strips.PDDLObject;
import javaff.data.strips.PredicateSymbol;
import javaff.data.strips.Variable;
import javaff.planning.MetricState;
import javaff.scheduling.MatrixSTN;

public class NamedFunction extends javaff.data.Literal implements Function
{
	protected NamedFunction()
    {

	}
	
	public NamedFunction(FunctionSymbol fs)
    {
		super.setPredicateSymbol((PredicateSymbol)fs);
	}
	
	public BigDecimal getValue(MetricState s)
    {
		return s.getValue(this);
	}

	public BigDecimal getMaxValue(MatrixSTN n)
    {
		return getValue(null);
	}

	public BigDecimal getMinValue(MatrixSTN n)
    {
		return getValue(null);
	}

	public boolean effectedBy(ResourceOperator o)
    {
		return this.equals(o.resource);
	}

	public Function replace(ResourceOperator o)
    {
		if (o.resource.equals(this))
		{
			if (o.type == MetricSymbolStore.increase) return new BinaryFunction(MetricSymbolStore.plus, this, o.change);
			else if (o.type == MetricSymbolStore.decrease) return new BinaryFunction(MetricSymbolStore.minus, this, o.change);
			else if (o.type == MetricSymbolStore.scaleUP) return new BinaryFunction(MetricSymbolStore.multiply, this, o.change);
			else if (o.type == MetricSymbolStore.scaleDown) return new BinaryFunction( MetricSymbolStore.divide, this, o.change);
			else if (o.type == MetricSymbolStore.assign) return o.change;
			else return this;
		}
		else return this;
	}

	public Function staticify(Map fValues)
    {
		if (!isStatic())
			return this;
		BigDecimal d = (BigDecimal) fValues.get(this);
		return new NumberFunction(d);
	}

	public Function makeOnlyDurationDependent(MetricState s)
    {
		return new NumberFunction(getValue(s));
	}

	public Function ground(Map varMap)
    {
		NamedFunction nf = new NamedFunction((FunctionSymbol)name);
		for (Iterator pit = parameters.iterator(); pit.hasNext();) {
			Variable v = (Variable) pit.next();
			PDDLObject po = (PDDLObject) varMap.get(v);
			nf.addParameter(po);
		}
		return nf;
	}

	public String toString()
    {
		return "("+super.toString()+")";
    }

	public String toStringTyped()
    {
		return "("+super.toStringTyped()+")";
	}

	public int hashCode()
    {
		int hash = 31 * 7 ^ name.hashCode();
		return hash = 31 * hash ^ parameters.hashCode();
    }
}
