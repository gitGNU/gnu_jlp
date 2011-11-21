/**
 * Copyright © 2010-2011 École Centrale Paris
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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.decisiondeck.jlp.utils.LpUtils;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

/**
 * A simple mutable implementation of {@link LpLinear} based on a {@link LinkedHashSet}.
 * 
 * @param <T>
 *            the class used for the variables.
 * 
 * @author Olivier Cailloux
 * 
 */
public class LpLinearImpl<T> extends LinkedHashSet<LpTerm<T>> implements LpLinear<T>, Set<LpTerm<T>> {

    private static final long serialVersionUID = 1L;

    public LpLinearImpl() {
	/** Public no argument constructor. */
    }

    public LpLinearImpl(Collection<LpTerm<T>> terms) {
	super(terms);
    }

    @Override
    public boolean add(double coefficient, T variable) {
	LpTerm<T> term = new LpTerm<T>(coefficient, variable);
	return add(term);
    }

    @Override
    public String toString() {
	final ToStringHelper helper = Objects.toStringHelper(this);
	helper.add("size", "" + size());
	helper.add("expr", LpUtils.getAsString(this));
	return helper.toString();
    }

}
