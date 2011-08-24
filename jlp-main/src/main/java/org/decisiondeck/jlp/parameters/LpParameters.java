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

import java.util.Map;

/**
 * <p>
 * Holds values of the parameters that may be used to configure a solver. The set of values a parameter can take and
 * their meaning is documented in the appropriate enum types, i.e. {@link LpDoubleParameter}, {@link LpIntParameter},
 * {@link LpStringParameter}. The default values for all parameters may be queried from the
 * {@link LpParametersDefaultValues} class.
 * </p>
 * <p>
 * A value associated to a parameter may be either not set, which means the value will be the default value for that
 * parameter, or set to a non-default value. Setting a parameter to its default value has the same effect as not setting
 * the parameter at all.
 * </p>
 * <p>
 * Some parameters may have <code>null</code> as a possible value, which has a special meaning depending on the
 * parameter, e.g. "be clever" or "the fastest" or "do not use that parameter". A <code>null</code> value is preferred
 * to a meaningless number value such as zero or a negative value for an Integer or Double parameter, as it makes clear
 * that the value is special.
 * </p>
 * <p>
 * The legality of some parameter values may vary according to the solver implementation. This is always mentioned in
 * the parameter documentation in the relevant enum type − which means that if nothing is mentioned about possible
 * values, everything is to be considered legal independently of the solver. Trying to set a parameter value to an
 * unmeaningful value (e.g. a negative number when a value accepts only positive numbers) results in an
 * {@link IllegalArgumentException} be thrown. However, setting the value of a parameter to a value that is
 * <em>possibly</em> illegal depending on the solver does not throw an exception. But the value may reveal illegal when
 * the solver is decided, so be cautious about possibly illegal values.
 * </p>
 * <p>
 * As of vocabulary, a value is <em>unmeaningful</em> if it is necessarily inadequate (e.g. a value of zero for a number
 * of threads); it is <em>illegal</em>, considering a given solver, if the solver does not implement that parameter
 * value. A value is also said to be <em>illegal</em> (independently of the solver) if it is unmeaningful. A value is
 * thus <em>meaningful</em> if it is possibly satisfiable, i.e. there exists at least one solver that will take the
 * value into account, and <em>legal</em> if it is meaningful and the chosen solver implements it. Some values are
 * <em>legal</em> independently of the solver, thus are <em>necessarily</em> legal.
 * <p>
 * A class implementing this interface may be read-only, in which case it should consistently throw
 * {@link UnsupportedOperationException} on the methods attempting to modify its state.
 * </p>
 * <p>
 * An {@link LpParameters} object <code>o1</code> {@link #equals(Object)} an {@link LpParameters} object <code>o2</code>
 * iff they contain the same value for each parameter. The hashcode of such an object is equal to
 * {@link LpParametersUtils#hash(LpParameters)}.
 * </p>
 * 
 * @author Olivier Cailloux
 * 
 */
public interface LpParameters {

    /**
     * Retrieves a copy of the non default double values set in this object.
     * 
     * @return not <code>null</code>.
     */
    public Map<LpDoubleParameter, Double> getDoubleParameters();

    /**
     * Retrieves a copy of the non default integer values set in this object.
     * 
     * @return not <code>null</code>.
     */
    public Map<LpIntParameter, Integer> getIntParameters();

    /**
     * Retrieves a copy of the non default string values set in this object.
     * 
     * @return not <code>null</code>.
     */
    public Map<LpStringParameter, String> getStringParameters();

    /**
     * Retrieves the value associated with the given parameter. If the value has not been set, returns the default value
     * for that parameter.
     * 
     * @param parameter
     *            not <code>null</code>.
     * @return a meaningful value for that parameter, possibly <code>null</code> as this is a meaningful value for some
     *         parameters.
     */
    public Double getValue(LpDoubleParameter parameter);

    /**
     * Retrieves the value associated with the given parameter. If the value has not been set, returns the default value
     * for that parameter.
     * 
     * @param parameter
     *            not <code>null</code>.
     * @return a meaningful value for that parameter, possibly <code>null</code> as this is a meaningful value for some
     *         parameters.
     */
    public Integer getValue(LpIntParameter parameter);

    /**
     * Retrieves the value associated with the given parameter. If the value has not been set, returns the default value
     * for that parameter.
     * 
     * @param parameter
     *            not <code>null</code>.
     * @return a meaningful value for that parameter, possibly <code>null</code> as this is a meaningful value for some
     *         parameters.
     */
    public String getValue(LpStringParameter parameter);

    /**
     * Retrieves the value associated with the given parameter. If the value has not been set, returns the default value
     * for that parameter. This is a non type safe method equivalent to other get methods found in this object.
     * 
     * @param parameter
     *            not <code>null</code>. The type must be {@link LpIntParameter}, {@link LpDoubleParameter} or
     *            {@link LpStringParameter}.
     * @return the associated value, possibly <code>null</code> as this is a meaningful value for some parameters.
     */
    public Object getValueAsObject(Enum<?> parameter);

    /**
     * Sets the value associated with a parameter. The value must be a meaningful value for that parameter. To restore a
     * parameter to its default value, use the value given by {@link LpParametersDefaultValues}.
     * 
     * @param parameter
     *            not <code>null</code>.
     * @param value
     *            a meaningful value for that parameter. May be <code>null</code> only if <code>null</code> is a
     *            meaningful value for that parameter.
     * @return <code>true</code> iff the state of this object changed as a result of this call. E.g., setting a default
     *         value for a parameter that had not previously been set returns <code>false</code>.
     */
    public boolean setValue(LpDoubleParameter parameter, Double value);

    /**
     * Sets the value associated with a parameter. The value must be a meaningful value for that parameter. To restore a
     * parameter to its default value, use the value given by {@link LpParametersDefaultValues}.
     * 
     * @param parameter
     *            not <code>null</code>.
     * @param value
     *            a meaningful value for that parameter. May be <code>null</code> only if <code>null</code> is a
     *            meaningful value for that parameter.
     * @return <code>true</code> iff the state of this object changed as a result of this call. E.g., setting a default
     *         value for a parameter that had not previously been set returns <code>false</code>.
     */
    public boolean setValue(LpIntParameter parameter, Integer value);

    /**
     * Sets the value associated with a parameter. The value must be a meaningful value for that parameter. To restore a
     * parameter to its default value, use the value given by {@link LpParametersDefaultValues}.
     * 
     * @param parameter
     *            not <code>null</code>.
     * @param value
     *            a meaningful value for that parameter. May be <code>null</code> only if <code>null</code> is a
     *            meaningful value for that parameter.
     * @return <code>true</code> iff the state of this object changed as a result of this call. E.g., setting a default
     *         value for a parameter that had not previously been set returns <code>false</code>.
     */
    public boolean setValue(LpStringParameter parameter, String value);

    /**
     * Sets the value associated with a parameter. The value must be a meaningful value for that parameter. To restore a
     * parameter to its default value, use the value given by {@link LpParametersDefaultValues}. This is a non type safe
     * method equivalent to other set methods found in this object.
     * 
     * @param parameter
     *            not <code>null</code>. The type must be {@link LpIntParameter}, {@link LpDoubleParameter} or
     *            {@link LpStringParameter}.
     * @param value
     *            a meaningful value for that parameter. May be <code>null</code> only if <code>null</code> is a
     *            meaningful value for that parameter.
     * @return <code>true</code> iff the state of this object changed as a result of this call. E.g., setting a default
     *         value for a parameter that had not previously been set returns <code>false</code>.
     */
    public boolean setValueAsObject(Enum<?> parameter, Object value);
}
