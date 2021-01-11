package com.yuansong.tools.task.impl;

import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import com.yuansong.tools.common.ObjectManager;
import com.yuansong.tools.task.IDynamicTask;
import com.yuansong.tools.task.IDynamicTaskManager;

@Component
public class DynamicTaskManagerImpl implements IDynamicTaskManager {
	
	@Autowired
	private ThreadPoolTaskScheduler threadPoolTaskScheduler;
	
	private ObjectManager<IDynamicTask> configManager;
	
	private ObjectManager<ScheduledFuture<?>> taskManager;
	
	public DynamicTaskManagerImpl() {
		this.configManager = new ObjectManager<IDynamicTask>();
		this.taskManager = new ObjectManager<ScheduledFuture<?>>();
	}
	
	@Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(10);
		threadPoolTaskScheduler.setThreadNamePrefix("dynamicTask-");
		threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
		threadPoolTaskScheduler.setAwaitTerminationSeconds(60);
        return threadPoolTaskScheduler;
    }

	@Override
	public void addTask(IDynamicTask task) {
		synchronized(configManager) {
			String taskName = task.getName();
			this.removeTask(taskName);
			this.configManager.register(taskName, task);
			this.resumeTask(taskName);
		}
	}

	@Override
	public synchronized void removeTask(String name) {
		synchronized(configManager) {
			String taskName = name;
			this.pauseTask(taskName);
			if(this.configManager.containsKey(taskName)) {
				this.removeTask(taskName);
			}
		}
	}

	@Override
	public void pauseTask(String name) {
		synchronized(taskManager) {
			if(this.taskManager.containsKey(name)) {
				ScheduledFuture<?> task = this.taskManager.getObject(name);
				if(task != null) {
					task.cancel(false);
				}
				this.taskManager.unregister(name);
			}
		}
	}

	@Override
	public void resumeTask(String name) {
		synchronized(taskManager) {
			if(!this.taskManager.containsKey(name)) {
				return;
			}
		}
	}

	@Override
	public boolean isTaskRunning(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getTask(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containTask(String name) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
