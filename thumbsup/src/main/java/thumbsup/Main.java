/*
 * Copyright (c) 2023, Red Hat Inc. All rights reserved.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package thumbsup;


import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;

import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

// $ rm -rf images/*.jpg target
// $ mvn clean package
// $ java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image -jar target/thumbsup.jar ./images/
// $ jar uf target/thumbsup.jar -C src/main/resources/ META-INF
// $ native-image --no-fallback --link-at-build-time -jar target/thumbsup.jar target/thumbsup
// $ rm -rf images/*.jpg
// $ ./target/thumbsup ./images/
public class Main {
    public static void main(String[] args) throws IOException, FontFormatException {
        Thumbnails.of(Objects.requireNonNull(new File(args[0]).listFiles()))
                .size(50, 50)
                .outputFormat("jpg")
                .toFiles(Rename.PREFIX_DOT_THUMBNAIL);
    }
}
