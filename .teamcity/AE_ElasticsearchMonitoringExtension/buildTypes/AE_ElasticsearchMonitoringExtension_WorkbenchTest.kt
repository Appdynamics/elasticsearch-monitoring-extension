package AE_ElasticsearchMonitoringExtension.buildTypes

import AE_ElasticsearchMonitoringExtension.publishCommitStatus
import AE_ExtensionStarter.triggerAfter
import AE_ElasticsearchMonitoringExtension.vcsRoots.AE_ElasticsearchMonitoringExtension
import AE_ElasticsearchMonitoringExtension.withDefaults
import jetbrains.buildServer.configs.kotlin.v2018_2.BuildStep
import jetbrains.buildServer.configs.kotlin.v2018_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2018_2.FailureAction
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.exec
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

object AE_ElasticsearchMonitoringExtension_WorkbenchTest : BuildType({
    uuid = "bfa6cbf6-fa68-4543-a499-dff0e1306b93"
    name = "Test Workbench mode"

    withDefaults()

    steps {
        exec {
            path = "make"
            arguments = "workbenchTest"
        }
        exec {
            executionMode = BuildStep.ExecutionMode.ALWAYS
            path = "make"
            arguments = "dockerClean"
        }
    }

    triggers {
        vcs {
        }
    }

    dependencies {
        dependency(AE_ElasticsearchMonitoringExtension_Build) {
            snapshot {
                onDependencyFailure = FailureAction.FAIL_TO_START
            }
            artifacts {
                artifactRules = """
                +:target/ElasticsearchMonitor-*.zip => target/
            """.trimIndent()
            }
        }
    }

    publishCommitStatus()

    triggerAfter(AE_ExtensionStarter_Build)
})
