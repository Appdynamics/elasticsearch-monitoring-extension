package AE_ElasticsearchMonitoringExtension

import AE_ElasticsearchMonitoringExtension.vcsRoots.*
import AE_ElasticsearchMonitoringExtension.vcsRoots.elasticsearchmonitoringextensionci
import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.Project
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.VersionedSettings
import jetbrains.buildServer.configs.kotlin.v2018_2.projectFeatures.versionedSettings

object Project : Project({
    uuid = "55dbe7cb-182a-4d42-b45c-b0567e2879ae"
    id("AE_ElasticsearchMonitoringExtension")
    parentId("AE")
    name = "Elasticsearch Monitoring Extension"

    vcsRoot(elasticsearchmonitoringextensionci)

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
})
