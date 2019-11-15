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

package AE_ElasticsearchMonitoringExtension

import AE_ElasticsearchMonitoringExtension.buildTypes.*
import AE_ElasticsearchMonitoringExtension.vcsRoots.AE_ElasticsearchMonitoringExtension
import jetbrains.buildServer.configs.kotlin.v2018_2.Project
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.VersionedSettings.BuildSettingsMode.PREFER_SETTINGS_FROM_VCS
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.VersionedSettings.Format.KOTLIN
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.VersionedSettings.Mode.ENABLED
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.versionedSettings

object Project : Project({
    uuid = "666330a9-dfca-4147-8f20-89adbd1f8d30"
    id("AE_ElasticsearchMonitoringExtension")
    parentId("AE")
    name = "AE_ElasticsearchMonitoringExtension"

    vcsRoot(AE_ElasticsearchMonitoringExtension)
    buildType(AE_ElasticsearchMonitoringExtension_Build)
    buildType(AE_ElasticsearchMonitoringExtension_IntegrationTests)
    buildType(AE_ElasticsearchMonitoringExtension_WorkbenchTest)

    features {
        versionedSettings {
            mode = ENABLED
            buildSettingsMode = PREFER_SETTINGS_FROM_VCS
            rootExtId = "${AE_ElasticsearchMonitoringExtension.id}"
            showChanges = true
            settingsFormat = KOTLIN
            storeSecureParamsOutsideOfVcs = true
        }
    }

     buildTypesOrder = arrayListOf(
         AE_ElasticsearchMonitoringExtension_Build,
         AE_ElasticsearchMonitoringExtension_IntegrationTests,
         AE_ElasticsearchMonitoringExtension_WorkbenchTest
    )
})
