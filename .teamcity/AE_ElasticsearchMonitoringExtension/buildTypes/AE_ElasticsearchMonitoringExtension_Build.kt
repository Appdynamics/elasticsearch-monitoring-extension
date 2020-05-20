package AE_ElasticsearchMonitoringExtension.buildTypes

import AE_ElasticsearchMonitoringExtension.publishCommitStatus
import AE_ElasticsearchMonitoringExtension.vcsRoots.AE_ElasticsearchMonitoringExtension
import AE_ElasticsearchMonitoringExtension.withDefaults
import jetbrains.buildServer.configs.kotlin.v2018_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.schedule

object AE_ElasticsearchMonitoringExtension_Build : BuildType({
    uuid = "7347eee0-b25b-422a-9843-3fc233d79a63"
    name = "Elasticsearch Monitoring Extension Build"

    withDefaults()

    steps {
        maven {
            goals = "clean install"
            mavenVersion = defaultProvidedVersion()
            jdkHome = "%env.JDK_18%"
            userSettingsSelection = "teamcity-settings"
        }
    }

    triggers { 
        vcs { 
         }
        schedule {
            schedulingPolicy = cron {
                hours = "4"
            }
            branchFilter = "+:master"
            triggerBuild = always()
            withPendingChangesOnly = false
            param("revisionRule", "lastFinished")
            param("dayOfWeek", "SUN-SAT")
        }
     }

    artifactRules = """
    +:target/ElasticsearchMonitor-*.zip => target/
""".trimIndent()

    publishCommitStatus()
})
