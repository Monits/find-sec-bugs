package com.h3xstream.findsecbugs.endpoint;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.OpcodeStack;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;

/**
 * This detector cover the Servlet/HttpServlet API which give access to user input.
 * The developer should not have expectation on those value and should apply validation
 * in most cases.
 */
public class ServletEndpointDetector extends OpcodeStackDetector {

    private static final String GET_PARAMETER_TYPE = "SERVLET_PARAMETER";
    private static final String CONTENT_TYPE = "SERVLET_CONTENT_TYPE";
    private static final String SERVER_NAME_TYPE = "SERVLET_SERVER_NAME";

    private static final String SESSION_ID_TYPE = "SERVLET_SESSION_ID";
    private static final String QUERY_STRING_TYPE = "SERVLET_QUERY_STRING";
    private static final String HEADER_TYPE = "SERVLET_HEADER";
    private static final String HEADER_REFERER_TYPE = "SERVLET_HEADER_REFERER";
    private static final String HEADER_USER_AGENT_TYPE = "SERVLET_HEADER_USER_AGENT";

    private BugReporter bugReporter;

    public ServletEndpointDetector(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void sawOpcode(int seen) {

        //All call to ServletRequest
        //TODO : getProtocol, getRemoteAddr, getRemoteHost, getLocalName
        if (seen == INVOKEINTERFACE && (getClassConstantOperand().equals("javax/servlet/ServletRequest") ||
                getClassConstantOperand().equals("javax/servlet/http/HttpServletRequest"))) {

            //ServletRequest

            if (getNameConstantOperand().equals("getParameter") ||
                    getNameConstantOperand().equals("getParameterValues") ||
                    getNameConstantOperand().equals("getParameterMap") ||
                    getNameConstantOperand().equals("getParameterNames")) {

                bugReporter.reportBug(new BugInstance(this, GET_PARAMETER_TYPE, LOW_PRIORITY) //
                        .addClass(this).addMethod(this).addSourceLine(this)
                        .addString(getNameConstantOperand())); //Passing the method name
            } else if (getNameConstantOperand().equals("getContentType")) {

                bugReporter.reportBug(new BugInstance(this, CONTENT_TYPE, LOW_PRIORITY) //
                        .addClass(this).addMethod(this).addSourceLine(this));
            } else if (getNameConstantOperand().equals("getServerName")) {

                bugReporter.reportBug(new BugInstance(this, SERVER_NAME_TYPE, LOW_PRIORITY) //
                        .addClass(this).addMethod(this).addSourceLine(this));
            }

            //HttpServletRequest

            else if (getNameConstantOperand().equals("getRequestedSessionId")) {
                bugReporter.reportBug(new BugInstance(this, SESSION_ID_TYPE, LOW_PRIORITY) //
                        .addClass(this).addMethod(this).addSourceLine(this));
            } else if (getNameConstantOperand().equals("getQueryString")) {

                bugReporter.reportBug(new BugInstance(this, QUERY_STRING_TYPE, LOW_PRIORITY) //
                        .addClass(this).addMethod(this).addSourceLine(this));
            } else if (getNameConstantOperand().equals("getHeader")) {
                //Extract the value being push..
                OpcodeStack.Item top = stack.getStackItem(0);
                String value = (String) top.getConstant();//Safe see if condition
                if (value.equals("Host")) {

                    bugReporter.reportBug(new BugInstance(this, SERVER_NAME_TYPE, LOW_PRIORITY) //
                            .addClass(this).addMethod(this).addSourceLine(this));
                } else if (value.equalsIgnoreCase("Referer")) {

                    bugReporter.reportBug(new BugInstance(this, HEADER_REFERER_TYPE, LOW_PRIORITY) //
                            .addClass(this).addMethod(this).addSourceLine(this));
                } else if (value.equalsIgnoreCase("User-Agent")) {

                    bugReporter.reportBug(new BugInstance(this, HEADER_USER_AGENT_TYPE, LOW_PRIORITY) //
                            .addClass(this).addMethod(this).addSourceLine(this));
                } else {
                    
                    bugReporter.reportBug(new BugInstance(this, HEADER_TYPE, LOW_PRIORITY) //
                            .addClass(this).addMethod(this).addSourceLine(this));
                }
            }
        }
    }
}