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
package org.decisiondeck.jlp.parameters;

import org.decisiondeck.jlp.LpSolverException;

public enum LpDoubleParameter {
    MAX_CPU_SECONDS, MAX_TREE_SIZE_MB, /**
     * Caution must be exercised when using very small values for this parameter. The
     * value will be rounded if the underlying solver accepts only integer number of seconds. In that case and if the
     * timeout is less than 0.5 seconds, it would round to zero seconds, and a {@link LpSolverException} would be raised
     * when solving.
     */
    MAX_WALL_SECONDS
}
