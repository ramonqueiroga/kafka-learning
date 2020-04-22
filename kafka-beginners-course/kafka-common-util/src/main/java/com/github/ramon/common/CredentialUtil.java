package com.github.ramon.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;

public class CredentialUtil {
    private static final String REGEX = "=";

    public static Properties retrieveAccessConfig(String filename) {
        InputStream inputStream = readFile(filename);
        Scanner scanner = new Scanner(inputStream);

        Properties properties = new Properties();
        while(scanner.hasNext()) {
	    String[] splitedLine = scanner.nextLine().split(REGEX);
	    properties.put(splitedLine[0], splitedLine[1]);
        }

        return properties;
    }

    private static InputStream readFile(String filename) {
        try {
	    URL url = ClassLoader.getSystemResources(filename).nextElement();
	    return url.openStream();
        } catch (IOException e) {
	    throw new RuntimeException("Error reading file");
        }
    }

}
