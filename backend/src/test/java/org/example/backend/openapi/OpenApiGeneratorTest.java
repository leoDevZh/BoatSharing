package org.example.backend.openapi;

import org.example.backend.TestDBConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestDBConfiguration.class)
class OpenApiGeneratorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void generateOpenApiSpec() throws Exception {
        String projectDir = System.getProperty("user.dir");
        Path buildPath = Paths.get(projectDir, "build", "api-docs").normalize();
        Files.createDirectories(buildPath);
        Path outputFile = buildPath.resolve("openapi.json");
        MvcResult result = mockMvc.perform(get("/api-docs")).andReturn();
        String openApiJson = result.getResponse().getContentAsString();
        Files.write(outputFile, openApiJson.getBytes());
    }
}