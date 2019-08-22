/*
 * Copyright (c) 2019 AppDynamics,Inc.
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

package com.appdynamics.extensions.elasticsearch;

import com.appdynamics.extensions.conf.processor.ConfigProcessor;
import com.appdynamics.extensions.controller.ControllerClient;
import com.appdynamics.extensions.controller.ControllerClientFactory;
import com.appdynamics.extensions.controller.ControllerInfo;
import com.appdynamics.extensions.controller.ControllerInfoFactory;
import com.appdynamics.extensions.controller.ControllerInfoValidator;
import com.appdynamics.extensions.controller.apiservices.ControllerAPIService;
import com.appdynamics.extensions.controller.apiservices.ControllerAPIServiceFactory;
import com.appdynamics.extensions.controller.apiservices.CustomDashboardAPIService;
import com.appdynamics.extensions.controller.apiservices.MetricAPIService;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.yml.YmlReader;
import org.slf4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.appdynamics.extensions.Constants.ENCRYPTION_KEY;

class IntegrationTestUtils {
    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(IntegrationTestUtils.class);
    // change to install dir path to initialize from controller-info.xml
    private static final File INSTALL_DIR = null;
    private static final File CONFIG_FILE = new File("src/integration-test/resources/conf/config.yml");
    private static final String CONTROLLER_HOST = "controllerHost";
    private static final ControllerAPIService controllerApiService = initializeControllerAPIService();

    static MetricAPIService initializeMetricAPIService() {
        if (controllerApiService != null) {
            LOGGER.info("Attempting to setup Metric API Service");
            return controllerApiService.getMetricAPIService();
        } else {
            LOGGER.error("Failed to setup Metric API Service");
            return null;
        }
    }

    static CustomDashboardAPIService initializeCustomDashboardAPIService() {
        if (controllerApiService != null) {
            LOGGER.info("Attempting to setup Dashboard API Service");
            return controllerApiService.getCustomDashboardAPIService();
        } else {
            LOGGER.error("Failed to setup Dashboard API Service");
            return null;
        }
    }

    private static ControllerAPIService initializeControllerAPIService() {
        Map<String, ?> config = ConfigProcessor.process(YmlReader.readFromFileAsMap(CONFIG_FILE));
        if (config == null) {
            LOGGER.error("Unable to process config file");
            return null;
        }
        Map<String, String> controllerInfoMap = new HashMap<>((Map<String, String>) config.get("controllerInfo"));
        controllerInfoMap.put(CONTROLLER_HOST, "localhost");
        controllerInfoMap.put(ENCRYPTION_KEY, (String) config.get(ENCRYPTION_KEY));
        try {
            ControllerInfo controllerInfo = ControllerInfoFactory.initialize(controllerInfoMap, INSTALL_DIR);
            LOGGER.info("Initialized ControllerInfo");
            ControllerInfoValidator controllerInfoValidator = new ControllerInfoValidator(controllerInfo);
            if (controllerInfoValidator.isValidated()) {
                ControllerClient controllerClient = ControllerClientFactory.initialize(controllerInfo,
                        (Map<String, ?>) config.get("connection"), (Map<String, ?>) config.get("proxy"),
                        (String) config.get(ENCRYPTION_KEY));
                LOGGER.debug("Initialized ControllerClient");
                return ControllerAPIServiceFactory.initialize(controllerInfo, controllerClient);
            } else {
                LOGGER.error("Unable to validate config");
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to initialize the Controller API Service");
        }
        return null;
    }
}
