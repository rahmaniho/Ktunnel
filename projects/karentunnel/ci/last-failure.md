# آخرین خروجی ناموفق CI

- اجرا: https://github.com/rahmaniho/Ktunnel/actions/runs/41764940840
- کامیت: `ddc53b37e76d008fe6a45c4ef8a2887933f31c7a`
- زمان: 2026-11-04T03:09:00Z

## خطاهای کامپایلر

```
> Task :vpn:lintDebug FAILED
* What went wrong:
Execution failed for task ':vpn:lintDebug'.
org.gradle.api.tasks.TaskExecutionException: Execution failed for task ':vpn:lintDebug'.
Caused by: java.lang.RuntimeException: Lint found errors in the project; aborting build.
BUILD FAILED in 37s
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


BUILD FAILED in 37s
196 actionable tasks: 59 executed, 5 from cache, 132 up-to-date
```
