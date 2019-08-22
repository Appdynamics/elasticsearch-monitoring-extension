package AE_ElasticsearchMonitoringExtension.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.exec

import AE_ElasticsearchMonitoringExtension.vcsRoots.elasticsearchmonitoringextensionci
import AE_ElasticsearchMonitoringExtension.buildTypes.ES_Build

object ES_Setup : BuildType({
    uuid = "e57d4eb5-5f2b-4b60-b2bf-d0fe87a09bec"
    name = "Setup docker containers"

    vcs {
        root(elasticsearchmonitoringextensionci)
    }
    steps {
        exec {
            path = "make"
            arguments = "dockerRun sleep"
        }
    }
    dependencies {
        dependency(ES_Build) {
            snapshot { }
            artifacts {
                artifactRules = """
                    target/ElasticsearchMonitor-*.zip => target
                    """.trimIndent()
            }
        }
    }
    triggers {
        vcs { }
    }
})
