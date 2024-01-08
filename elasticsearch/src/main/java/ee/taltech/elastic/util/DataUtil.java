package ee.taltech.elastic.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.util.StreamUtils.copyToString;

@NoArgsConstructor(access = PRIVATE)
public class DataUtil {

    @SneakyThrows
    public static <T> T readFromJson(String path, ObjectMapper objectMapper, TypeReference<T> typeReference) {
        return objectMapper.readValue(readResourceAsString(path), typeReference);
    }

    @SneakyThrows
    public static String readResourceAsString(@NonNull String path) {
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            throw new RuntimeException(path);
        }
        try (InputStream stream = resource.getInputStream()) {
            return readFromStream(stream);
        }
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        return copyToString(inputStream, UTF_8);
    }
}
