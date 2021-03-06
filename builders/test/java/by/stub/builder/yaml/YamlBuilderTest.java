package by.stub.builder.yaml;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;


/**
 * @author Alexander Zagniotov
 * @since 4/13/13, 12:50 AM
 */
public class YamlBuilderTest {

   @Test
   public void shouldBuildStubbedResponseWithSequenceResponses() throws Exception {
      final String expectedYaml =
         "-  request:\n" +
            "      method: [PUT]\n" +
            "      url: /invoice\n" +
            "\n" +
            "   response:\n" +
            "      -  status: 200\n" +
            "         headers: \n" +
            "            content-type: application/json\n" +
            "         body: OK\n" +
            "\n" +
            "      -  status: 200\n" +
            "         headers: \n" +
            "            content-type: application/json\n" +
            "         body: Still going strong!\n" +
            "\n" +
            "      -  status: 500\n" +
            "         headers: \n" +
            "            content-type: application/json\n" +
            "         body: OMFG!!!\n" +
            "         file: ../../response.json";

      final YamlBuilder yamlBuilder = new YamlBuilder();
      final String actualYaml = yamlBuilder
         .newStubbedRequest()
         .withMethodPut()
         .withUrl("/invoice")
         .newStubbedResponse()
         .withSequenceResponseStatus("200")
         .withSequenceResponseHeaders("content-type", "application/json")
         .withSequenceResponseLiteralBody("OK")
         .withLineBreak()
         .withSequenceResponseStatus("200")
         .withSequenceResponseHeaders("content-type", "application/json")
         .withSequenceResponseLiteralBody("Still going strong!")
         .withLineBreak()
         .withSequenceResponseStatus("500")
         .withSequenceResponseHeaders("content-type", "application/json")
         .withSequenceResponseLiteralBody("OMFG!!!")
         .withSequenceResponseFile("../../response.json")
         .withLineBreak()
         .build();

      assertThat(actualYaml).isEqualTo(expectedYaml);

   }

   @Test
   public void shouldBuildStubbedResponseWithSequenceResponsesFoldedBodyAndFile() throws Exception {
      final String expectedYaml =
         "-  request:\n" +
            "      method: [PUT]\n" +
            "      url: /invoice\n" +
            "\n" +
            "   response:\n" +
            "      -  status: 200\n" +
            "         headers: \n" +
            "            content-type: application/json\n" +
            "         body: OK\n" +
            "\n" +
            "      -  status: 200\n" +
            "         headers: \n" +
            "            content-type: application/json\n" +
            "         body: >\n" +
            "            {\"status\", \"200\"}\n" +
            "\n" +
            "      -  status: 500\n" +
            "         headers: \n" +
            "            content-type: application/json\n" +
            "         file: ../path/to/error.file";

      final YamlBuilder yamlBuilder = new YamlBuilder();
      final String actualYaml = yamlBuilder
         .newStubbedRequest()
         .withMethodPut()
         .withUrl("/invoice")
         .newStubbedResponse()
         .withSequenceResponseStatus("200")
         .withSequenceResponseHeaders("content-type", "application/json")
         .withSequenceResponseLiteralBody("OK")
         .withLineBreak()
         .withSequenceResponseStatus("200")
         .withSequenceResponseHeaders("content-type", "application/json")
         .withSequenceResponseFoldedBody("{\"status\", \"200\"}")
         .withLineBreak()
         .withSequenceResponseStatus("500")
         .withSequenceResponseHeaders("content-type", "application/json")
         .withSequenceResponseFile("../path/to/error.file")
         .withLineBreak()
         .build();

      assertThat(actualYaml).isEqualTo(expectedYaml);

   }

   @Test
   public void shouldBuildStubbedRequestWithMultipleMethods() throws Exception {
      final String expectedYaml =
         "-  request:\n" +
            "      method: [HEAD, GET, PUT]\n" +
            "      url: /invoice\n" +
            "\n" +
            "   response:\n" +
            "      status: 200\n" +
            "      body: OK";

      final YamlBuilder yamlBuilder = new YamlBuilder();
      final String actualYaml = yamlBuilder.
         newStubbedRequest().
         withMethodHead().
         withMethodGet().
         withMethodPut().
         withUrl("/invoice").
         newStubbedResponse().
         withStatus("200").
         withLiteralBody("OK").build();

      assertThat(actualYaml).isEqualTo(expectedYaml);

   }

   @Test
   public void shouldBuildStubbedRequestWithStubbedResponse() throws Exception {
      final String expectedYaml =
         "-  request:\n" +
            "      query:\n" +
            "         status: active\n" +
            "         type: full\n" +
            "      method: [GET]\n" +
            "      url: /invoice\n" +
            "\n" +
            "   response:\n" +
            "      headers:\n" +
            "         content-type: application/json\n" +
            "         pragma: no-cache\n" +
            "      status: 200\n" +
            "      file: ../json/systemtest-body-response-as-file.json";

      final YamlBuilder yamlBuilder = new YamlBuilder();
      final String actualYaml = yamlBuilder.
         newStubbedRequest().
         withQuery("status", "active").
         withQuery("type", "full").
         withMethodGet().
         withUrl("/invoice").
         newStubbedResponse().
         withHeaders("content-type", "application/json").
         withHeaders("pragma", "no-cache").
         withStatus("200").
         withFile("../json/systemtest-body-response-as-file.json").build();

      assertThat(actualYaml).isEqualTo(expectedYaml);

   }


   @Test
   public void shouldBuildStubbedRequestWithMultilineStubbedResponse() throws Exception {

      final String expectedYaml =
         "-  request:\n" +
            "      method: [PUT]\n" +
            "      url: /invoice/123\n" +
            "      headers:\n" +
            "         content-type: application/json\n" +
            "      post: >\n" +
            "         {\"name\": \"milk\", \"description\": \"full\", \"department\": \"savoury\"}\n" +
            "\n" +
            "   response:\n" +
            "      headers:\n" +
            "         content-type: application/json\n" +
            "         pragma: no-cache\n" +
            "      status: 200\n" +
            "      body: >\n" +
            "         {\"id\": \"123\", \"status\": \"updated\"}";

      final YamlBuilder yamlBuilder = new YamlBuilder();
      final String actualYaml = yamlBuilder.
         newStubbedRequest().
         withMethodPut().
         withUrl("/invoice/123").
         withHeaders("content-type", "application/json").
         withFoldedPost("{\"name\": \"milk\", \"description\": \"full\", \"department\": \"savoury\"}").
         newStubbedResponse().
         withHeaders("content-type", "application/json").
         withHeaders("pragma", "no-cache").
         withStatus("200").
         withFoldedBody("{\"id\": \"123\", \"status\": \"updated\"}").build();

      assertThat(actualYaml).isEqualTo(expectedYaml);

   }


   @Test
   public void shouldBuildStubbedRequestWithStubbedResponseWhenBothHaveManyHeaders() throws Exception {
      final String expectedYaml =
         "-  request:\n" +
            "      headers:\n" +
            "         content-type: application/json\n" +
            "         content-language: US-en\n" +
            "         content-length: 30\n" +
            "         authorization: bob:secret\n" +
            "      method: [GET]\n" +
            "      url: /invoice\n" +
            "\n" +
            "   response:\n" +
            "      headers:\n" +
            "         content-type: application/json\n" +
            "         content-language: US-en\n" +
            "         content-length: 30\n" +
            "         pragma: no-cache\n" +
            "         location: /invoice/exit";

      final YamlBuilder yamlBuilder = new YamlBuilder();
      final String actualYaml = yamlBuilder.
         newStubbedRequest().
         withHeaderContentType("application/json").
         withHeaderContentLanguage("US-en").
         withHeaderContentLength("30").
         withHeaderAuthorization("bob:secret").
         withMethodGet().
         withUrl("/invoice").
         newStubbedResponse().
         withHeaderContentType("application/json").
         withHeaderContentLanguage("US-en").
         withHeaderContentLength("30").
         withHeaderPragma("no-cache").
         withHeaderLocation("/invoice/exit").build();

      assertThat(actualYaml).isEqualTo(expectedYaml);

   }
}