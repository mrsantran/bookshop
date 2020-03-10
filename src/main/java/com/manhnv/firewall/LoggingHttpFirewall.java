package com.manhnv.firewall;

//import java.util.logging.Level;
//import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.web.firewall.FirewalledRequest;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.security.web.firewall.StrictHttpFirewall;

/**
 * Overrides the StrictHttpFirewall to log some useful information about blocked
 * requests.
 */
public final class LoggingHttpFirewall extends StrictHttpFirewall {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LogManager.getLogger(LoggingHttpFirewall.class);

	/**
	 * Default constructor.
	 */
	public LoggingHttpFirewall() {
		super();
		return;
	}

	/**
	 * Provides the request object which will be passed through the filter chain.
	 *
	 * @returns A FirewalledRequest (required by the HttpFirewall interface) which
	 *          inconveniently breaks the general contract of ServletFilter because
	 *          we can't upcast this to an HttpServletRequest. This prevents us from
	 *          re-wrapping this using an HttpServletRequestWrapper.
	 * @throws RequestRejectedException if the request should be rejected
	 *                                  immediately.
	 */
	@Override
	public FirewalledRequest getFirewalledRequest(final HttpServletRequest request) throws RequestRejectedException {
		try {
			return super.getFirewalledRequest(request);
		} catch (RequestRejectedException ex) {
			LOGGER.warn("Intercepted RequestBlockedException: Remote Host: " + request.getRemoteHost() + " User Agent: "
								+ request.getHeader("User-Agent") + " Request URL: "
								+ request.getRequestURL().toString());

			// Wrap in a new RequestRejectedException with request metadata and a shallower
			// stack trace.
			throw new RequestRejectedException(ex.getMessage() + ".\n Remote Host: " + request.getRemoteHost()
					+ "\n User Agent: " + request.getHeader("User-Agent") + "\n Request URL: "
					+ request.getRequestURL().toString()) {
				private static final long serialVersionUID = 1L;

				@Override
				public synchronized Throwable fillInStackTrace() {
					return this; // suppress the stack trace.
				}
			};
		}
	}

	/**
	 * Provides the response which will be passed through the filter chain. This
	 * method isn't extensible because the request may already be committed.
	 * Furthermore, this is only invoked for requests that were not blocked, so we
	 * can't control the status or response for blocked requests here.
	 *
	 * @param response The original HttpServletResponse.
	 * @return the original response or a replacement/wrapper.
	 */
	@Override
	public HttpServletResponse getFirewalledResponse(final HttpServletResponse response) {
		// Note: The FirewalledResponse class is not accessible outside the package.
		return super.getFirewalledResponse(response);
	}
}
