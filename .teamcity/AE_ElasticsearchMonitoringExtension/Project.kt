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
