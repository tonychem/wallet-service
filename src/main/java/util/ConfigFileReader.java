package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigFileReader {
    public static void read(String configFileName) throws IOException {
        ClassLoader cl = ConfigFileReader.class.getClassLoader();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(cl.getResourceAsStream(configFileName),
                StandardCharsets.UTF_8))) {
            while (br.ready()) {
                String[] propertyLine = br.readLine().split("=");
                String key = propertyLine[0];
                String value = propertyLine[1];
                System.setProperty(key, value);
            }
        }
    }
}
