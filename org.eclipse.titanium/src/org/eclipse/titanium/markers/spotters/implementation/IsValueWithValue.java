/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.IsValueExpression;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
 * This class marks the following code smell:
 * isvalue expressions used with a value as parameter.
 *
 * @author Viktor Varga
 */
public class IsValueWithValue extends BaseModuleCodeSmellSpotter {

	private static final String ERR_MSG = "Isvalue check on value always returns true. 'isbound' should be used to check existence. ";

	public IsValueWithValue() {
		super(CodeSmellType.ISVALUE_WITH_VALUE);
	}

	@Override
	protected void process(final IVisitableNode node, final Problems problems) {
		if (!(node instanceof IsValueExpression)) {
			return;
		}
		final IsValueExpression ive = (IsValueExpression)node;
		final ExpressionVisitor visitor = new ExpressionVisitor(problems);
		ive.accept(visitor);
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(IsValueExpression.class);
		return ret;
	}

	private static final class ExpressionVisitor extends ASTVisitor {

		private final Problems problems;

		public ExpressionVisitor(final Problems problems) {
			this.problems = problems;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof IsValueExpression) {
				return V_CONTINUE;
			} else if (node instanceof TemplateInstance) {
				return V_CONTINUE;
			} else if (node instanceof TTCN3Template) {
				ITTCN3Template template = (TTCN3Template)node;
				template = template.setLoweridToReference(CompilationTimeStamp.getBaseTimestamp());
				if (template.isValue(CompilationTimeStamp.getBaseTimestamp())) {
					problems.report(template.getLocation(), ERR_MSG);
					return V_SKIP;
				}
			}
			return V_SKIP;
		}

	}

}
