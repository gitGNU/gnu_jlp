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

import java.util.Set;

/**
 * <p>
 * A linear expression consisting of a sum of terms, where a term is a coefficient multiplying a variable. As addition
 * is commutative, the set of term order is irrelevant. A linear object {@link #equals(Object)} an other one iff they
 * contain the same terms.
 * </p>
 * <p>
 * Such a linear object may be immutable, in which case the methods modifying the state will throw
 * {@link UnsupportedOperationException}.
 * </p>
 * 
 * @author Olivier Cailloux
 * 
 * @param <T>
 *            the class used for the variables.
 */
public interface LpLinear<T> extends Set<LpTerm<T>> {
    /**
     * Adds a term to this linear expression.
     * 
     * @param coefficient
     *            a valid double.
     * @param variable
     *            not <code>null</code>.
     * @return <code>true</code> if this set did not already contain the specified element.
     */
    public boolean add(double coefficient, T variable);
}
