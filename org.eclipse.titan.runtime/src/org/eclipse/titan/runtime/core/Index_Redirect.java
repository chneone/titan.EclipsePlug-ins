/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

/** Base class for index redirects
*
* For every index redirect the compiler generates a new class that inherits
* this one and implements its virtual function. An instance of the new class
* is passed to a port, timer or component operation, performed on an array with
* the help of the 'any from' clause, which, if successful, calls the instance's
* add_index() function once for each dimension in the array.
*
* List of operations in question: receive, check-receive, trigger, getcall,
* check-getcall, getreply, check-getreply, catch, check-catch, check, timeout,
* running (for both components and timers), done, killed and alive.
*
*
* @author Gergo Ujhelyi
**/


public abstract class Index_Redirect {
	/** If the port, timer or component operation in question succeeds, then the
	 * index in the current dimension of the port, timer or component array is
	 * stored in the array/record of element indicated by this member. Only used
	 * if the indices are being redirected to an integer array or record of
	 * integer. If the index is redirected to a single integer, then this member
	 * is ignored. *
	 *
	 **/
	protected int pos;

	public Index_Redirect() {
		pos = -1;
	}

	public void incr_pos() {
		++pos;
	}

	public void decr_pos() {
		--pos;
	}

	public abstract void add_index(final int p_index);
}
