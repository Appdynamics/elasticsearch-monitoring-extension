package AE_ElasticsearchMonitoringExtension

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.Project
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.VersionedSettings
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.versionedSettings

import AE_ElasticsearchMonitoringExtension.vcsRoots.elasticsearchmonitoringextensionci
import AE_ElasticsearchMonitoringExtension.buildTypes.ES_Build
import AE_ElasticsearchMonitoringExtension.buildTypes.ES_IntegrationTests
import AE_ElasticsearchMonitoringExtension.buildTypes.ES_Setup
import AE_ElasticsearchMonitoringExtension.buildTypes.ES_Stop

object Project : Project({
    uuid = "55dbe7cb-182a-4d42-b45c-b0567e2879ae"
    id("AE_ElasticsearchMonitoringExtension")
    parentId("AE")
    name = "Elasticsearch Monitoring Extension"

    vcsRoot(elasticsearchmonitoringextensionci)
    buildTypes(ES_Build)
    buildTypes(ES_IntegrationTests)
    buildTypes(ES_Setup)
    buildTypes(ES_Stop)

    features {
        versionedSettings {
            id = "PROJECT_EXT_2"
            mode = VersionedSettings.Mode.ENABLED
            buildSettingsMode = VersionedSettings.BuildSettingsMode.PREFER_SETTINGS_FROM_VCS
            rootExtId = "${elasticsearchmonitoringextensionci.id}"
            showChanges = false
            settingsFormat = VersionedSettings.Format.KOTLIN
            storeSecureParamsOutsideOfVcs = true
        }
    }

    buildTypesOrder = arrayListOf(ES_Build, ES_Setup, ES_IntegrationTests, ES_Stop)
})
