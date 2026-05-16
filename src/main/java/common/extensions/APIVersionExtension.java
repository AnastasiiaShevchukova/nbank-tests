package common.extensions;

import api.configs.Config;
import api.specs.RequestSpecs;
import common.annotations.APIVersion;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class APIVersionExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        // Шаг 1: проверка, есть ли у теста аннотация APIVersion
        APIVersion annotation = extensionContext.getRequiredTestMethod().getAnnotation(APIVersion.class);
        if (annotation == null) {
            RequestSpecs.setBaseUrl(Config.getProperty("apiBaseUrl"));
            return;
        }

        switch (annotation.value()) {
            case VALIDATION_FIX ->
                    RequestSpecs.setBaseUrl(Config.getProperty("apiBaseUrl"));

            case DATABASE_FIX ->
                    RequestSpecs.setBaseUrl(Config.getProperty("apiBaseUrlWithDb"));
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        RequestSpecs.clearBaseUrl();
    }
}
