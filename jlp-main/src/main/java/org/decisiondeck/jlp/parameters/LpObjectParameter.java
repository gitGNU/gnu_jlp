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

import java.util.Map;

import org.decisiondeck.jlp.LpFileFormat;
import org.decisiondeck.jlp.problem.LpProblem;
import org.decisiondeck.jlp.problem.LpProblemUtils;

import com.google.common.base.Function;

public enum LpObjectParameter {
    /**
     * <p>
     * A function which, given a variable, returns its name. The value associated to this parameter must have a type
     * {@link Function}. The function must be deterministic: it must associate the same name to a given variable over
     * time as long as the underlying problem does not change. The function may only return string values or
     * <code>null</code>. A <code>null</code> or empty string return value means that the variable has no name. The
     * function is never given a <code>null</code> input.
     * </p>
     * <p>
     * The default value for this parameter is <code>null</code>, which means that the name of the variable set in the
     * problem should be used. In this case, the name given by {@link LpProblem#getVarNameComputed} will be used.
     * </p>
     * <p>
     * If this function is set, the variables names set in the problem and the variable namer set in the problem are
     * ignored. Therefore, the choice of a name for a variable when a solver is used is as follows. If this parameter is
     * set, that function is used. Else, if a function namer is set in the problem, that function is used. Else, if a
     * variable name is associated to the variable in the problem, that name is used. Else, the variable has no name.
     * Note that if a function is set and used as per the preceding rule, and if that function gives a <code>null</code>
     * or empty string return value for a given variable, the variable is considered unnamed: no other means of
     * obtaining a name for the variable is tried.
     * </p>
     * <p>
     * An example function that can be used is available through {@link LpProblemUtils#getToStringFunction()}.
     * </p>
     */
    NAMER_VARIABLES,

    /**
     * <p>
     * A function which, given a constraint, returns its name. The value associated to this parameter must have a type
     * {@link Function}. The function must be deterministic: it must associate the same name to a given constraint over
     * time as long as the underlying problem does not change. The function may only return string values or
     * <code>null</code>. A <code>null</code> or empty string return value means that the constraint has no name. The
     * function is never given a <code>null</code> input.
     * </p>
     * <p>
     * The default value for this parameter is <code>null</code>, which means that the name of the constraint as
     * determined by the problem should be used. In this case, the name given by {@link LpProblem#getConstraintsNamer()}
     * will be used.
     * </p>
     * <p>
     * If this function is set, the constraints namer set in the problem is ignored.
     * </p>
     */
    NAMER_CONSTRAINTS,

    /**
     * <p>
     * An association of export formats and namer functions. The export formats are those from {@link LpFileFormat}. The
     * namer functions must conform to the description of {@link #NAMER_VARIABLES}. The value of this parameter must be
     * a {@link Map}, or be <code>null</code>. The map tells which namer to use according to the chosen export format.
     * This parameter is only used when writing the problem. If the value of this parameter is <code>null</code>, or no
     * entry in the map matches the chosen export format, the default namer will be used according to the rule specified
     * in {@link #NAMER_VARIABLES}.
     * </p>
     * <p>
     * This parameter permits to specify namers depending on the export format, which is useful because different
     * formats have different tolerance viz. special characters.
     * </p>
     */
    NAMER_VARIABLES_BY_FORMAT, /**
     * <p>
     * An association of export formats and namer functions. The export formats are those from {@link LpFileFormat}. The
     * namer functions must conform to the description of {@link #NAMER_CONSTRAINTS}. The value of this parameter must
     * be a {@link Map}, or be <code>null</code>. The map tells which namer to use according to the chosen export
     * format. This parameter is only used when writing the problem. If the value of this parameter is <code>null</code>
     * , or no entry in the map matches the chosen export format, the default namer will be used according to the rule
     * specified in {@link #NAMER_CONSTRAINTS}.
     * </p>
     * <p>
     * This parameter permits to specify namers depending on the export format, which is useful because different
     * formats have different tolerance viz. special characters.
     * </p>
     */
    NAMER_CONSTRAINTS_BY_FORMAT
}
