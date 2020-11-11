### Done 
- Event message to message file
- IdType refactor: Entity change, id generated 
- Remove empty message
- Async process after an event generated
- RF, Calculate content field 
- publish event
- Us, Modify project, include modify the owner, the manager, the name, might need audit record

Delete/cancel a projct
Aa a Project owner, or mamager I can delete/cancel my project, delete will drop all of project data. related data should be dropped cascadelly; cancel will mark project as 'STOP', related person will receive Message.
- Add action cancel (done)
- Can not modify project after STOP, 400 should be return (done)
- Notify all participants that the project was cancel.  (done)
- Delete project, and notify all participants (done)

refine message rule and event, save event args in array
- args saved in one field  (done)
- type + key => enum (done)
- remove priority (done)
- send project.removed event (done)
- remove endEmit (done)
- remove enum EventType (done)

### Suspend

### Next

Add new phase

As a project manager, I can insert phase in Project Edit view, information I provided: after/before phase, phase name, planned end date. Constraint, planned start date < planned end date .

- Append phase entity(done)
- Remove phase (done)
- Insert phase (done)
- Change plannedStartDate and plannedEndDate (doing)




