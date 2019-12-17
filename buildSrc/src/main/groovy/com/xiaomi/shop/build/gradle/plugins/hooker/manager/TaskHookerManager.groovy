package com.xiaomi.shop.build.gradle.plugins.hooker.manager

import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformTask
import com.xiaomi.shop.build.gradle.plugins.hooker.GradleTaskHooker
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState

abstract class TaskHookerManager {

    protected Map<String, GradleTaskHooker> taskHookerMap = new HashMap<>()

    protected Project mProject
    protected AppExtension android

    TaskHookerManager(Project project) {
        this.mProject = project
        android = project.extensions.findByType(AppExtension)
        project.gradle.addListener(new MiShopTaskListener())
    }

    abstract void registerTaskHookers(Plugin plugin)

    protected void registerTaskHooker(GradleTaskHooker taskHooker) {
        taskHooker.setTaskHookerManager(this)
        taskHookerMap.put(taskHooker.taskName, taskHooker)
//        println("registerTaskHooker name[${taskHooker.class.name}]")
//        try {
//            throw new IllegalArgumentException("registerTaskHooker")
//        } catch (Exception e) {
//            println(e.stackTrace.toString().replace(','.charAt(0),'\n'.charAt(0)))
//        }
    }


    public <T> T findHookerByName(String taskName) {
        return taskHookerMap[taskName] as T
    }


    private class MiShopTaskListener implements TaskExecutionListener {

        @Override
        void beforeExecute(Task task) {
            if (task.project == mProject) {
                if (task in TransformTask) {
                    taskHookerMap["${task.transform.name}For${task.variantName.capitalize()}".toString()]?.beforeTaskExecute(task)
                } else {
                    taskHookerMap[task.name]?.beforeTaskExecute(task)
                }
            }
        }

        @Override
        void afterExecute(Task task, TaskState taskState) {

            if (task.project == mProject) {
                if (task in TransformTask) {
                    taskHookerMap["${task.transform.name}For${task.variantName.capitalize()}".toString()]?.afterTaskExecute(task)
                } else {
                    taskHookerMap[task.name]?.afterTaskExecute(task)
                }
            }
//            recordInputAndOutput(task)
        }

        void recordInputAndOutput(Task task) {
            if (task.name == "lintVitalRelease") {
                return
            }
            println("task_name[${task.name} --- task_class[${task.class.name}]\n")
            ArrayList<String> record = new ArrayList<>()
            task.inputs.files.files.each {
                record.add("[input_path]:[${it.absolutePath}]")
            }
            task.outputs.files.files.each {
                record.add("[output_path]:[${it.absolutePath}]")
            }
            record.each {
                println(it)
            }
//            FileUtil.saveFile(project.getRootDir(), "allTaskInputAndOutput",
//                    {
//                        return record
//                    })
        }
    }

}