/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.solutions.spannerddl.testUtils;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ReadTestDatafile {

  /**
   * Reads the test data file, parsing out the test titles and data from the file.
   *
   * @return LinkedHashMap of segment name => contents
   */
  public static Map<String, String> readDdlSegmentsFromFile(String filename) throws IOException {
    return readDdlSegmentsFromFile(filename, false);
  }

  /**
   * Reads the test data file, parsing out the test titles and data from the file.
   *
   * @return LinkedHashMap of segment name => contents
   */
  public static Map<String, String> readDdlSegmentsFromFile(String filename, boolean preserveSpace)
      throws IOException {
    File file = new File("src/test/resources/" + filename).getAbsoluteFile();
    LinkedHashMap<String, String> output = new LinkedHashMap<>();

    try (BufferedReader in = Files.newBufferedReader(file.toPath(), UTF_8)) {

      String sectionName = null;
      StringBuilder section = new StringBuilder();
      String rawLine;
      while (null != (rawLine = in.readLine())) {
        String line = preserveSpace ? rawLine : rawLine.replaceAll("#.*", "").trim();
        if (line.isEmpty()) {
          continue;
        }
        if (line.startsWith("==")) {
          // new section
          if (sectionName != null) {
            // add closed section.
            output.put(sectionName, section.length() > 0 ? section.toString() : null);
          }
          sectionName = line;
          section = new StringBuilder();
          continue;
        } else if (sectionName == null) {
          throw new IOException("no section name before first statement");
        }
        section.append(line).append('\n');
      }
      // Check if there is an unclosed last section
      if (section.length() > 0) {
        // add last section
        throw new IOException("Missing section border '==' at end of file " + filename);
      }
      return output;
    }
  }
}
