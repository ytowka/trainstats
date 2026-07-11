select w.id,w.dateTime,w.exercises,s.reps,s.weightKg,s.orderPosition,e.name from workoutentity as w
inner join exercisesetentity as s on w.id = s.workoutId
inner join exerciseentity as e on e.id = s.exerciseId