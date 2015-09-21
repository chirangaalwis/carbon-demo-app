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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class FileOutputThread implements Runnable {
    private String fileName;
    private List<String> dataList;

    private static final Logger LOG = LogManager.getLogger(FileOutputThread.class);

    public FileOutputThread(String fileName, List<String> data) {
        this.fileName = fileName;
        dataList = data;
    }

    public void run() {
        try {
            FileOutput output = new FileOutput();
            output.openFile(fileName);
            output.addDataToFile(dataList);
            output.closeFile();
        } catch (Exception exception) {
            String message = "Could not output data to the external file.";
            LOG.error(message, exception);
        }
    }
}