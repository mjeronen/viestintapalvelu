package fi.vm.sade.ajastuspalvelu.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.sade.ajastuspalvelu.dao.ScheduledTaskDao;
import fi.vm.sade.ajastuspalvelu.model.ScheduledTask;
import fi.vm.sade.ajastuspalvelu.service.ScheduledTaskService;
import fi.vm.sade.ajastuspalvelu.service.converter.ScheduledTaskConverter;
import fi.vm.sade.ajastuspalvelu.service.dto.ScheduledTaskCriteriaDto;
import fi.vm.sade.ajastuspalvelu.service.dto.ScheduledTaskListDto;
import fi.vm.sade.ajastuspalvelu.service.dto.ScheduledTaskModifyDto;
import fi.vm.sade.ajastuspalvelu.service.dto.ScheduledTaskSaveDto;
import fi.vm.sade.ajastuspalvelu.service.scheduling.QuartzSchedulingService;
import fi.vm.sade.ajastuspalvelu.service.scheduling.impl.SingleScheduledTaskTaskSchedule;
import fi.vm.sade.viestintapalvelu.common.util.OptionalHelper;

import static com.google.common.base.Optional.fromNullable;

@Service
public class ScheduledTaskServiceImpl implements ScheduledTaskService {

    @Autowired
    private ScheduledTaskDao dao;
    
    @Autowired
    private ScheduledTaskConverter scheduledTaskConverter;
    
    @Autowired
    private QuartzSchedulingService schedulingService;

    @Override
    @Transactional
    public ScheduledTaskListDto insert(ScheduledTaskSaveDto dto) throws SchedulerException {
        ScheduledTask task = dao.insert(scheduledTaskConverter.convert(dto, new ScheduledTask()));
        updateScheduling(task);
        return scheduledTaskConverter.convert(task);
    }

    @Override
    @Transactional
    public void update(ScheduledTaskModifyDto dto) throws SchedulerException {
        ScheduledTask task = fromNullable(dao.read(dto.getId()))
            .or(OptionalHelper.<ScheduledTask>notFound("ScheduledTask not found by id=" + dto.getId()));
        dao.update(scheduledTaskConverter.convert(dto, task));
        updateScheduling(task);
    }

    private void updateScheduling(ScheduledTask task) throws SchedulerException {
        schedulingService.scheduleJob(task.getId(), new SingleScheduledTaskTaskSchedule(task.getRuntimeForSingle()));
    }

    @Override
    @Transactional
    public void remove(long id) throws SchedulerException {
      ScheduledTask task = fromNullable(dao.read(id))
              .or(OptionalHelper.<ScheduledTask>notFound("ScheduledTask not found by id=" + id));
      task.setRemoved(new DateTime());
      dao.update(task);
      schedulingService.unscheduleJob(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduledTaskListDto> list(ScheduledTaskCriteriaDto criteriaDto) {
        List<ScheduledTask> tasks = dao.find(criteriaDto);
        List<ScheduledTaskListDto> dtos = new ArrayList<>();
        for (ScheduledTask scheduledTask : tasks) {
            dtos.add(scheduledTaskConverter.convert(scheduledTask));
        }
        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public int count(ScheduledTaskCriteriaDto criteria) {
        return dao.count(criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduledTaskModifyDto findById(long id) {
        return scheduledTaskConverter.convert(fromNullable(dao.read(id))
                .or(OptionalHelper.<ScheduledTask>notFound("ScheduledTask not found by id=" + id)),
                new ScheduledTaskModifyDto());
    }

}
