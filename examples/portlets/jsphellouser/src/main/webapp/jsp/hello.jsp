<jsp:useBean id="bean" scope="request" type="org.jboss.portal.portlet.samples.RequestBean"/>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:defineObjects/>

<div class="portlet-section-header">Remember we love  you: ${bean.getName()} %></div>

<portlet:renderURL var="myRenderURL"/>
<br/>
<a href="<%= myRenderURL %>">Ask me again</a>
