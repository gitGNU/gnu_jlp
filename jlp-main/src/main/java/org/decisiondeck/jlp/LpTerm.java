/**
 * Copyright © 2010-2011 Olivier Cailloux
 *
 * 	This file is part of JLP.
 *
 * 	JLP is free software: you can redistribute it and/or modify it under the
 * 	terms of the GNU Lesser General Public License version 3 as published by
 * 	the Free Software Foundation.
 *
 * 	JLP is distributed in the hope that it will be useful, but WITHOUT ANY
 * 	WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * 	FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * 	more details.
 *
 * 	You should have received a copy of the GNU Lesser General Public License
 * 	along with JLP. If not, see <http://www.gnu.org/licenses/>.
 */
package org.decisiondeck.jlp;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Preconditions;

/**
 * A term in a linear expression in a linear program or mixed integer program, consisting of a coefficient multiplying a
 * variable.
 * 
 * @author Olivier Cailloux
 * @param <T>
 *            the class used for the variables.
 * 
 */
public class LpTerm<T> {
    private final double m_coefficient;

    private final T m_variable;

    /**
     * @param coefficient
     *            a valid number.
     * @param variable
     *            not <code>null</code>.
     */
    public LpTerm(double coefficient, T variable) {
	Preconditions.checkNotNull(variable);
	Preconditions.checkArgument(!Double.isInfinite(coefficient));
	Preconditions.checkArgument(!Double.isNaN(coefficient));
	m_coefficient = coefficient;
	m_variable = variable;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (!(obj instanceof LpTerm<?>)) {
	    return false;
	}

	LpTerm<?> t2 = (LpTerm<?>) obj;
	if (m_coefficient != t2.m_coefficient) {
	    return false;
	}
	if (!m_variable.equals(t2.m_variable)) {
	    return false;
	}

	return true;
    }

    /**
     * Retrieves the coefficient that multiplies the variable in this term.
     * 
     * @return a valid number.
     */
    public double getCoefficient() {
	return m_coefficient;
    }

    /**
     * Retrieves the variable of this term.
     * 
     * @return not <code>null</code>.
     */
    public T getVariable() {
	return m_variable;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	long temp;
	temp = Double.doubleToLongBits(m_coefficient);
	result = prime * result + (int) (temp ^ (temp >>> 32));
	result = prime * result + m_variable.hashCode();
	return result;
    }

    @Override
    public String toString() {
	final ToStringHelper helper = Objects.toStringHelper(this);
	helper.addValue(getCoefficient() + "*" + getVariable());
	return helper.toString();
    }
}
