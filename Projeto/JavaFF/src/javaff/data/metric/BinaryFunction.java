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
import java.util.Map;

import javaff.planning.MetricState;
import javaff.scheduling.MatrixSTN;

public class BinaryFunction implements Function {

	public Function first;
	public Function second;
	public int type;

	public BinaryFunction(String s, Function f1, Function f2) {
		type = MetricSymbolStore.getType(s);
		first = f1;
		second = f2;
	}

	public BinaryFunction(int t, Function f1, Function f2) {
		type = t;
		first = f1;
		second = f2;
	}

	public boolean effectedBy(ResourceOperator o) {
		return (first.effectedBy(o) || second.effectedBy(o));
	}

	public Function replace(ResourceOperator o) {
		return new BinaryFunction(type, first.replace(o), second.replace(o));
	}

	public boolean isStatic() {
		return (first.isStatic() && second.isStatic());
	}

	public Function staticify(Map fValues) {
		first = first.staticify(fValues);
		second = second.staticify(fValues);
		if (isStatic()) {
			return new NumberFunction(getValue(null));
		} else
			return this;
	}

	public Function makeOnlyDurationDependent(MetricState s) {
		BinaryFunction bf = new BinaryFunction(type, first.makeOnlyDurationDependent(s),
				second.makeOnlyDurationDependent(s));
		Function f = null;
		if (bf.first instanceof NumberFunction && bf.second instanceof NumberFunction) {
			f = new NumberFunction(bf.getValue(s));
		} else
			f = bf;
		return f;
	}

	public Function ground(Map varMap) {
		return new BinaryFunction(type, first.ground(varMap), second.ground(varMap));
	}

	public boolean equals(Object o) {
		if (o instanceof BinaryFunction) {
			BinaryFunction bf = (BinaryFunction) o;
			if (bf.type == this.type && first.equals(bf.first) && second.equals(bf.second))
				return true;
			else if (((bf.type == MetricSymbolStore.plus && this.type == MetricSymbolStore.plus)
					|| (this.type == MetricSymbolStore.multiply && bf.type == MetricSymbolStore.multiply))
					&& (first.equals(bf.second) && second.equals(bf.first)))
				return true;
			else
				return false;
		} else
			return false;
	}

	public String toString() {
		return "(" + MetricSymbolStore.getSymbol(type) + " " + first.toString() + " " + second.toString() + ")";
	}

	public String toStringTyped() {
		return "(" + MetricSymbolStore.getSymbol(type) + " " + first.toStringTyped() + " " + second.toStringTyped()
				+ ")";
	}

	public BigDecimal getValue(MetricState s) {
		BigDecimal fbd = first.getValue(s);
		BigDecimal sbd = second.getValue(s);
		if (type == MetricSymbolStore.plus)
			return fbd.add(sbd);
		if (type == MetricSymbolStore.minus)
			return fbd.subtract(sbd);
		else if (type == MetricSymbolStore.multiply)
			return fbd.multiply(sbd);
		else if (type == MetricSymbolStore.divide)
			return fbd.divide(sbd, MetricSymbolStore.scale, MetricSymbolStore.round);
		else
			return null;
	}

	public BigDecimal getMaxValue(MatrixSTN n) {
		if (type == MetricSymbolStore.plus)
			return first.getMaxValue(n).add(second.getMaxValue(n));
		else if (type == MetricSymbolStore.minus)
			return first.getMaxValue(n).subtract(second.getMinValue(n));
		else if (type == MetricSymbolStore.multiply)
			return first.getMaxValue(n).multiply(second.getMaxValue(n));
		else if (type == MetricSymbolStore.divide)
			return first.getMaxValue(n).divide(second.getMinValue(n), MetricSymbolStore.scale, MetricSymbolStore.round);
		else
			return null;
	}

	public BigDecimal getMinValue(MatrixSTN n) {
		if (type == MetricSymbolStore.plus)
			return first.getMinValue(n).add(second.getMinValue(n));
		else if (type == MetricSymbolStore.minus)
			return first.getMinValue(n).subtract(second.getMaxValue(n));
		else if (type == MetricSymbolStore.multiply)
			return first.getMinValue(n).multiply(second.getMinValue(n));
		else if (type == MetricSymbolStore.divide)
			return first.getMinValue(n).divide(second.getMaxValue(n), MetricSymbolStore.scale, MetricSymbolStore.round);
		else
			return null;
	}
}
