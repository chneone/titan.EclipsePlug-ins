/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.titan.runtime.core.TTCN_Runtime.executorStateEnum;
import org.eclipse.titan.runtime.core.TTCN_Logger.Severity;

/**
 * The class handling parallel mode operations.
 *
 * TODO: lots to implement
 *
 * @author Kristof Szabados
 */
public final class Runtime_Parallel_main {

	private Runtime_Parallel_main() {
		// private constructor to disable accidental instantiation
	}

	//FIXME this is much more complicated
	public static int parallelMain(final String[] argv) {
		int returnValue = 0;

		TitanComponent.self.set(new TitanComponent(TitanComponent.MTC_COMPREF));
		TTCN_Runtime.set_state(executorStateEnum.SINGLE_CONTROLPART);

		if (argv.length != 2) {
			System.out.println("For now only 2 arguments can be passed the host address and port number");
		}

		String local_addr = null;
		final String MC_host = argv[0];
		final int MC_port = Integer.parseInt(argv[1]);
		//FIXME implement call for Module_list ... list_testcases/list_modulepars

		try {
			TTCN_Snapshot.initialize();
			TTCN_Logger.initialize_logger();
			TTCN_Logger.set_start_time();

			try {
				Module_List.pre_init_modules();
				returnValue = TTCN_Runtime.hc_main(local_addr, MC_host, MC_port);
				//FIXME implement missing parts
			} catch (TtcnError error) {
				returnValue = -1;
			}
		} catch (final Throwable e) {
			TTCN_Logger.log_str(Severity.ERROR_UNQUALIFIED, "Fatal error. Aborting execution.");
			final StringWriter error = new StringWriter();
			e.printStackTrace(new PrintWriter(error));

			TTCN_Logger.begin_event(Severity.ERROR_UNQUALIFIED);
			TTCN_Logger.log_event_str("Dynamic test case error: ");
			TTCN_Logger.log_event_str(error.toString());
			TTCN_Logger.end_event();

			returnValue = -1;
		}

		//FIXME implement clears
		TitanComponent.clear_component_names();
		TTCN_EncDec.clear_error();

		TTCN_Logger.terminate_logger();
		TTCN_Snapshot.terminate();

		return returnValue;
	}
}
