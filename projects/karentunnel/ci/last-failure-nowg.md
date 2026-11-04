# شکست کامپایل چهار ماژول کتابخانه‌ای

- اجرا: https://github.com/rahmaniho/Ktunnel/actions/runs/41759854884

```
> Task :data:compileDebugKotlin FAILED
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/data/src/main/kotlin/ir/karensoft/karentunnel/data/secure/SecureConfigStore.kt:163:1 Syntax error: Unclosed comment.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/vpn/src/main/kotlin/ir/karensoft/karentunnel/vpn/controller/WireGuardTunnelController.kt:88:21 Unresolved reference 'fill'.
> Task :vpn:compileDebugKotlin FAILED
> Task :core:compileDebugUnitTestKotlin FAILED
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:5:43 Unresolved reference 'TestConfigs'.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:18:49 Unresolved reference 'TestConfigs'.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:29:49 Unresolved reference 'TestConfigs'.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:40:19 Unresolved reference 'TestConfigs'.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:56:19 Unresolved reference 'TestConfigs'.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:67:19 Unresolved reference 'TestConfigs'.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:75:19 Unresolved reference 'TestConfigs'.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:89:19 Unresolved reference 'TestConfigs'.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:89:63 Cannot infer type for this parameter. Specify it explicitly.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:96:19 Unresolved reference 'TestConfigs'.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:103:19 Unresolved reference 'TestConfigs'.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:110:19 Unresolved reference 'TestConfigs'.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:121:19 Unresolved reference 'TestConfigs'.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:121:45 Cannot infer type for this parameter. Specify it explicitly.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:121:49 Cannot infer type for this parameter. Specify it explicitly.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:121:54 Unresolved reference 'copy'.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:121:78 Unresolved reference 'privateKey'.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:128:19 Unresolved reference 'TestConfigs'.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:135:49 Unresolved reference 'TestConfigs'.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:136:54 Unresolved reference 'TestConfigs'.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:138:55 Unresolved reference 'TestConfigs'.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:145:19 Unresolved reference 'TestConfigs'.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:152:19 Unresolved reference 'TestConfigs'.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:166:19 Unresolved reference 'TestConfigs'.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:173:49 Unresolved reference 'TestConfigs'.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:181:57 Unresolved reference 'TestConfigs'.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:187:57 Unresolved reference 'TestConfigs'.
e: file:///home/runner/work/Ktunnel/Ktunnel/projects/karentunnel/core/src/test/kotlin/ir/karensoft/karentunnel/core/model/ConfigNormalizerTest.kt:192:57 Unresolved reference 'TestConfigs'.
Execution failed for task ':data:compileDebugKotlin'.
org.gradle.api.tasks.TaskExecutionException: Execution failed for task ':data:compileDebugKotlin'.
Execution failed for task ':vpn:compileDebugKotlin'.
org.gradle.api.tasks.TaskExecutionException: Execution failed for task ':vpn:compileDebugKotlin'.
Execution failed for task ':core:compileDebugUnitTestKotlin'.
org.gradle.api.tasks.TaskExecutionException: Execution failed for task ':core:compileDebugUnitTestKotlin'.
BUILD FAILED in 31s
```

```
	at java.base/java.util.Optional.orElseGet(Optional.java:364)
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
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
	at java.base/java.lang.Thread.run(Thread.java:840)
Caused by: org.jetbrains.kotlin.gradle.tasks.CompilationErrorException: Compilation error. See log for more details
	at org.jetbrains.kotlin.gradle.tasks.TasksUtilsKt.throwExceptionIfCompilationFailed(tasksUtils.kt:21)
	at org.jetbrains.kotlin.compilerRunner.GradleKotlinCompilerWork.run(GradleKotlinCompilerWork.kt:119)
	at org.jetbrains.kotlin.compilerRunner.GradleCompilerRunnerWithWorkers$GradleKotlinCompilerWorkAction.execute(GradleCompilerRunnerWithWorkers.kt:76)
	at org.gradle.workers.internal.DefaultWorkerServer.execute(DefaultWorkerServer.java:63)
	at org.gradle.workers.internal.NoIsolationWorkerFactory$1$1.create(NoIsolationWorkerFactory.java:66)
	at org.gradle.workers.internal.NoIsolationWorkerFactory$1$1.create(NoIsolationWorkerFactory.java:62)
	at org.gradle.internal.classloader.ClassLoaderUtils.executeInClassloader(ClassLoaderUtils.java:100)
	at org.gradle.workers.internal.NoIsolationWorkerFactory$1.lambda$execute$0(NoIsolationWorkerFactory.java:62)
	at org.gradle.workers.internal.AbstractWorker$1.call(AbstractWorker.java:44)
	at org.gradle.workers.internal.AbstractWorker$1.call(AbstractWorker.java:41)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:210)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:205)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:67)
	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:60)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:167)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:60)
	at org.gradle.internal.operations.DefaultBuildOperationRunner.call(DefaultBuildOperationRunner.java:54)
	at org.gradle.workers.internal.AbstractWorker.executeWrappedInBuildOperation(AbstractWorker.java:41)
	at org.gradle.workers.internal.NoIsolationWorkerFactory$1.execute(NoIsolationWorkerFactory.java:59)
	at org.gradle.workers.internal.DefaultWorkerExecutor.lambda$submitWork$0(DefaultWorkerExecutor.java:174)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner.runExecution(DefaultConditionalExecutionQueue.java:194)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner.access$700(DefaultConditionalExecutionQueue.java:127)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner$1.run(DefaultConditionalExecutionQueue.java:169)
	at org.gradle.internal.Factories$1.create(Factories.java:31)
	at org.gradle.internal.work.DefaultWorkerLeaseService.withLocks(DefaultWorkerLeaseService.java:263)
	at org.gradle.internal.work.DefaultWorkerLeaseService.runAsWorkerThread(DefaultWorkerLeaseService.java:127)
	at org.gradle.internal.work.DefaultWorkerLeaseService.runAsWorkerThread(DefaultWorkerLeaseService.java:132)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner.runBatch(DefaultConditionalExecutionQueue.java:164)
	at org.gradle.internal.work.DefaultConditionalExecutionQueue$ExecutionRunner.run(DefaultConditionalExecutionQueue.java:133)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:539)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
	... 5 more

==============================================================================

BUILD FAILED in 31s
44 actionable tasks: 35 executed, 9 from cache
```
