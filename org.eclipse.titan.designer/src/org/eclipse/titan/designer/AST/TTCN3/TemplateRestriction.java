/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3;

import java.text.MessageFormat;

import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TTCN3Template;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;


/**
 * Provides the data type to store the template restriction data
 * and helper functions used by the AST nodes of TTCN-3 definitions which contain restriction.
 *
 * @author Kristof Szabados
 * */
public final class TemplateRestriction {

	public enum Restriction_type {
		/** no restriction was given. */ TR_NONE(""),
		TR_OMIT("omit"),
		TR_VALUE("value"),
		TR_PRESENT("present");

		private final String name;

		private Restriction_type(final String name) {
			this.name = name;
		}

		public String getDisplayName() {
			return name;
		}
	}

	/** private constructor to disable instantiation */
	private TemplateRestriction() {
	}

	/**
	 * Calculates the restriction of the sub-fields from the restriction of the template.
	 *
	 * @param tr the restriction on the definition
	 * @param timestamp compilation timestamp
	 * @param ref the reference that points to the definition or to one of it's subfields
	 *
	 * @return the restriction on the sub-field
	 * */
	public static Restriction_type getSubRestriction(final Restriction_type tr, final CompilationTimeStamp timestamp, final Reference ref) {
		if (ref == null || ref.getSubreferences() == null) {
			return tr;
		}
		boolean isOptional = true;
		final Assignment ass = ref.getRefdAssignment(timestamp, false, null);
		if (ass != null) {
			IType type = ass.getType(timestamp);
			if (type != null) {
				//TODO maybe we should have a different function to check if we are referring an optional type.
				type = type.getFieldType(timestamp, ref, 1, Expected_Value_type.EXPECTED_TEMPLATE, true);
				if (type != null) {
					isOptional = false;
				}
			}
		}
		switch (tr) {
		case TR_NONE:
			return Restriction_type.TR_NONE;
		case TR_OMIT:
			return Restriction_type.TR_OMIT;
		case TR_VALUE:
			return isOptional ? Restriction_type.TR_OMIT : Restriction_type.TR_VALUE;
		case TR_PRESENT:
			return isOptional ? Restriction_type.TR_NONE : Restriction_type.TR_PRESENT;
		default:
			return tr;
		}
	}

	/**
	 * Checks if neededTemplateRestriction is satisfied by refd_tr.
	 *
	 * @param neededTemplateRestriction the restriction that should be satisfied.
	 * @param refdTemplateRestriction the restriction that should be satisfying.
	 *
	 * @return true if neededTemplateRestriction is satisfied by refd_tr, false otherwise.
	 * */
	public static boolean isLessRestrictive(final Restriction_type neededTemplateRestriction, final Restriction_type refdTemplateRestriction) {
		switch (neededTemplateRestriction) {
		case TR_NONE:
			return false;
		case TR_VALUE:
			return refdTemplateRestriction != Restriction_type.TR_VALUE;
		case TR_OMIT:
			return refdTemplateRestriction != Restriction_type.TR_VALUE && refdTemplateRestriction != Restriction_type.TR_OMIT;
		case TR_PRESENT:
			return refdTemplateRestriction != Restriction_type.TR_VALUE && refdTemplateRestriction != Restriction_type.TR_PRESENT;
		default:
			return true;
		}
	}

	public static boolean check(final CompilationTimeStamp timestamp, final Definition definition, final ITTCN3Template template, final Reference ref) {
		if (template.getIsErroneous(timestamp)) {
			return false;
		}

		final ITTCN3Template last = template.getTemplateReferencedLast(timestamp);
		Restriction_type tr = definition.getTemplateRestriction();
		tr = getSubRestriction(tr, timestamp, ref);
		switch (tr) {
		case TR_NONE:
			return false;
		case TR_VALUE:
			return last.checkValueomitRestriction(timestamp, definition.getAssignmentName(), false, template.getLocation());
		case TR_OMIT:
			return last.checkValueomitRestriction(timestamp, definition.getAssignmentName(), true, template.getLocation());
		case TR_PRESENT:
			return last.checkPresentRestriction(timestamp, definition.getAssignmentName(), template.getLocation());
		default:
			return false;
		}
	}

	public static void generateRestrictionCheckCode(final JavaGenData aData, final StringBuilder source, final Location location, final String name, final Restriction_type templateRestriction) {
		String restrictionName;
		switch(templateRestriction) {
		case TR_OMIT:
			restrictionName = "template_res.TR_OMIT";
			break;
		case TR_VALUE:
			restrictionName = "template_res.TR_VALUE";
			break;
		case TR_PRESENT:
			restrictionName = "template_res.TR_PRESENT";
			break;
		default:
			return;
		}

		aData.addBuiltinTypeImport("Base_Template.template_res");

		final boolean omitInValueList = TTCN3Template.allowOmitInValueList(location, true);
		source.append(MessageFormat.format("{0}.check_restriction({1}, null, {2});\n", name, restrictionName, omitInValueList? "true": "false"));
	}
}
