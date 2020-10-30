# MVP
## Target
Provide skeleton model and function to implement main aspact of project management. include scheduling/planning, assigning tasks, executing tasks, move phase, message, status viewing.

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
Aa a Project owner, or mamager I can delete/cancel his project, delete will drop all of project data. related data should be dropped cascadelly; cancel will mark project as 'STOP', related person will receive Message.

### Modify a project
Aa owner or manager of a Project, I can modify the project properties. which include the name, targetDescription, owner, manager, projectMembers..., related person will receive Message.

### Project planning or Replanning
As a manager of a Project, I planning/replanning project by  1) select/change start date,  2)Add new phasea 3) ;Remove INIT phase, 4)Change phase timeline, 5)Postpone Project; replanning project can be done at anytime; When project in 'INIT ', all of modify is silent,  otherwise replanning will generate event to notify project all related person. 

#### Add new phase
As a project manager, I can insert phase in Project Edit view, information I provided: after/before phase, phase name, planned end date. Constrant, the planned end date new created phase should not be lated than planned end date of next phase.

#### Remove INIT phase 
As a project manager, I can remove phase if phase is 'INIT', this operation will drop all of data belong the phase

#### Change phase timeline 
As a project manager, I can modify phase in Project Edit view, 1) select phase to modify 2) PlannedEndDate(only INIT, RUNNING).

#### Postpone Project
As a project manager, I can postpone the Project by: 1) select phase to postpone, 2) select how many day to postpone, 3) apply. after this operation, all related phase's planned end date should be postpone, all deadLine of workItem which belong these phase should also be postpone. 

### Start project
As a prject manager, I can start project, after this operation, project status change to RUNNING, the first phase will be marked as 'RUNNING', all project related person will receive Message

### Add workItem
As a project manager, I can add WorkItem and assign it to one of 'Project Members', operations: 1) select a non 'STOP' phase, 2) add workItem by input title description, priority, group, deadline..., after creation, the new WorkItem phase was set, running status will be at 'INIT'

### Assign workItem
As a project mamager, I can assign workItem to assignee, 1) select workItem, set assignee, assignee will receive Message

### Modify workITem
As a project mamager, I can modify workItem , include change title, description, deadLine, assignee, priority..., assignee will receive the Message, project owner will also receive Message if the workItem is 'Primary'

### View workItem from My Tasks List
As a workItem assignee, I can see workItems in My Tasks list, 'STOP' workItem is not shown by default.  Sorted by deadLine is default.

### Start workItem
As a workItem assignee, I can start a workItem which in Running phase, after start, the workItem running status will be 'RUNNING', project mamager will receive Message; project owner will also receive Message if the workItem is 'Primary'

### Report workItems progress
As a workItem assingee, I can take a 'Note' to the workItem to report the progress, a project mamager will receive Message, project owner will also receive Message if the workItem is 'Primary'

### Done a workItem
As a workItem assignee, I can mark the workItem is done, after this the workItem runnint status is 'STOP', the project mamager will receive Message about this operation. project owner will also receive this Message if the workItem is 'Primary'

### Remove/cancel workItem
As a project manager, I can remove/cancel WorkItem, the 'INIT' workItem can be removed, including all data, cancel only mark running status as 'STOP', assignee will review Message, project owner will also receive this Message if the workItem is 'Primary'

### Done a phase.
As a project manager, I can finished current phase if all of workItem has 'STOP'. After this operation, the current phase  will be at 'STOP', If next Phase exist, it will be set to 'RUNNING'; all project related person will receive this Message

### Done a project.
As a project manager, I can done the project which mamaged by me, if all of phases has 'STOP'. After this operation, the Project is finished, which means runningStatus of the project is 'STOP'. all project related person will receive this Message

### View Message
As a person, I can view Message from my message box, and mark message as readed. 

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

### Entity & Properties
#### Project
- name: String
- targetDescription: String
- owner: Person
- manager: Person
- projectMembers: PersonGroup
- status: RunningStatus
- isCanceled
- plannedStartDate: Date
- plannedEndDate: Date (Calculated, should be set as latest phase' plannedEndDate)


#### Phase
- name: String
- project: Project
- status: RunningStatus
- plannedStartDate: Date (Calculated. should be set as pre-phase's plannedEndDate or Project's plannedStartDate)
- plannedEndDate: Date

#### WorkItem
- name: String
- detail: String
- assignee: Person
- phase: Phase
- deadLine: Date
- priority: Priority
- isCanceled
- isPrimary

#### WorkItemNote
- date: Date
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
- TYPE: PROJECT_CHANGED, MEMBER_ADDED, MEMBER_REMOVED, PROJECT_RE_PLANNING, PROJECT_START, WORK_ITEM_ASSIGNED....
- project: Project
- phase: Phase
- workitem: WorkItem
- message: String
- timestmp: dataTime
- trigger: person

#### Message
- event: Event
- receiver: Person
- isReaded: boolean

-----------------------------------------------------------------------------------------------------------
# ProjectTemplate
## Target
1. Provide facility to help planning when project init project plan. 
2. Manage project by mandatory workItem pre-defined in template

## Entity & properties
### Project
- isTemplate: boolean


## User case
### Admin add project template
As a Admin, I can add project template, 1) add project template, set name, 2) add phase, set phase name, duration(unit day/week) of the phase, 3) add workItem to phase, set properties of workItem

### Admin modify project template
As a Admin, I can add project template, 1) change name, 2) add phase, remove, modify phase name, duration(unit day/week) of the phase, 3) add/remove/modify workItem to phase, set properties of workItem

### Admin enable/disable template
As a Admin, I can enable/disable project template, Only enabled template can be display in template list when project manager init a project.

### Admin drop project template
As a Admin, I can add any project template, after drop data will be drop

### Planning project by template
As project manager, I can init project by template

---
# Alert, KPI, HEALTH
## Target
Monitor project, generate KPI progress_rate,  send alert to related person, when risk occurred, hightlight unHealth project

- ProgressRate: ? ON_TRACK, DELAY
    - plannedEndDate: Date
    - actualEndDate: Date
  Apply entity: WorkItem, Phase, Project

# Health: GOOD, WARNNING, CRITICAL, FAULT
Calculate result of KPIs  

---
# Invoved more people
## Project Stakeholds
## Workitem Participent
## Organize workItem by group
## Set lead for funcitonal group, and receive all related message/alert

WorkItem could have some perticipents, not only assignee

## Entity & properties
### WorkItem
- participants: List of Person

## Use Case

### WorkItem Assignee invite participants
As a workItem assingee, I can invite/un invite participants, after invite, the workItem will appeared in 'My Tasks' of participants

### WorkItem Perticipant can view workItem from My Tasks List
As a Employee, I can see workItems in My Tasks list, which assignee/partipent is me, and can switch view all WorkItem Or the workItems which is in 'Active' phase(default) 

### WorkItem participant add comments
As a workItem participant, I can take a 'Note' to the workItem, the note type will be COMMENTS

### workItem event notify related participent
As a participent of a workItem, I can receive all event about this workItem

---
# WorkItem split to Meeting, Task, CheckPoint

---
# Add social interaction
## add concern
## interaction like Ding, Zan, Pai...

---
# Gernerate report
## personal daily/weekly report
## project daily/weekly/phase report

-- 
# support Other status, BLOCK, SUSPEND

--- 
# Project re-play and analsys
## event replay
## project report: delay report, time report...

---
# Support customized filed and customzed enum for entity

---
# Role & Permission
### Role:
- name:
- permissions

### Permission:
- name: String


### OOB Date:
Permission: {CHANGE_PROJECT_OWNER, CHANGE_PROJECT_MAMAGER, CHANGE_PROJECT_MEMBERS, CHANGE_PROJECT_STAKEHOLDS}
Role: [
  {name:'ProjectOwner',permissions:[]},
  {name:'PerjectManager',permissions:[]},
  {name:'WorkItemOwner',permissions:[]},
  {name:'WorkItemAssignee',permissions:[]},
]





