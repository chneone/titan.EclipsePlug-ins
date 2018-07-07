/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Port_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents a generic type mapping target.
 *
 * @author Kristof Szabados
 * */
public abstract class TypeMappingTarget extends ASTNode implements ILocateableNode {
	public enum TypeMapping_type {
		SIMPLE, DISCARD, FUNCTION, ENCODE, DECODE
	}

	/** the time when this attribute was checked the last time. */
	protected CompilationTimeStamp lastTimeChecked;

	/**
	 * The location of the whole mapping. This location encloses the mapping
	 * fully, as it is used to report errors to.
	 **/
	private Location location = NULL_Location.INSTANCE;

	public abstract TypeMapping_type getTypeMappingType();

	public abstract String getMappingName();

	@Override
	/** {@inheritDoc} */
	public final void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	/** {@inheritDoc} */
	public final Location getLocation() {
		return location;
	}

	public abstract Type getTargetType();

	/**
	 * Does the semantic checking of the type mapping target.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param sourceType
	 *                the type used as source for the mapping.
	 * @param portType
	 *                the type of the mapping port.
	 * @param legacy
	 *                is this the legacy behavior.
	 * @param incoming
	 *                is it mapping in incoming direction?
	 * */
	public abstract void check(final CompilationTimeStamp timestamp, final Type sourceType, final Port_Type portType, final boolean legacy, final boolean incoming);
}
