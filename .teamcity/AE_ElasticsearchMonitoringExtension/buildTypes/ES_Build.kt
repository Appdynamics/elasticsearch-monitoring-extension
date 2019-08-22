package AE_ElasticsearchMonitoringExtension.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.exec
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

import AE_ElasticsearchMonitoringExtension.vcsRoots.elasticsearchmonitoringextensionci

object ES_Build : BuildType({
    uuid = "44e8a107-6078-48ac-bc49-e03591db8207"
    name = "Run Unit Tests and Build Extension"

    vcs {
        root(elasticsearchmonitoringextensionci)
    }
    steps {
        maven {
            goals = "clean install"
            mavenVersion = defaultProvidedVersion()
            jdkHome = "%env.JDK_18%"
        }
    }
    triggers {
        vcs { }
    }
    artifactRules = """
       +:target/ElasticsearchMonitor-*.zip => target
    """.trimIndent()
})
