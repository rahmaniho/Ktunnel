# آخرین خروجی ناموفق CI

- اجرا: https://github.com/rahmaniho/Ktunnel/actions/runs/41765886354
- کامیت: `d88f2c1ff593aebe1ff3cee4b3e01a909aff1244`
- زمان: 2026-11-04T03:19:05Z

## خطاهای کامپایلر

```
> Task :app:lintDebug FAILED
* What went wrong:
Execution failed for task ':app:lintDebug'.
org.gradle.api.tasks.TaskExecutionException: Execution failed for task ':app:lintDebug'.
Caused by: java.lang.RuntimeException: Lint found errors in the project; aborting build.
BUILD FAILED in 53s
```

## تست‌های ناموفق

```
<testcase name="round trip preserves every field" classname="ir.karensoft.karentunnel.data.secure.FileSecureConfigStoreTest"
<testcase name="file on disk does not contain the private key" classname="ir.karensoft.karentunnel.data.secure.FileSecureConfigStoreTest"
<testcase name="load of missing config fails cleanly" classname="ir.karensoft.karentunnel.data.secure.FileSecureConfigStoreTest"
<testcase name="list returns saved ids sorted" classname="ir.karensoft.karentunnel.data.secure.FileSecureConfigStoreTest"
<testcase name="tampered file is reported as failure not garbage" classname="ir.karensoft.karentunnel.data.secure.FileSecureConfigStoreTest"
<testcase name="delete removes the config" classname="ir.karensoft.karentunnel.data.secure.FileSecureConfigStoreTest"
<testcase name="server id with path characters cannot escape the directory" classname="ir.karensoft.karentunnel.data.secure.FileSecureConfigStoreTest"
<testcase name="wipeAll removes every config" classname="ir.karensoft.karentunnel.data.secure.FileSecureConfigStoreTest"
<testcase name="failed save does not leave a temp file behind" classname="ir.karensoft.karentunnel.data.secure.FileSecureConfigStoreTest"
<testcase name="port out of range is refused" classname="ir.karensoft.karentunnel.core.model.ConfigNormalizerTest"
<testcase name="malformed private key is refused" classname="ir.karensoft.karentunnel.core.model.ConfigNormalizerTest"
<testcase name="canonical base64 key of 32 bytes is accepted" classname="ir.karensoft.karentunnel.core.model.ConfigNormalizerTest"
<testcase name="non-literal dns entry is refused" classname="ir.karensoft.karentunnel.core.model.ConfigNormalizerTest"
<testcase name="keys with invalid characters are rejected" classname="ir.karensoft.karentunnel.core.model.ConfigNormalizerTest"
<testcase name="ipv6 only dns is refused when ipv6 cannot be tunneled" classname="ir.karensoft.karentunnel.core.model.ConfigNormalizerTest"
<testcase name="key equal to peer key is refused" classname="ir.karensoft.karentunnel.core.model.ConfigNormalizerTest"
<testcase name="secret is redacted in toString" classname="ir.karensoft.karentunnel.core.model.ConfigNormalizerTest"
<testcase name="mtu outside range is refused" classname="ir.karensoft.karentunnel.core.model.ConfigNormalizerTest"
<testcase name="dual stack config tunnels IPv6" classname="ir.karensoft.karentunnel.core.model.ConfigNormalizerTest"
<testcase name="ipv4 only config excludes IPv6" classname="ir.karensoft.karentunnel.core.model.ConfigNormalizerTest"
<testcase name="keys with wrong length are rejected" classname="ir.karensoft.karentunnel.core.model.ConfigNormalizerTest"
<testcase name="missing dns is refused" classname="ir.karensoft.karentunnel.core.model.ConfigNormalizerTest"
<testcase name="endpoint without port is refused" classname="ir.karensoft.karentunnel.core.model.ConfigNormalizerTest"
<testcase name="ipv6 literal endpoint is parsed" classname="ir.karensoft.karentunnel.core.model.ConfigNormalizerTest"
<testcase name="ipv6 interface address without default route is refused" classname="ir.karensoft.karentunnel.core.model.ConfigNormalizerTest"
<testcase name="default mtu is applied" classname="ir.karensoft.karentunnel.core.model.ConfigNormalizerTest"
<testcase name="blank dns entries do not count as dns" classname="ir.karensoft.karentunnel.core.model.ConfigNormalizerTest"
<testcase name="never produces TUNNELED without a v6 default route" classname="ir.karensoft.karentunnel.core.model.ConfigNormalizerTest"
<testcase name="too many dns servers is refused" classname="ir.karensoft.karentunnel.core.model.ConfigNormalizerTest"
<testcase name="ipv6 entries are stripped when policy is excluded" classname="ir.karensoft.karentunnel.core.model.ConfigNormalizerTest"
<testcase name="can always disconnect from any state" classname="ir.karensoft.karentunnel.core.connection.ConnectionStateMachineTest"
<testcase name="retry stops at max attempts" classname="ir.karensoft.karentunnel.core.connection.ConnectionStateMachineTest"
<testcase name="backoff grows and is capped" classname="ir.karensoft.karentunnel.core.connection.ConnectionStateMachineTest"
<testcase name="no connection without system vpn network" classname="ir.karensoft.karentunnel.core.connection.ConnectionStateMachineTest"
<testcase name="no connection without a handshake" classname="ir.karensoft.karentunnel.core.connection.ConnectionStateMachineTest"
<testcase name="failure never maps to connected" classname="ir.karensoft.karentunnel.core.connection.ConnectionStateMachineTest"
<testcase name="jitter stays within bounds" classname="ir.karensoft.karentunnel.core.connection.ConnectionStateMachineTest"
<testcase name="connected requires all three proofs" classname="ir.karensoft.karentunnel.core.connection.ConnectionStateMachineTest"
<testcase name="no connection when handshake is stale" classname="ir.karensoft.karentunnel.core.connection.ConnectionStateMachineTest"
<testcase name="no network is not retried until the network returns" classname="ir.karensoft.karentunnel.core.connection.ConnectionStateMachineTest"
<testcase name="attempt cap cannot be configured into an infinite loop" classname="ir.karensoft.karentunnel.core.connection.ConnectionStateMachineTest"
<testcase name="cannot jump from disconnected straight to connected" classname="ir.karensoft.karentunnel.core.connection.ConnectionStateMachineTest"
<testcase name="degraded is treated as protected but is not success" classname="ir.karensoft.karentunnel.core.connection.ConnectionStateMachineTest"
<testcase name="degraded reason distinguishes missing from stale handshake" classname="ir.karensoft.karentunnel.core.connection.ConnectionStateMachineTest"
<testcase name="no connection without backend up" classname="ir.karensoft.karentunnel.core.connection.ConnectionStateMachineTest"
<testcase name="permission errors are never retried automatically" classname="ir.karensoft.karentunnel.core.connection.ConnectionStateMachineTest"
<testcase name="dns failure with dead tcp stays unknown" classname="ir.karensoft.karentunnel.diagnostics.ProbeEngineTest"
<testcase name="healthy probes never produce a filtering claim" classname="ir.karensoft.karentunnel.diagnostics.ProbeEngineTest"
<testcase name="dns failure alone is only probable" classname="ir.karensoft.karentunnel.diagnostics.ProbeEngineTest"
<testcase name="report headline never says filtering confirmed" classname="ir.karensoft.karentunnel.diagnostics.ProbeEngineTest"
<testcase name="run uses injected probes and records evidence" classname="ir.karensoft.karentunnel.diagnostics.ProbeEngineTest"
<testcase name="unreachable endpoint is probable with low confidence" classname="ir.karensoft.karentunnel.diagnostics.ProbeEngineTest"
<testcase name="permission failure goes terminal without retry" classname="ir.karensoft.karentunnel.vpn.connection.ConnectionManagerTest"
<testcase name="missing system vpn network prevents connected" classname="ir.karensoft.karentunnel.vpn.connection.ConnectionManagerTest"
<testcase name="stale handshake is degraded" classname="ir.karensoft.karentunnel.vpn.connection.ConnectionManagerTest"
<testcase name="health loop demotes a connected tunnel when the handshake goes stale" classname="ir.karensoft.karentunnel.vpn.connection.ConnectionManagerTest"
<testcase name="becomes connected only when all evidence is present" classname="ir.karensoft.karentunnel.vpn.connection.ConnectionManagerTest"
<testcase name="transient failure retries a bounded number of times" classname="ir.karensoft.karentunnel.vpn.connection.ConnectionManagerTest"
<testcase name="tunnel up without handshake is degraded not connected" classname="ir.karensoft.karentunnel.vpn.connection.ConnectionManagerTest"
<testcase name="external teardown disconnects safely" classname="ir.karensoft.karentunnel.vpn.connection.ConnectionManagerTest"

--- failure messages ---

--- stdout / assertion traces ---
```

## خطاهای Lint

```
message="Not targeting the latest versions of Android; compatibility modes apply. Consider testing and updating this version. Consult the `android.os.Build.VERSION_CODES` javadoc for details."
message="Not targeting the latest versions of Android; compatibility modes apply. Consider testing and updating this version. Consult the `android.os.Build.VERSION_CODES` javadoc for details."
message="foregroundServiceType:systemExempted requires permission:[android.permission.FOREGROUND_SERVICE_SYSTEM_EXEMPTED] AND any permission in list:[android.permission.SCHEDULE_EXACT_ALARM, android.permission.USE_EXACT_ALARM]"
message="A newer version of Gradle than 8.13 is available: 8.14.6"
message="A newer version of com.android.application than 8.13.2 is available: 9.5.0"
message="A newer version of com.android.library than 8.13.2 is available: 9.5.0"
message="A newer version of androidx.core:core-ktx than 1.15.0 is available: 1.19.1"
message="A newer version of androidx.activity:activity-compose than 1.10.1 is available: 1.13.0"
message="A newer version of androidx.lifecycle:lifecycle-runtime-compose than 2.9.1 is available: 2.11.0"
message="A newer version of androidx.lifecycle:lifecycle-runtime-ktx than 2.9.1 is available: 2.11.0"
message="A newer version of androidx.lifecycle:lifecycle-viewmodel-compose than 2.9.1 is available: 2.11.0"
message="A newer version of androidx.compose:compose-bom than 2025.06.01 is available: 2026.09.00"
message="A newer version of androidx.datastore:datastore-preferences than 1.1.7 is available: 1.3.0"
message="A newer version of androidx.annotation:annotation than 1.9.1 is available: 1.11.0"
message="A newer version of androidx.collection:collection than 1.5.0 is available: 1.7.0"
message="A newer version of androidx.test.ext:junit than 1.2.1 is available: 1.3.0"
message="A newer version of androidx.test.espresso:espresso-core than 3.6.1 is available: 3.7.0"
message="A newer version of org.jetbrains.kotlin.android than 2.1.20 is available: 2.4.21"
message="A newer version of org.jetbrains.kotlin.plugin.compose than 2.1.20 is available: 2.4.21"
message="A newer version of org.jetbrains.kotlin.plugin.serialization than 2.1.20 is available: 2.4.21"
message="A newer version of org.jetbrains.kotlinx:kotlinx-coroutines-android than 1.10.2 is available: 1.11.0"
message="A newer version of org.jetbrains.kotlinx:kotlinx-coroutines-core than 1.10.2 is available: 1.11.0"
message="A newer version of org.jetbrains.kotlinx:kotlinx-serialization-json than 1.8.1 is available: 1.12.0"
message="A newer version of com.google.truth:truth than 1.4.4 is available: 1.4.5"
message="A newer version of io.mockk:mockk than 1.14.2 is available: 1.14.11"
message="A newer version of org.jetbrains.kotlinx:kotlinx-coroutines-test than 1.10.2 is available: 1.11.0"
message="A newer version of org.robolectric:robolectric than 4.14.1 is available: 4.17"
message="A newer version of app.cash.turbine:turbine than 1.2.0 is available: 1.2.1"
message="Unnecessary; `SDK_INT` is always >= 26"
message="This folder configuration (`v26`) is unnecessary; `minSdkVersion` is 26. Merge all the resources in this folder into `mipmap-anydpi`."
message="The resource `R.string.about_version` appears to be unused"
message="The resource `R.string.about_backend` appears to be unused"
message="The resource `R.string.about_licenses` appears to be unused"
message="The resource `R.string.about_security_note` appears to be unused"
message="The application adaptive icon is missing a monochrome tag"
==> ./data/build/reports/lint-results-debug.txt <==
No issues found.

==> ./core/build/reports/lint-results-debug.txt <==
No issues found.

==> ./app/build/reports/lint-results-debug.txt <==
/home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/app/build.gradle.kts:15: Warning: Not targeting the latest versions of Android; compatibility modes apply. Consider testing and updating this version. Consult the android.os.Build.VERSION_CODES javadoc for details. [OldTargetApi]
        targetSdk = libs.versions.targetSdk.get().toInt()
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
/home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/gradle/libs.versions.toml:14: Warning: Not targeting the latest versions of Android; compatibility modes apply. Consider testing and updating this version. Consult the android.os.Build.VERSION_CODES javadoc for details. [OldTargetApi]
targetSdk = "35"
            ~~~~

   Explanation for issues of type "OldTargetApi":
   When your application or sdk runs on a version of Android that is more
   recent than your targetSdkVersion specifies that it has been tested with,
   various compatibility modes kick in. This ensures that your application
   continues to work, but it may look out of place. For example, if the
   targetSdkVersion is less than 14, your app may get an option button in the
   UI.

   To fix this issue, set the targetSdkVersion to the highest available value.
   Then test your app to make sure everything works correctly. You may want to
   consult the compatibility notes to see what changes apply to each version
   you are adding support for:
   https://developer.android.com/reference/android/os/Build.VERSION_CODES.html
   as well as follow this guide:
   https://developer.android.com/distribute/best-practices/develop/target-sdk.
   html

   https://developer.android.com/distribute/best-practices/develop/target-sdk.html

/home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/app/src/main/AndroidManifest.xml:58: Error: foregroundServiceType:systemExempted requires permission:[android.permission.FOREGROUND_SERVICE_SYSTEM_EXEMPTED] AND any permission in list:[android.permission.SCHEDULE_EXACT_ALARM, android.permission.USE_EXACT_ALARM] [ForegroundServicePermission]
        <service
        ^

   Explanation for issues of type "ForegroundServicePermission":
   For targetSdkVersion >= 34, each foregroundServiceType listed in the
   <service> element requires specific sets of permissions to be declared in
   the manifest. If permissions are missing, then when the foreground service
   is started with a foregroundServiceType that has missing permissions, a
   SecurityException will be thrown.

/home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/gradle/wrapper/gradle-wrapper.properties:3: Warning: A newer version of Gradle than 8.13 is available: 8.14.6 [AndroidGradlePluginVersion]
distributionUrl=https\://services.gradle.org/distributions/gradle-8.13-bin.zip
                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
/home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/gradle/libs.versions.toml:7: Warning: A newer version of com.android.application than 8.13.2 is available: 9.5.0 [AndroidGradlePluginVersion]
agp = "8.13.2"
      ~~~~~~~~
/home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/gradle/libs.versions.toml:7: Warning: A newer version of com.android.library than 8.13.2 is available: 9.5.0 [AndroidGradlePluginVersion]
agp = "8.13.2"
      ~~~~~~~~

   Explanation for issues of type "AndroidGradlePluginVersion":
   This detector looks for usage of the Android Gradle Plugin where the
   version you are using is not the current stable release. Using older
   versions is fine, and there are cases where you deliberately want to stick
   with an older version. However, you may simply not be aware that a more
   recent version is available, and that is what this lint check helps find.

/home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/gradle/libs.versions.toml:24: Warning: A newer version of androidx.core:core-ktx than 1.15.0 is available: 1.19.1 [GradleDependency]
coreKtx = "1.15.0"
          ~~~~~~~~
/home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/gradle/libs.versions.toml:25: Warning: A newer version of androidx.activity:activity-compose than 1.10.1 is available: 1.13.0 [GradleDependency]
activityCompose = "1.10.1"
                  ~~~~~~~~

==> ./diagnostics/build/reports/lint-results-debug.txt <==
No issues found.

==> ./vpn/build/reports/lint-results-debug.txt <==
No issues found.
```

## ۱۲۰ خط آخر خروجی

```
	at org.gradle.api.internal.project.taskfactory.StandardTaskAction.execute(StandardTaskAction.java:51)
	at org.gradle.api.internal.project.taskfactory.StandardTaskAction.execute(StandardTaskAction.java:29)
	at org.gradle.api.internal.tasks.execution.TaskExecution$3.run(TaskExecution.java:244)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$1.execute(DefaultBuildOperationRunner.java:30)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$1.execute(DefaultBuildOperationRunner.java:27)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:67)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:60)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:167)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:60)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.run(DefaultBuildOperationRunner.java:48)
	at org.gradle.api.internal.tasks.execution.TaskExecution.executeAction(TaskExecution.java:229)
	at org.gradle.api.internal.tasks.execution.TaskExecution.executeActions(TaskExecution.java:212)
	at org.gradle.api.internal.tasks.execution.TaskExecution.executeWithPreviousOutputFiles(TaskExecution.java:195)
	at org.gradle.api.internal.tasks.execution.TaskExecution.execute(TaskExecution.java:162)
	at org.gradle.internal.execution.steps.ExecuteStep.executeInternal(ExecuteStep.java:105)
	at org.gradle.internal.execution.steps.ExecuteStep.access$000(ExecuteStep.java:44)
	at org.gradle.internal.execution.steps.ExecuteStep$1.call(ExecuteStep.java:59)
	at org.gradle.internal.execution.steps.ExecuteStep$1.call(ExecuteStep.java:56)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:210)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:205)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:67)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:60)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:167)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:60)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.call(DefaultBuildOperationRunner.java:54)
	at org.gradle.internal.execution.steps.ExecuteStep.execute(ExecuteStep.java:56)
	at org.gradle.internal.execution.steps.ExecuteStep.execute(ExecuteStep.java:44)
	at org.gradle.internal.execution.steps.CancelExecutionStep.execute(CancelExecutionStep.java:42)
	at org.gradle.internal.execution.steps.TimeoutStep.executeWithoutTimeout(TimeoutStep.java:75)
	at org.gradle.internal.execution.steps.TimeoutStep.execute(TimeoutStep.java:55)
	at org.gradle.internal.execution.steps.PreCreateOutputParentsStep.execute(PreCreateOutputParentsStep.java:50)
	at org.gradle.internal.execution.steps.PreCreateOutputParentsStep.execute(PreCreateOutputParentsStep.java:28)
	at org.gradle.internal.execution.steps.RemovePreviousOutputsStep.execute(RemovePreviousOutputsStep.java:67)
	at org.gradle.internal.execution.steps.RemovePreviousOutputsStep.execute(RemovePreviousOutputsStep.java:37)
	at org.gradle.internal.execution.steps.BroadcastChangingOutputsStep.execute(BroadcastChangingOutputsStep.java:61)
	at org.gradle.internal.execution.steps.BroadcastChangingOutputsStep.execute(BroadcastChangingOutputsStep.java:26)
	at org.gradle.internal.execution.steps.CaptureOutputsAfterExecutionStep.execute(CaptureOutputsAfterExecutionStep.java:69)
	at org.gradle.internal.execution.steps.CaptureOutputsAfterExecutionStep.execute(CaptureOutputsAfterExecutionStep.java:46)
	at org.gradle.internal.execution.steps.ResolveInputChangesStep.execute(ResolveInputChangesStep.java:40)
	at org.gradle.internal.execution.steps.ResolveInputChangesStep.execute(ResolveInputChangesStep.java:29)
	at org.gradle.internal.execution.steps.BuildCacheStep.executeWithoutCache(BuildCacheStep.java:189)
	at org.gradle.internal.execution.steps.BuildCacheStep.lambda$execute$1(BuildCacheStep.java:75)
	at org.gradle.internal.Either$Right.fold(Either.java:175)
	at org.gradle.internal.execution.caching.CachingState.fold(CachingState.java:62)
	at org.gradle.internal.execution.steps.BuildCacheStep.execute(BuildCacheStep.java:73)
	at org.gradle.internal.execution.steps.BuildCacheStep.execute(BuildCacheStep.java:48)
	at org.gradle.internal.execution.steps.StoreExecutionStateStep.execute(StoreExecutionStateStep.java:46)
	at org.gradle.internal.execution.steps.StoreExecutionStateStep.execute(StoreExecutionStateStep.java:35)
	at org.gradle.internal.execution.steps.SkipUpToDateStep.executeBecause(SkipUpToDateStep.java:75)
	at org.gradle.internal.execution.steps.SkipUpToDateStep.lambda$execute$2(SkipUpToDateStep.java:53)
	at org.gradle.internal.execution.steps.SkipUpToDateStep.execute(SkipUpToDateStep.java:53)
	at org.gradle.internal.execution.steps.SkipUpToDateStep.execute(SkipUpToDateStep.java:35)
	at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsFinishedStep.execute(MarkSnapshottingInputsFinishedStep.java:37)
	at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsFinishedStep.execute(MarkSnapshottingInputsFinishedStep.java:27)
	at org.gradle.internal.execution.steps.ResolveIncrementalCachingStateStep.executeDelegate(ResolveIncrementalCachingStateStep.java:49)
	at org.gradle.internal.execution.steps.ResolveIncrementalCachingStateStep.executeDelegate(ResolveIncrementalCachingStateStep.java:27)
	at org.gradle.internal.execution.steps.AbstractResolveCachingStateStep.execute(AbstractResolveCachingStateStep.java:71)
	at org.gradle.internal.execution.steps.AbstractResolveCachingStateStep.execute(AbstractResolveCachingStateStep.java:39)
	at org.gradle.internal.execution.steps.ResolveChangesStep.execute(ResolveChangesStep.java:65)
	at org.gradle.internal.execution.steps.ResolveChangesStep.execute(ResolveChangesStep.java:36)
	at org.gradle.internal.execution.steps.ValidateStep.execute(ValidateStep.java:107)
	at org.gradle.internal.execution.steps.ValidateStep.execute(ValidateStep.java:56)
	at org.gradle.internal.execution.steps.AbstractCaptureStateBeforeExecutionStep.execute(AbstractCaptureStateBeforeExecutionStep.java:64)
	at org.gradle.internal.execution.steps.AbstractCaptureStateBeforeExecutionStep.execute(AbstractCaptureStateBeforeExecutionStep.java:43)
	at org.gradle.internal.execution.steps.AbstractSkipEmptyWorkStep.executeWithNonEmptySources(AbstractSkipEmptyWorkStep.java:125)
	at org.gradle.internal.execution.steps.AbstractSkipEmptyWorkStep.execute(AbstractSkipEmptyWorkStep.java:56)
	at org.gradle.internal.execution.steps.AbstractSkipEmptyWorkStep.execute(AbstractSkipEmptyWorkStep.java:36)
	at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsStartedStep.execute(MarkSnapshottingInputsStartedStep.java:38)
	at org.gradle.internal.execution.steps.LoadPreviousExecutionStateStep.execute(LoadPreviousExecutionStateStep.java:36)
	at org.gradle.internal.execution.steps.LoadPreviousExecutionStateStep.execute(LoadPreviousExecutionStateStep.java:23)
	at org.gradle.internal.execution.steps.HandleStaleOutputsStep.execute(HandleStaleOutputsStep.java:75)
	at org.gradle.internal.execution.steps.HandleStaleOutputsStep.execute(HandleStaleOutputsStep.java:41)
	at org.gradle.internal.execution.steps.AssignMutableWorkspaceStep.lambda$execute$0(AssignMutableWorkspaceStep.java:35)
	at org.gradle.api.internal.tasks.execution.TaskExecution$4.withWorkspace(TaskExecution.java:289)
	at org.gradle.internal.execution.steps.AssignMutableWorkspaceStep.execute(AssignMutableWorkspaceStep.java:31)
	at org.gradle.internal.execution.steps.AssignMutableWorkspaceStep.execute(AssignMutableWorkspaceStep.java:22)
	at org.gradle.internal.execution.steps.ChoosePipelineStep.execute(ChoosePipelineStep.java:40)
	at org.gradle.internal.execution.steps.ChoosePipelineStep.execute(ChoosePipelineStep.java:23)
	at org.gradle.internal.execution.steps.ExecuteWorkBuildOperationFiringStep.lambda$execute$2(ExecuteWorkBuildOperationFiringStep.java:67)
	at org.gradle.internal.execution.steps.ExecuteWorkBuildOperationFiringStep.execute(ExecuteWorkBuildOperationFiringStep.java:67)
	at org.gradle.internal.execution.steps.ExecuteWorkBuildOperationFiringStep.execute(ExecuteWorkBuildOperationFiringStep.java:39)
	at org.gradle.internal.execution.steps.IdentityCacheStep.execute(IdentityCacheStep.java:46)
	at org.gradle.internal.execution.steps.IdentityCacheStep.execute(IdentityCacheStep.java:34)
	at org.gradle.internal.execution.steps.IdentifyStep.execute(IdentifyStep.java:48)
	at org.gradle.internal.execution.steps.IdentifyStep.execute(IdentifyStep.java:35)
	at org.gradle.internal.execution.impl.DefaultExecutionEngine$1.execute(DefaultExecutionEngine.java:61)
	at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeIfValid(ExecuteActionsTaskExecuter.java:127)
	at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.execute(ExecuteActionsTaskExecuter.java:116)
	at org.gradle.api.internal.tasks.execution.ProblemsTaskPathTrackingTaskExecuter.execute(ProblemsTaskPathTrackingTaskExecuter.java:40)
	at org.gradle.api.internal.tasks.execution.FinalizePropertiesTaskExecuter.execute(FinalizePropertiesTaskExecuter.java:46)
	at org.gradle.api.internal.tasks.execution.ResolveTaskExecutionModeExecuter.execute(ResolveTaskExecutionModeExecuter.java:51)
	at org.gradle.api.internal.tasks.execution.SkipTaskWithNoActionsExecuter.execute(SkipTaskWithNoActionsExecuter.java:57)
	at org.gradle.api.internal.tasks.execution.SkipOnlyIfTaskExecuter.execute(SkipOnlyIfTaskExecuter.java:74)
	at org.gradle.api.internal.tasks.execution.CatchExceptionTaskExecuter.execute(CatchExceptionTaskExecuter.java:36)
	at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.executeTask(EventFiringTaskExecuter.java:77)
	at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.call(EventFiringTaskExecuter.java:55)
	at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.call(EventFiringTaskExecuter.java:52)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:210)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:205)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:67)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:60)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:167)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:60)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.call(DefaultBuildOperationRunner.java:54)
	at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter.execute(EventFiringTaskExecuter.java:52)
	at org.gradle.execution.plan.LocalTaskNodeExecutor.execute(LocalTaskNodeExecutor.java:42)
	at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$InvokeNodeExecutorsAction.execute(DefaultTaskExecutionGraph.java:331)
	at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$InvokeNodeExecutorsAction.execute(DefaultTaskExecutionGraph.java:318)
	at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.lambda$execute$0(DefaultTaskExecutionGraph.java:314)
	at org.gradle.internal.operations.CurrentBuildOperationRef.with(CurrentBuildOperationRef.java:85)
	at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.execute(DefaultTaskExecutionGraph.java:314)
	at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.execute(DefaultTaskExecutionGraph.java:303)
	at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.execute(DefaultPlanExecutor.java:459)
	at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.run(DefaultPlanExecutor.java:376)
	at org.gradle.internal.concurrent.ExecutorPolicy$CatchAndRecordFailures.onExecute(ExecutorPolicy.java:64)
	at org.gradle.internal.concurrent.AbstractManagedExecutor$1.run(AbstractManagedExecutor.java:48)


BUILD FAILED in 53s
198 actionable tasks: 61 executed, 5 from cache, 132 up-to-date
```
