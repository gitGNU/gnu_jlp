/**
 * Copyright © 2010-2011 Olivier Cailloux
 *
 *     This file is part of JLP.
 *
 *     JLP is free software: you can redistribute it and/or modify it under the
 *     terms of the GNU Lesser General Public License version 3 as published by
 *     the Free Software Foundation.
 *
 *     JLP is distributed in the hope that it will be useful, but WITHOUT ANY
 * 	WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * 	FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * 	more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with JLP. If not, see <http://www.gnu.org/licenses/>.
 */
package org.decisiondeck.jlp;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ForwardingSet;

public class LpLinearImmutable<T> extends ForwardingSet<LpTerm<T>> implements LpLinear<T> {
    private final LpLinear<T> m_delegate;

    public LpLinearImmutable(Collection<LpTerm<T>> terms) {
	m_delegate = new LpLinearImpl<T>(terms);
    }

    /**
     * @param source
     *            not <code>null</code>.
     */
    public LpLinearImmutable(LpLinear<T> source) {
	Preconditions.checkNotNull(source);
	if (source instanceof LpLinearImmutable) {
	    m_delegate = source;
	} else {
	    m_delegate = new LpLinearImpl<T>(source);
	}
    }

    @Override
    public boolean add(double coefficient, T variable) {
	throw new UnsupportedOperationException("This object is immutable.");
    }

    @Override
    protected Set<LpTerm<T>> delegate() {
	return Collections.unmodifiableSet(m_delegate);
    }
}
