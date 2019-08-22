package AE_ElasticsearchMonitoringExtension.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.exec

import AE_ElasticsearchMonitoringExtension.vcsRoots.elasticsearchmonitoringextensionci
import AE_ElasticsearchMonitoringExtension.buildTypes.ES_Setup
import AE_ElasticsearchMonitoringExtension.buildTypes.ES_IntegrationTests

object ES_Stop : BuildType({
    uuid = "59d82e96-0ec9-48de-a0cf-5aee48bb45bc"
    name = "Stop and Remove all Docker Containers"

    vcs {
        root(elasticsearchmonitoringextensionci)
    }
    steps {
        exec {
            path = "make"
            arguments = "dockerStop"
        }
    }
    dependencies {
        dependency(ES_Setup) {
            snapshot{
                runOnSameAgent = true
            }
        }
        dependency(ES_IntegrationTests) {
            snapshot {
                onDependencyFailure = FailureAction.IGNORE
            }
        }
    }
    triggers {
        vcs { }
    }
})