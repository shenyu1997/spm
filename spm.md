# MVP (minimum viable product)
## Goal
Provide skeleton model and function to mamage  projects. Include planning project, manage tasks, executing, progress sync, re-planning, event notification, and track information about actual project execution  automatically.

## Model List
- Project
- Phase
- WorkItem
- WorkItemNote
- Person
- PersonGroup
- Event/Message


## User case:
### Create a Project. 
As a Person, I can create a project, the owner is myself,  Set project name; Set project manager; Select person and add to project members, selected person will received Message. 

### Vew project list
As a project related person, I can view all related projects. As default, 'RUNNING' project should be shown, Project related person means project owner, project manager, project members.

### Delete/cancel a projct
Aa a Project owner, or mamager I can delete/cancel my project, delete will drop all of project data. related data should be dropped cascadelly; cancel will mark project as 'STOP', related person will receive Message.

### Modify a project
Aa owner or manager of a Project, I can modify the project properties. which include the name, Goal, owner, manager, projectMembers..., related person will receive Message.

### Planning or Replanning
As a manager of a Project, I cna planning/replanning project by  1) select/change start date of project,  2)Add new phase, 3) Remove INIT phase, 4)Change phase time frame, 5)Postpone Project; replanning project can be done at anytime; When project in 'INIT ', all of modify is silent,  otherwise replanning will generate event to notify all person related which project . 

#### Add new phase
As a project manager, I can insert phase in Project Edit view, information I provided: after/before phase, phase name, planned end date. Constrant, the planned end date new created phase should not be lated than planned end date of next phase.

#### Remove INIT phase 
As a project manager, I can remove phase if phase is 'INIT', this operation will drop all of data belong the phase

#### Change phase time fame 
As a project manager, I can modify phase in Project Edit view, 1) select phase to modify 2) PlannedEndDate(only INIT, RUNNING).

#### Postpone a project
As a project manager, I can postpone the Project by: 1) select postpone start date, 2) decide how many day to postpone, 2) apply. after this operation, all related time frame which later than postpone start date will be postpone specified day, include phase and workItem. 

### Start a project
As a prject manager, I can start project, after this operation, project status change to 'RUNNING', the actualStartDate will be recored, the first phase will be marked as 'RUNNING', all project related person will receive Message

### Add a workItem
As a project manager, I can add WorkItem and assign it to one of 'Project Members', operations: 1) select a non 'STOP' phase, 2) add workItem by input title description, priority, group, deadline..., after creation, the new WorkItem phase was set, running status will be at 'INIT', the default value of planned date is workItem created date; Constraint: plannedDate should early than deadLine, plannedDate and deadLine should in project time range,

### Assign a workItem
As a project mamager, I can assign workItem to assignee, 1) select workItem, set assignee, assignee will receive Message

### Modify a workITem
As a project mamager, I can modify workItem , include change title, description, plannedDate, deadLine, assignee, priority..., Constraint: deadLine can not be empty; plannedDate (if not empty) should early than deadLine, plannedDate and deadLine should in project time range, assignee will receive the Message, project owner will also receive Message if the workItem is 'Primary'

### View workItems from My Tasks List
As a person, I can see workItems which assign to me in My Tasks list, 'STOP' workItem is not shown by default.  Sorted by deadLine is default.

### Start a workItem
As a workItem assignee, I can start a workItem which in Running phase, after start, the workItem running status will be 'RUNNING', project mamager will receive Message; project owner will also receive Message if the workItem is 'Primary'

### Report a workItems progress
As a workItem assingee, I can take a 'Note' to the workItem to report the progress, a project mamager will receive Message, project owner will also receive Message if the workItem is 'Primary'

### Done a workItem
As a workItem assignee, I can mark the workItem is done, after this the workItem runnint status is 'STOP', record actualDoneDate, the project mamager will receive Message about this operation. project owner will also receive this Message if the workItem is 'Primary'

### Remove/cancel a workItem
As a project manager, I can remove/cancel WorkItem, the 'INIT' workItem can be removed, including all data, cancel only mark running status as 'STOP', assignee will review Message, project owner will also receive this Message if the workItem is 'Primary'

### Done a phase.
As a project manager, I can finished current phase if all of workItem has 'STOP'. After this operation, the current phase  will be at 'STOP',record actualEndDate, If next Phase exist, it will be set to 'RUNNING'; all project related person will receive this Message

### Done a project.
As a project manager, I can done the project which mamaged by me, if all of phases has 'STOP'. record actualFinishDate ,After this operation, the Project is finished, which means runningStatus of the project is 'STOP'. all project related person will receive this Message

### View Messages
As a person, I can view Message from my message box, and mark message as readed.  click/tap the message, I can navigate to related item(project/phase/workItem)

## Form/UI
- Project list view
- Project create view
- Project moidify view
- Project planning/replanning view
- Project detail view (include workitem list)
- WorkItem detail modify view
- WorkItem execute view: report grogress, add comment, marked as done
- My task view
- My message box view
- message detail view

## Model Design
### Common Enum & List
- RunningStatus: INIT ,RUNNING, STOP
- Priority: TOP, HIGHT, MEDIUM, LOW

### Model & Properties
#### Project
- name: String (Label propertiy)
- description: String
- owner: Person
- manager: Person
- projectMembers: PersonGroup
- status: RunningStatus
- isCanceled
- plannedStartDate: Date
- plannedEndDate: Date (Calculated, should be set as latest phase' plannedEndDate)
- actualStartDate: Date
- actualEndDate: Date


#### Phase
- name: String
- goal: String
- project: Project
- status: RunningStatus
- plannedStartDate: Date (Calculated. should be set as pre-phase's plannedEndDate or Project's plannedStartDate)
- plannedEndDate: Date
- actualStartDate: Date (Calculated. should be set as pre-phase's actualEndDate or Project's plannedStartDate)
- actualEndDate: Date

#### WorkItem
- name: String
- detail: String
- assignee: Person
- phase: Phase
- plannedDate: Date
- deadLine: Date
- actualDoneDate: Date
- priority: Priority
- isCanceled: boolean
- isPrimary: boolean

#### WorkItemNote
- createdTimestemp: DateTime
- content: String
- author: Person

#### Person
- name: String
- phone: String
- wxNum: String

#### PersonGroup
- name: String
- members: List of Person

#### Event
- type: SHCHEUL_CHANGED, EXECUTION_STATUS_UPDATED, PARTICIPENT_CHANGED, OTHER_CHANGED 
- subType:PROJECT_CHANGED, MEMBER_ADDED, MEMBER_REMOVED, PROJECT_RE_PLANNING, PROJECT_START, WORK_ITEM_ASSIGNED....
- source: Project/Phase/workItem
- message: String
- timestmp: dataTime
- version: int
- trigger: person

#### Message
- source: Event
- receiver: Person
- isReaded: boolean

------------------------------------------------------------------------------------------------------------------------------------
# My Project and multiple participent project
## Goal
Current system can be used between Multiplayer collaboration(MC) project, also can be used in personal project(MY).

## Impacted Model
- Project

## User case

### Indicate the project is my project or Multiplayer collaboration project
As a Person, when I create a project, I can set project is multiplayer collaboration(MC) project (default) or my project. This setting will impact: 1) workItem assignee will be null in MC project and workItem assignee will be myself by default in case 'MyProject'; 2) No event message will show in MessageBox, because 'MyProject' planning and execute will always be trigger by myself. 

### Switch project between MC Project or My Project
As a project owner, I can switch project between MC Project or My Projec;

## Impacted Model propeties
### Project
- isPersonal

------------------------------------------------------------------------------------------------------------------------------------
# Collaboration Than Management
## Goal
As project manager/boss, I can decided change style of managing tent to more collaboration or mmore management, eg, could participent can add/manage/delete workItem by himself? or could person can add workItem which not belong to any project, only belong to himself. 



------------------------------------------------------------------------------------------------------------------------------------
# Delay Motinor & HEALTH
## Goal
Monitor execution of project, phase, workItem, if 'time frame action' is delay, send alert to related person, and will present as one Health indictor finally; project list should only display un-health project by default. 

Time frame action means the active should be tracked by a time, here, 
- The end of project
- The end of phase
- the done of WorkItem, if deadLine was set
- The start of workItem, if plannedStarDate was set

## Model Enum List
- DelayAlert
- Health
- DelayAlertLevelDefination (configuation)
- AlertPointDefinition (configuation)
- HealthLevelDefiniation (configuation)

## User case
### Alert if delay occured
As System, I can create a Alert if if 'time frame action' is delay; it will be done this by: when I receive SCHEUD_CHANGED event,  I will checkout source entity type, for example: if phase planned end date was set, I will create a serial checkCallbacks on planned end date, and the checkCallBack will create/update/delete DelayAlert if the phase runningStatus wat not as 'STOP', level is 'Delay', source is phase. The level and checkCallBack check time should be calculated by pre-config DelayAlertLevelDefination. Related person will receive the alert.

### Alert cancel
As system, when I receive EXECUTION_STATUS_CHANGED event, I will delete related alert

### View workItem/phase/project related Alert
As project related person, I can view workItem/phase/project related alert. As a project owner, the view might be more important than workItem list in current phase.

### View alert from messageBox
As a person, I can see the alert message from messageBox

### Message can be staleness if alert was delete
As a person, wen I view message, message will be marked as staleness if the source alert was delete by System. Message box should show staleness == false message by default

### Alert will aggreage to Health indicator
As System, when I receive alert, I will recalucate health indicate by reference HealthLevelDefiniation. one project has one Health indicator, health only apply to project; when health level changed, project related person should received message

Algroithm of Agregate Alert to Health:
- Alert will be calculate to point by AlertPointDefinition
- Sum of alert's point with the project
- Identify health level by HealthLevelDefiniation


## Model 
### DelayAlert
- type: PHASE_DONE, WORK_ITEM_DONE, PROJECT_DONE, WORK_ITEM_START
- level: DELAY, WARN, CRITICAL
- source: project/phase/workItem
- lastUpDate: dateTime

### Message
- source: Event, Alert
- staleness: boolean

### Healthy
- level: GOOD, PROBLEM, CRETICAL, FAULT
- source: project
- lastUpdateTime: DateTime

### Event
- source: Project/Phase/workItem/health


## OOB Conguration
### DelayAlertLevelDefination
```
{
  WORK_ITEM_START: {
    WARN: 3, day
    CRITICAL: 5 day
  },
  WORK_ITEM_DONE: {
    WARN: 3, day
    CRITICAL: 5 day
  },  
  PRIMARY_WORK_ITEM_DONE: {
    WARN: 1, day
    CRITICAL: 3 day
  },
  PRIMARY_WORK_ITEM_START: {
    WARN: 2, day
    CRITICAL: 5 day
  },
  PHASE : {
    WARN: 5, day
    CRITICAL: 15 day
  }
  PROJECT_DONE {
    WARN: 10, day
    CRITICAL: 30 day
  }
}


```

### AlertPointDefinition
```
{
  MORMAL_WORK_ITEM:  {
      DELAY: 1,
      WARN: 2,
      CRITICAL:3
  }
  PRIMARY_WORK_ITEM: {
      DELAY: 3,
      WARN: 5,
      CRITICAL:8
  },
  PHASE: {
      DELAY: 8,
      WARN: 13,
      CRITICAL:21
  },
  PROJECT: {
      DELAY: 21,
      WARN: 34,
      CRITICAL:55
  },
}
```

### HealthLevelDefiniation
```
{
  GOOD: < 3 
  PROBLEM: < 20 
  CRETICAL: < 50
  FAULT: > 50

}
```


------------------------------------------------------------------------------------------------------------------------------------
# ProjectTemplate
## Goal
1. Provide facility to help planning when project init project plan. 
2. Manage project management by pre-defined mandatory workItem in template.

## Impacted/Added Model
- ProjectTemplate
- Porject
- PhaseTemplate
- WorkItemTemplate
- WorkITem
- Role (Configuarion)

## User case
### Add/modify/delete project template
As a Admin, I can add/modify project template, and set properties of project tempalte; I also can delete a project template, after delete all related data and referece will be removed

### Creat projectTemplate by generate by project
AS a Admin, I can create projectTemplate by: 1) select a project, 2) generate projectTemplate from the project. After do this, the project will be generated, and in 'disable' status by default.


### Pre-plannging a project
As a Admin, I can pre-planning a project template by: adding phaseTemplate, removing, modify phaseTemplate name, duration(unit day/week) of the phase, 3) add/remove/modify workItemTemplate to phaseTemplate, set properties of workItemTemplate.

### Enable/disable template
As a Admin, I can enable/disable project template, Only enabled template can be display in template list when project manager init a project.

### Planning project by using template
As project manager, when I init a project, I can init project by select project template, after this step, all phases and all workItems which pre-defined in projectTemplate will be add to project automatically, all time frame will be calculated also.

### Mandatory workItem can not be removed or canceled
As a project manager, when I remove/cancel WorkItem, add Constraint: workItem is can not be remove or cancel if is mark as mandatory

### Set/Unset mandatory property of workItem
As a project owner, I can set/unSet workItem as mandatory

### 

## Form/UI
- ProjectTemplateListView
- ProjectTemplateCreateView
- ProjectTemplateEditView

## Model & properties
### ProjectTempate
- name: String (Label propertiy)
- description: String
- plannedStartDate: Date
- duration: Date (Calculated, should be set as sum of all phaseTempate duration)
- enabled: boolean (only effective when is template)

### Project
- template: Project (reference a template project)


### PhaseTemplate
- name: String
- goal: String
- project: ProjectTemplate
- duration: int, the lenght of current phase, because template phase can not set planned end date, so using duration to present. 

### WorkItemTemplate
- name: String
- detail: String
- assignee: Person
- phase: PhaseTempalte
- priority: Priority
- isMandatory: boolean
- plannedDateOffset: int
- duration: int

### WorkItem
- isMandatory: boolean

## Configuation
### Role (Config):
```
[
  {name: 'Admin'},
  {name: 'EMP'}
]
```

------------------------------------------------------------------------------------------------------------------------------------
# Add social interaction
## Goal
Give system more ablity to support social properties, 1) interactions like person can add/cancel attention to workItem and review all message from concerned item. Other behavior like, add 'star', add 'encourage'; 

## User case

### Add attention to workItem
As a person, I want add attention on any workItem which I can see. after this operation, I can receive all message send from it. atterntion also can be removed, after remove, I won't receive message from the workItem caused by atterntion. Add/remove attention will send message to workItem owner/assignee. event type: SOCIAL_INTERACTION. Owner/assignee can view this message from his messageBox

### View all concerned workItem.
As a person, I can view all concerned workItem and it's status in a view. Concerned workItem means workItem which bean added attention be myself. I can click these workItem and navigate to workItem detail form.

### Aware attention when view workItem.
As a person, I can know who add attention to current workItem when I view workItem.

### Add star/encourage on a workItem
As a person, I can add/encourage star on any workITem which I can see. Event will be sent, and event type: SOCIAL_INTERACTION Owner/assignee can receive message.

### Aware star/encourage when view workItem.
As a person, I can know how many and who add star and encourage on workItem  when I view workItem.

------------------------------------------------------------------------------------------------------------------------------------
# Pre-Calculate resouce contension when re-planning, alert if resource confilict
## Goal
When do re-planing (include postpone), the most hard part is to eslimate if re-planing is doable, if there are any resource confilict after re-planning. We holp, when do re-planning, sytem can detect the resource confilict before finish re-planning.


------------------------------------------------------------------------------------------------------------------------------------
# Invoved more people
## Goal
invoved more people in project lifecycle, like add stakeholds(PersonGroup) on project to report status change; like add Participents(PersonGroup) to workItem, to implement multiplayer collaboration task; 

## User case
### Invite participant for workItem.
As a person, If my workItem need more person participate, I can invite any Person. after invited, the person invited will be as a participant to current workItem and his will receive any message related this workItem. Behavior 'Invite' will send message to participant, event type PARTICIPENT_CHANGED;

### Participant add comments on workItem
As a participant, I can add workItemNote to the workItem, noteType is 'COMMENT', add comment will send event to related person, type: EXECUTION_STATUS_CHANGED

### Aware assignee/perticipant when I view myTask
As a person, I can distinguish whether I am assignee or perticipant  in MyTask View

### WorkItem's event notify related participent
As a participent of a workItem, I can receive all event about this workItem

### Add/Remove stakehold to project
In complex organiztion, a perject might be report to many stakeholds rather than the project owner. As a project owner, I can add/remove any person to project stakeholds. The Operation will send event to project related person, event type PARTICIPENT_CHANGED;  After this person who is stakehold will receive message about the project. Remove stakehold is vise operation.


------------------------------------------------------------------------------------------------------------------------------------
# Grouping workItem by funcitonal group (oranization related)
## Goal
In litter complex organiztion, person will be grouped by pre-defined group, like dev group, market group, purchase group..., etc. Coordinate, workItems can also group be functional group/team after assignee be set, assignee's funciton group is workItem's functional group, workItem can filter by functional group when will, and workItem status and altert will also sent to functional group/team leader if it has value

## User case
### Define/Modify functional group
As a Admin, I can create funcitonal group, set name, select person to add to functional group. So far, one person only support belong to on funcitonal group. Person add to functional group will receive the event message, event type: ORG_CHANGED

### Set/Unset funcitonalGroup's leader
As a Admin, I can set/unset the leader with a funcitonalGroup. The person who is leader will reveived message, event type: ORG_CHANGED, After this operation, the lead will receive all related workItems's event and alert.

### Move person to another funcitonal group
As a Admin, I can select a person, move him to a functional group.

### Remove functional group
As a admin, I can remove functional group, after remove person is not belong to this functional group any more.

### View workItem and filter by functionalGroup
As a person, when I view workItem list, I can filter them's functionalGroup by assignee's functionalGroup

------------------------------------------------------------------------------------------------------------------------------------
# Dependency management between workItems
## Goal
In real work, there are dependencies between workItems, means if pre-workItems is not finished, then after-workItem is not ready to start.


------------------------------------------------------------------------------------------------------------------------------------
# WorkItem split to Meeting, Task, CheckPoint
## Goal
Refine workItem to sub-class like Meeting, Task, CheckPoint, to provide more appropriate properties and behaviors on these target, eg, as meeting , people can take metting minutes; as checkPoint, can task check lists...

------------------------------------------------------------------------------------------------------------------------------------ 
# Gernerate report
## Goal
Generate work/project daily/weekly report automatically, to save person time or avoid reduntent routine work.

------------------------------------------------------------------------------------------------------------------------------------
# Support Other status, BLOCK, SUSPEND
## Goal
Project manage can suspend project temporaryly, to let people focus on-doing projects. Consider support other status'BLOCK', 'SUSPEND' and action.


------------------------------------------------------------------------------------------------------------------------------------
# Project re-play and analsys
## Goal
After project finished, provide a tool to re-view whole project lifecycle, and some report to summary key factor of project, like delay item report..., then impove project management by modify project template like add more mandatory workItem add checkList, etc.

------------------------------------------------------------------------------------------------------------------------------------
# Support customized filed and customzed enum, tag in ProjectSetting
## Goal
Give project manage ability to decided how to run his project; implement this by supporting config system behaviors in two level, project level, tenant level; project settings can be override tenant level settings;

------------------------------------------------------------------------------------------------------------------------------------
# Support more permission control
## Goal
Satisfy 'Desire to control' from boss; provide more fine-grained permission control, like whether project manager can modify perject memvers, whether project manager can re-schedule project after project 'START', whether workItem can be removed, etc.





