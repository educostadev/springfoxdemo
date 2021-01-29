package com.example.springfoxdemo.config;

import static java.text.MessageFormat.format;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.servers.Server;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.web.OpenApiTransformationContext;
import springfox.documentation.oas.web.WebMvcOpenApiTransformationFilter;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ListVendorExtension;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SpringFoxConfig {

  private static final String X_CLIENT_KEY = "x-client-key";
  private static final String HISTORY_TABLE_TEMPLATE =
      "<br>"
          + "<table>\n"
          + "  <thead>\n"
          + "    <tr>\n"
          + "      <th>Version</th>\n"
          + "      <th>Created By</th>\n"
          + "      <th>Date</th>\n"
          + "      <th>Changes</th>\n"
          + "    </tr>\n"
          + "  </thead>\n"
          + "  <tbody>\n"
          + "    <tr>\n"
          + "      <td>{0}</td>\n"
          + "      <td>{1}</td>\n"
          + "      <td>{2}</td>\n"
          + "      <td>{3}</td>\n"
          + "    </tr>\n"
          + "  </tbody>\n"
          + "</table>";

  private final ListVendorExtension<String> extension =
      new ListVendorExtension<>("x-my-test", List.of("myvalue"));

  private Contact createContact() {
    return new Contact("Created by me", "https://mywebsite.com", "contact@email.com");
  }

  private ApiInformation createApiInformation() {
    return ApiInformation.builder()
        .apiName("Geo Location API")
        .description("This API provides the geo location from cities.")
        .build();
  }

  private List<ChangeHistory> createChangeHistories() {
    return List.of(
        ChangeHistory.builder()
            .version("1.0.0")
            .createdBy("Edu")
            .date(LocalDate.of(2021, 2, 1))
            .changeDescription("First version of the API")
            .build());
  }

  private List<Server> createServers() {
    return List.of(
        new Server().description("dev").url("https://myhost.dev/"),
        new Server().description("hml").url("https://myhost.hml"),
        new Server().description("prod").url("https://myhost.prod"));
  }

  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
        .paths(PathSelectors.any())
        .build()
        .securitySchemes(securitySchemes())
        .securityContexts(securityContexts())
        .apiInfo(apiInfo());
  }

  private List<SecurityScheme> securitySchemes() {
    SecurityScheme securityScheme = new ApiKey(X_CLIENT_KEY, "ApiKeyAuth", In.HEADER.name());
    return List.of(securityScheme);
  }

  private List<SecurityReference> defaultAuth() {
    AuthorizationScope[] scopes = {new AuthorizationScope("global", "Read/Write Access")};
    return List.of(new SecurityReference(X_CLIENT_KEY, scopes));
  }

  private List<SecurityContext> securityContexts() {
    SecurityContext securityContext =
        SecurityContext.builder()
            .securityReferences(defaultAuth())
            .forPaths(PathSelectors.any())
            .build();
    return List.of(securityContext);
  }

  private ApiInfo apiInfo() {
    List<ChangeHistory> changeHistories = createChangeHistories();
    return new ApiInfoBuilder()
        .title(createApiInformation().getApiName())
        .description(createDescriptionTable(changeHistories))
        .version(createVersionText(changeHistories))
        .contact(createContact())
        .extensions(List.of(extension))
        .build();
  }

  @Bean
  @SuppressWarnings("unused")
  public CorsFilter corsFilter() {
    var config = new CorsConfiguration();
    config.addAllowedOriginPattern("*");
    config.addAllowedOrigin("*");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }

  private String createVersionText(List<ChangeHistory> changeHistories) {
    ChangeHistory mostRecentChange = getMostRecentChangeFrom(changeHistories);
    return mostRecentChange.getMajorValue() + ";" + mostRecentChange.getDateText();
  }

  private ChangeHistory getMostRecentChangeFrom(List<ChangeHistory> changeHistories) {
    List<ChangeHistory> changeHistoriesTemp = new ArrayList<>(changeHistories);
    changeHistoriesTemp.sort(Comparator.comparing(a -> a.date));
    return changeHistoriesTemp.get(0);
  }

  private String createDescriptionTable(List<ChangeHistory> changeHistories) {
    StringBuilder htmlDescriptionTable = new StringBuilder(createApiInformation().getDescription());
    changeHistories.forEach(
        changeHistory -> htmlDescriptionTable.append(createDescriptionRow(changeHistory)));
    return htmlDescriptionTable.toString();
  }

  private String createDescriptionRow(ChangeHistory changeHistory) {
    return format(
        HISTORY_TABLE_TEMPLATE,
        changeHistory.getVersion(),
        changeHistory.getCreatedBy(),
        changeHistory.getDateText(),
        changeHistory.getChangeDescription());
  }

  @Builder
  @Getter
  static class ApiInformation {
    private final String apiName;
    private final String description;
  }

  @Builder
  @Getter
  static class ChangeHistory {
    private final String version;
    private final String createdBy;
    private final LocalDate date;
    private final String changeDescription;

    private String getDateText() {
      return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private String getMajorValue() {
      return version.substring(0, version.contains(".") ? version.indexOf(".") : version.length());
    }
  }

  @Component
  @SuppressWarnings("unused")
  class SpringfoxSwaggerHostResolver implements WebMvcOpenApiTransformationFilter {

    @Override
    public OpenAPI transform(OpenApiTransformationContext<HttpServletRequest> context) {
      return context.getSpecification().servers(createServers());
    }

    @Override
    public boolean supports(DocumentationType docType) {
      return docType.equals(DocumentationType.OAS_30);
    }
  }
}
