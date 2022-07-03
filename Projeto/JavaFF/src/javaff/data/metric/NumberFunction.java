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

public class NumberFunction implements Function {
	protected final BigDecimal num;

	public NumberFunction(double d) {
		num = new BigDecimal(d);
	}

	public NumberFunction(BigDecimal d) {
		num = d;
	}

	public BigDecimal getValue(MetricState s) {
		return num;
	}

	public BigDecimal getMaxValue(MatrixSTN n) {
		return getValue(null);
	}

	public BigDecimal getMinValue(MatrixSTN n) {
		return getValue(null);
	}

	public Function staticify(Map fValues) {
		return this;
	}

	public Function makeOnlyDurationDependent(MetricState s) {
		return this;
	}

	public String toString() {
		return num.toString();
	}

	public String toStringTyped() {
		return toString();
	}

	public boolean isStatic() {
		return true;
	}

	public boolean effectedBy(ResourceOperator o) {
		return false;
	}

	public Function replace(ResourceOperator o) {
		return this;
	}

	public Function ground(Map varMap) {
		return this;
	}

	public boolean equals(Object o) {
		if (o instanceof NumberFunction) {
			NumberFunction nf = (NumberFunction) o;
			return num.equals(nf.num);
		} else
			return false;
	}
}
