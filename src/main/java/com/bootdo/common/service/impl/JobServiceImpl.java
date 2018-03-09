package com.bootdo.common.service.impl;

import com.bootdo.common.dao.TaskDao;
import com.bootdo.common.domain.ScheduleJob;
import com.bootdo.common.domain.TaskDO;
import com.bootdo.common.quartz.utils.QuartzManager;
import com.bootdo.common.service.JobService;
import com.bootdo.common.utils.ScheduleJobUtils;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@SuppressWarnings("unchecked")
public class JobServiceImpl implements JobService {
	
	@Autowired
	private TaskDao taskDao;


	@Override
	public TaskDO get(Long id) {
		return taskDao.get(id);
	}

	@Override
	public List<TaskDO> list(Map<String, Object> map) {
		return taskDao.list(map);
	}

	@Override
	public int count(Map<String, Object> map) {
		return taskDao.count(map);
	}

	@Override
	public int save(TaskDO taskScheduleJob) {
		return taskDao.save(taskScheduleJob);
	}

	@Override
	public int update(TaskDO taskScheduleJob) {
		return taskDao.update(taskScheduleJob);
	}

	@Override
	public int remove(Long id) {
		QuartzManager quartzManager = new QuartzManager();
		try {
			TaskDO scheduleJob = get(id);
			quartzManager.deleteJob(ScheduleJobUtils.entityToData(scheduleJob));
			return taskDao.remove(id);
		} catch (SchedulerException e) {
			e.printStackTrace();
			return 0;
		}
		
	}

	@Override
	public int batchRemove(Long[] ids) {
		QuartzManager quartzManager = new QuartzManager();
		for (Long id : ids) {
			try {
				TaskDO scheduleJob = get(id);
				quartzManager.deleteJob(ScheduleJobUtils.entityToData(scheduleJob));
			} catch (SchedulerException e) {
				e.printStackTrace();
				return 0;
			}
		}
		return taskDao.batchRemove(ids);
	}

	@Override
	public void initSchedule() throws SchedulerException {
		// 这里获取任务信息数据
		QuartzManager quartzManager = new QuartzManager();
		List<TaskDO> jobList = taskDao.list(new HashMap());
		for (TaskDO scheduleJob : jobList) {
			quartzManager.addJob(ScheduleJobUtils.entityToData(scheduleJob));
		}
	}
	@Override
	public void changeStatus(Long jobId, String cmd) throws SchedulerException {
		QuartzManager quartzManager = new QuartzManager();
		TaskDO scheduleJob = get(jobId);
		if (scheduleJob == null) {
			return;
		}
		if ("stop".equals(cmd)) {
			quartzManager.deleteJob(ScheduleJobUtils.entityToData(scheduleJob));
			scheduleJob.setJobStatus(ScheduleJob.STATUS_NOT_RUNNING);
		} else if ("start".equals(cmd)) {
			scheduleJob.setJobStatus(ScheduleJob.STATUS_RUNNING);
			quartzManager.addJob(ScheduleJobUtils.entityToData(scheduleJob));
		}
		 update(scheduleJob);
	}
	
	@Override
	public void updateCron(Long jobId) throws SchedulerException {
		QuartzManager quartzManager = new QuartzManager();
		TaskDO scheduleJob = get(jobId);
		if (scheduleJob == null) {
			return;
		}
		if (ScheduleJob.STATUS_RUNNING.equals(scheduleJob.getJobStatus())) {
			quartzManager.updateJobCron(ScheduleJobUtils.entityToData(scheduleJob));
		}
		 update(scheduleJob);
	}

}
