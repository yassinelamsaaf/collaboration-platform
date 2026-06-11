package com.inpt.collaborationplatform.projects.shared;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Predicate;

@Component
public class SlugGenerator {

    public String uniqueSlug(String source, Predicate<String> slugExists) {
        String baseSlug = toSlug(source);
        String candidate = baseSlug;
        int suffix = 2;

        while (slugExists.test(candidate)) {
            candidate = baseSlug + "-" + suffix;
            suffix++;
        }

        return candidate;
    }

    public String toSlug(String source) {
        String normalized = Normalizer.normalize(source, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");

        return normalized.isBlank() ? "item" : normalized;
    }
}
