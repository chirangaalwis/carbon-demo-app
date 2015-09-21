/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.strategy.miscellaneous.io;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.PatternSyntaxException;

class FileInput {
    // Scanner instance which reads data from the specified file
    private Scanner input;

    private static final Log LOG = LogFactory.getLog(FileInput.class);

    /**
     * initializes the Scanner instance using the name of the file specified by the String
     *
     * @param fileName name of the file to be used with the Scanner instance
     * @throws IOException
     * @throws SecurityException
     * @throws IllegalStateException
     * @throws NoSuchElementException
     */
    public void openFile(String fileName)
            throws IOException, SecurityException, IllegalStateException, NoSuchElementException {
        File outputFile = new File(fileName);
        boolean exists = outputFile.exists();
        if (!exists) {
            LOG.debug(outputFile.getAbsolutePath() + " does not exist.");
            boolean created = outputFile.createNewFile();
            if (created) {
                LOG.debug("New file " + outputFile.getAbsolutePath() + " created.");
            }
        }
        input = new Scanner(Paths.get(fileName));
    }

    /**
     * returns the String data items read from the file
     *
     * @return list of String data items read from the file
     * @throws IllegalStateException
     * @throws NoSuchElementException
     */
    public List<String> readSingletonDataFromFile() throws IllegalStateException, NoSuchElementException {
        List<String> data = new ArrayList<>();
        if (input != null) {
            while (input.hasNextLine()) {
                data.add(input.nextLine());
            }
        }
        return data;
    }

    /**
     * returns the String key-value data items read from the file
     *
     * @return list of String key-value data items read from the file
     * @throws IllegalStateException
     * @throws NoSuchElementException
     * @throws PatternSyntaxException
     */
    public Map<String, String> readKeyValueDataFromFile()
            throws IllegalStateException, NoSuchElementException, PatternSyntaxException {
        Map<String, String> data = new HashMap<>();
        String dataHolder;
        String[] tempKeyValueHolder;
        final int keyIndex = 0;
        final int valueIndex = 1;
        if (input != null) {
            while (input.hasNextLine()) {
                dataHolder = input.nextLine();
                tempKeyValueHolder = dataHolder.split("=");
                data.put(tempKeyValueHolder[keyIndex], tempKeyValueHolder[valueIndex]);
            }
        }
        return data;
    }

    /**
     * closes the Scanner instance if the Scanner instance is not equal to null
     */
    public void closeFile() {
        if (input != null) {
            input.close();
        }
    }
}
