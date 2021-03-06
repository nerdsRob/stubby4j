/*
HTTP stub server written in Java with embedded Jetty

Copyright (C) 2012 Alexander Zagniotov, Isa Goksu and Eric Mrak

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package by.stub.handlers;

import by.stub.database.StubbedDataManager;
import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import by.stub.server.JettyContext;
import by.stub.utils.ConsoleUtils;
import by.stub.utils.HandlerUtils;
import by.stub.utils.ReflectionUtils;
import by.stub.utils.StringUtils;
import by.stub.yaml.stubs.StubHttpLifecycle;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpSchemes;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public final class StatusHandler extends AbstractHandler {

   private static final String CSS_CLASS_HIGHLIGHTABLE = "highlightable";
   private static final String CSS_CLASS_NO_HIGHLIGHTABLE = "no-highlightable";
   private static final String HTML_TABLE_ROW_TEMPLATE = "<tr><td width='200px' valign='top' align='left'>%s</td><td class='%s' align='left'>%s</td></tr>";
   private static final List<String> highlightableProperties = Collections.unmodifiableList(Arrays.asList("file", "body", "post"));
   private final StubbedDataManager stubbedDataManager;
   private final JettyContext jettyContext;

   public StatusHandler(final JettyContext newContext, final StubbedDataManager newStubbedDataManager) {
      this.jettyContext = newContext;
      this.stubbedDataManager = newStubbedDataManager;
   }

   @Override
   public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
      ConsoleUtils.logIncomingRequest(request);

      final HttpServletResponseWithGetStatus wrapper = new HttpServletResponseWithGetStatus(response);

      baseRequest.setHandled(true);
      wrapper.setContentType(MimeTypes.TEXT_HTML_UTF_8);
      wrapper.setStatus(HttpStatus.OK_200);
      wrapper.setHeader(HttpHeaders.SERVER, HandlerUtils.constructHeaderServerName());

      try {
         wrapper.getWriter().println(getConfigDataPresentation());
      } catch (final Exception ex) {
         HandlerUtils.configureErrorResponse(wrapper, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
      }

      ConsoleUtils.logOutgoingResponse(request.getRequestURI(), wrapper);
   }

   private String getConfigDataPresentation() throws Exception {

      final List<StubHttpLifecycle> stubHttpLifecycles = stubbedDataManager.getStubHttpLifecycles();

      final StringBuilder builder = new StringBuilder();
      builder.append(buildSystemStatusHtmlTable());
      builder.append("<br /><br />");

      final String htmlTemplateContent = HandlerUtils.getHtmlResourceByName("snippet_request_response_tables");

      for (int cycleIndex = 0; cycleIndex < stubHttpLifecycles.size(); cycleIndex ++) {

         final StubHttpLifecycle stubHttpLifecycle = stubHttpLifecycles.get(cycleIndex);
         final String resourceId = stubHttpLifecycle.getResourceId();
         final StubRequest stubRequest = stubHttpLifecycle.getRequest();
         builder.append(buildPageBodyHtml(resourceId, htmlTemplateContent, "request", ReflectionUtils.getProperties(stubRequest)));

         final List<StubResponse> allResponses = stubHttpLifecycle.getAllResponses();
         for (int sequenceId = 0; sequenceId < allResponses.size(); sequenceId++) {

            String responseTableTitle = (allResponses.size() == 1 ? "response" : String.format("response/%s", sequenceId));

            final StubResponse stubResponse = allResponses.get(sequenceId);
            final Map<String, String> stubResponseProperties = ReflectionUtils.getProperties(stubResponse);
            builder.append(buildPageBodyHtml(resourceId, htmlTemplateContent, responseTableTitle, stubResponseProperties));
         }

         builder.append("<br /><br />");
      }

      return HandlerUtils.populateHtmlTemplate("status", stubHttpLifecycles.size(), builder.toString());
   }

   private String buildSystemStatusHtmlTable() throws Exception {

      final StringBuilder builder = new StringBuilder();

      final String host = jettyContext.getHost();
      final int clientPort = jettyContext.getStubsPort();
      final int sslPort = jettyContext.getStubsSslPort();
      final int adminPort = jettyContext.getAdminPort();

      builder.append(populateTableRowTemplate("CLIENT PORT", CSS_CLASS_NO_HIGHLIGHTABLE, clientPort));
      builder.append(populateTableRowTemplate("ADMIN PORT", CSS_CLASS_NO_HIGHLIGHTABLE, adminPort));
      builder.append(populateTableRowTemplate("SSL PORT", CSS_CLASS_NO_HIGHLIGHTABLE, sslPort));
      builder.append(populateTableRowTemplate("HOST", CSS_CLASS_NO_HIGHLIGHTABLE, host));
      builder.append(populateTableRowTemplate("CONFIGURATION", CSS_CLASS_NO_HIGHLIGHTABLE, stubbedDataManager.getYamlAbsolutePath()));

      final String endpointRegistration = HandlerUtils.linkifyRequestUrl(HttpSchemes.HTTP,
         AdminHandler.ADMIN_ROOT, host, adminPort);
      builder.append(populateTableRowTemplate("NEW STUB DATA POST URI", CSS_CLASS_NO_HIGHLIGHTABLE, endpointRegistration));

      final String systemStatusTable = HandlerUtils.getHtmlResourceByName("snippet_system_status_table");

      return String.format(systemStatusTable, builder.toString());
   }

   private String buildPageBodyHtml(final String resourceId, final String htmlTemplateContent, final String tableName, final Map<String, String> stubObjectProperties) throws Exception {
      final StringBuilder builder = new StringBuilder();

      for (final Map.Entry<String, String> keyValue : stubObjectProperties.entrySet()) {
         final String value = keyValue.getValue();
         final String key = keyValue.getKey();

         if (!StringUtils.isSet(value)) {
            continue;
         }

         builder.append(constructHtmlTableRow(resourceId, tableName, key, value));
      }
      return String.format(htmlTemplateContent, tableName, builder.toString());
   }

   private String constructHtmlTableRow(final String resourceId, final String tableName, final String fieldName, final String value) {
      final String escapedValue = StringUtils.escapeHtmlEntities(value);

      if (highlightableProperties.contains(fieldName)) {
         if (escapedValue.equals(StringUtils.NOT_PROVIDED)) {
            return populateTableRowTemplate(StringUtils.toUpper(fieldName), CSS_CLASS_NO_HIGHLIGHTABLE, escapedValue);
         }
         final String ajaxifiedLinkToResource =
            String.format("&nbsp;<strong><a class='ajaxable' href='/ajax/resource/%s/%s/%s'>[Click to View]</a></strong>&nbsp;",
               resourceId, tableName, fieldName);
         return populateTableRowTemplate(StringUtils.toUpper(fieldName), CSS_CLASS_HIGHLIGHTABLE, ajaxifiedLinkToResource);
      }

      if (fieldName.equals("url")) {
         final String linkifiedUrl = HandlerUtils.linkifyRequestUrl(HttpSchemes.HTTP, escapedValue, jettyContext.getHost(), jettyContext.getStubsPort());
         final String linkifiedSslUrl = HandlerUtils.linkifyRequestUrl(HttpSchemes.HTTPS, escapedValue, jettyContext.getHost(), jettyContext.getStubsSslPort());

         final String tableRowWithUrl = populateTableRowTemplate(StringUtils.toUpper(fieldName), CSS_CLASS_NO_HIGHLIGHTABLE, linkifiedUrl);
         final String tableRowWithSslUrl = populateTableRowTemplate("SSL " + StringUtils.toUpper(fieldName), CSS_CLASS_NO_HIGHLIGHTABLE, linkifiedSslUrl);

         return String.format("%s%s", tableRowWithUrl, tableRowWithSslUrl);
      }

      return populateTableRowTemplate(StringUtils.toUpper(fieldName), CSS_CLASS_NO_HIGHLIGHTABLE, escapedValue);
   }

   private String populateTableRowTemplate(final Object... tokens) {
      return String.format(HTML_TABLE_ROW_TEMPLATE, tokens);
   }
}