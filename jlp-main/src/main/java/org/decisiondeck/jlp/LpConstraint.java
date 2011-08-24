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

import org.decisiondeck.jlp.utils.LpSolverUtils;

import com.google.common.base.Equivalence;
import com.google.common.base.Preconditions;

/**
 * <p>
 * A constraint in a linear program, or mixed linear program, consisting of a linear expression on the left hand side
 * (lhs) and a constant on the right hand side (rhs), and an equality or inequality sign (as an {@link LpOperator}) in
 * between.
 * </p>
 * <p>
 * Such a constraint may have a name that may be used to describe it, e.g. when writing a mathematical problem to a
 * file.
 * </p>
 * <p>
 * This class is immutable.
 * </p>
 * <p>
 * A constraint {@link #equals(Object)} an other constraint iff they have the same left hand side, operator, and right
 * hand side. The name is not considered.
 * </p>
 * 
 * @author Olivier Cailloux
 * 
 * @param <T>
 *            the class used for the variables.
 */
public class LpConstraint<T> {
    private final LpLinearImmutable<T> m_lhs;

    /**
     * Never <code>null</code>, empty if not set.
     */
    private final String m_name;

    private final LpOperator m_op;

    private final double m_rhs;

    /**
     * @param name
     *            <code>null</code> or empty for no name.
     * @param lhs
     *            not <code>null</code>, not empty.
     * @param op
     *            not <code>null</code>.
     * @param rhs
     *            a valid number (not infinite, not NaN).
     */
    public LpConstraint(String name, LpLinear<T> lhs, LpOperator op, double rhs) {
	Preconditions.checkNotNull(lhs);
	Preconditions.checkNotNull(op);
	Preconditions.checkArgument(!Double.isInfinite(rhs));
	Preconditions.checkArgument(!Double.isNaN(rhs));
	Preconditions.checkArgument(lhs.size() >= 1);
	m_name = name == null ? "" : name;
	m_lhs = new LpLinearImmutable<T>(lhs);
	m_op = op;
	m_rhs = rhs;
    }

    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof LpConstraint)) {
	    return false;
	}
	LpConstraint<?> c2 = (LpConstraint<?>) obj;
	return LpSolverUtils.equivalent(this, c2);
    }

    /**
     * @return not <code>null</code>, not empty.
     */
    public LpLinear<T> getLhs() {
	return m_lhs;
    }

    /**
     * Retrieves the name of the problem.
     * 
     * @return never <code>null</code>, empty if not set.
     */
    public String getName() {
	return m_name;
    }

    /**
     * @return the op
     */
    public LpOperator getOperator() {
	return m_op;
    }

    /**
     * @return the rhs
     */
    public double getRhs() {
	return m_rhs;
    }

    @Override
    public int hashCode() {
	final Equivalence<LpConstraint<T>> equivalence = LpSolverUtils.getConstraintEquivalence();
	return equivalence.hash(this);
    }

    @Override
    public String toString() {
	return LpSolverUtils.getAsString(this);
    }
}
