package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Утилитарный класс для извлечения плоьзовательской конфигурации из файла.
 */
public class ConfigFileReader {

    /**
     * Начинает чтение конфигурационного файла. Инициализирует системные переменные значениями, указанными в файле.
     *
     * @param configFileName относительный путь до конфигурационного файла. Ожидается, что входной файл содержит строки
     *                       в виде ключ-значения, разделенных знаком "=". Пустые строки игнорируются.
     * @return объект Properties с вычитанными значениями конфигурации вида ключ-значение
     * @throws IOException
     */
    public static Properties read(String configFileName) throws IOException {
        ClassLoader cl = ConfigFileReader.class.getClassLoader();
        Properties properties = new Properties();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(cl.getResourceAsStream(configFileName),
                StandardCharsets.UTF_8))) {
            while (br.ready()) {
                String inputLine = br.readLine();

                if (inputLine.isEmpty() || inputLine.isBlank()) {
                    continue;
                }

                String[] propertyLine = inputLine.split("=");
                String key = propertyLine[0];
                String value = propertyLine[1];

                System.setProperty(key, value);
                properties.setProperty(key, value);
            }
        }

        return properties;
    }
}
