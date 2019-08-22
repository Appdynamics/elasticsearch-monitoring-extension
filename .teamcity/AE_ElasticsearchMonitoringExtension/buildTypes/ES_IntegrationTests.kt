package AE_ElasticsearchMonitoringExtension.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.dockerCompose
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

import AE_ElasticsearchMonitoringExtension.vcsRoots.elasticsearchmonitoringextensionci
import AE_ElasticsearchMonitoringExtension.buildTypes.AE_ElasticsearchMonitoringExtension_Setup

object ES_IntegrationTests : BuildType({
    uuid = "8ec33935-a8dc-4823-9825-2d2e4699016e"
    name = "Run Integration Tests - Linux"

    vcs {
        root(elasticsearchmonitoringextensionci)
    }
    steps {
        maven {
            goals = "clean verify -DskipITs=false"
            mavenVersion = defaultProvidedVersion()
            jdkHome = "%env.JDK_18%"
        }
    }
    dependencies {
        dependency(AE_ElasticsearchMonitoringExtension_Setup) {
            snapshot {
                runOnSameAgent = true
            }
        }
    }
    triggers {
        vcs { }
    }
})
