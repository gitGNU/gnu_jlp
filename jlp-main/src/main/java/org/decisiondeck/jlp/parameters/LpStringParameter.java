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
package org.decisiondeck.jlp.parameters;

public enum LpStringParameter {
    /**
     * If the implementing solver creates some temporary files during its solving process, they will be created in the
     * given directory or possibly a subdirectory of the given directory. Default to <code>null</code>, which means that
     * the solver will try to be clever and choose a temporary directory, or a directory with enough available space, or
     * anything it thinks is a good option. May not be empty.
     */
    WORK_DIR
}
